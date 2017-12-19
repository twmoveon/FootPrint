package course.examples.footprint;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,LocationListener ,com.google.android.gms.location.LocationListener {

    private PlaceAutocompleteFragment autocompleteFragment;
    private MapView mapView;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private boolean mLocationPermissionGranted = true;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private static final int DEFAULT_ZOOM = 15;
    private LocationRequest mLocationRequest;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private Marker searchResult;
    private FirebaseUser user;
    private HashMap<String,Object> markerMap;
    private Location mCurrentLocation;
    private FloatingActionButton btn_addNote;
    private  FirebaseFirestore db;
    private CollectionReference notesRef;
    private Activity activity = getActivity();

    public MapFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        // initialize mapview
        try
        {
            // Gets the MapView from the XML layout and creates it
            mapView = (MapView) v.findViewById(R.id.mapview);
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            // Gets to GoogleMap from the MapView and does initialization stuff
            mapView.getMapAsync(this);

            // connect Google API Client
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage((FragmentActivity) getActivity() /* FragmentActivity */,
                            this /* OnConnectionFailedListener */)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
            mGoogleApiClient.connect();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return v;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        if (activity==null)
            activity = getActivity();

        // get saved location
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // bind floating action button
        btn_addNote = activity.findViewById(R.id.btn_addNote);
        // floating action button on click listener
        btn_addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddNoteActivity.class);
                startActivity(intent);
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();

        notesRef = db.collection("notes");
        markerMap = new HashMap<>();

        // ---------------- auto complete fragment, implement methods after selecting places ------------------------
        autocompleteFragment = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.map_place_autocomplete_fragment);
        // change getFragmentManager into getChildFragmentManager since its a fragment inside list fragment
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {
                // Get info about the selected place.
                Log.i(TAG, "Search Result: " + place.getName());
                // clear map on new search
                mMap.clear();
                markerMap.clear();
                notesRef = db.collection("notes");

                // move camera to searched place
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
                // remove former marker
                if (searchResult!=null)
                {
                    searchResult.remove();
                }
                // add a marker to show the search result
                searchResult = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng()).title(place.getName().toString()).snippet(place.getAddress().toString())
                        .draggable(false));
                searchResult.showInfoWindow();

                // get filtered data from database and add markers to the map, show selected place's info

                notesRef.whereGreaterThan("lon",place.getLatLng().longitude - 0.5).whereLessThan("lon",place.getLatLng().longitude + 0.5).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                // filter latitude locally since firestore doesn't provide compound query on multi fields, can try to change data model in firestore
                                if (document.getDouble("lat") < place.getLatLng().latitude + 0.5 && document.getDouble("lat") > place.getLatLng().latitude - 0.5)
                                {
                                    //Toast.makeText(getContext(),"Add marker", Toast.LENGTH_SHORT).show();
                                    addMarker(document);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
            // on place selected
            @Override
            public void onError(Status status) {
                // Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // ---------------- auto complete fragment ends ------------------------

    }

    private void addMarker(final DocumentSnapshot document)
    {
        // determine this address already existed or not

        if (!markerMap.containsKey(document.getString("address")))
        {
            Toast.makeText(getActivity().getApplicationContext(),"Filtered Results: " + document.getData().get("address").toString(),Toast.LENGTH_SHORT).show();
            // get location info
            LatLng markerLatlng = new LatLng(Double.parseDouble(document.getData().get("lat").toString()),Double.parseDouble(document.getData().get("lon").toString()));
            // add marker according to address
            Marker marker = mMap.addMarker(new MarkerOptions().position(markerLatlng).title(document.getString("address")));
            // put marker into markerMap indexed by address
            markerMap.put(document.getString("address"),marker);
            // on click listener
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    // show info window when clicked
                    marker.showInfoWindow();
                    startViewNotesActivity(marker.getTitle());

                    return true;
                }
            });
        }
        // existed in marker map
        else
        {
            Log.d(TAG, "Note ID: " + document.getId()+": address on " + document.getString("address") + " already exists!");
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Enable / Disable my location button
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
        // Enable / Disable Compass icon
        mMap.getUiSettings().setCompassEnabled(true);
        // Enable / Disable Rotate gesture
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        mMap.getUiSettings().setZoomGesturesEnabled(true);
       // Toast.makeText(getActivity().getApplicationContext(), " mapready", Toast.LENGTH_SHORT).show();

        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    // update locationUI
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                //getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG,"connected");
        startLocationUpdates();
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity().getApplicationContext(), "connection failed", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
    /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }
        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
           // Toast.makeText(getActivity().getApplication(),"mCameraPosition != null",Toast.LENGTH_SHORT).show();
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            //Toast.makeText(getActivity().getApplication(),"mLastKnownLocation != null",Toast.LENGTH_SHORT).show();

        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
           // Toast.makeText(getActivity().getApplication(),"Current location is null. Using defaults.",Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastKnownLocation = location;
        LatLng current = new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    public void startViewNotesActivity(String title)
    {
        Intent intent = new Intent(getActivity(),MarkerListActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("address",title );
        intent.putExtras(bundle);
        startActivity(intent);
    }



}
