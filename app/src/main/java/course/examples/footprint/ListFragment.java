package course.examples.footprint;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment  {

    private List<Note> notesList;
    private PlaceAutocompleteFragment autocompleteFragment;
    private FirebaseFirestore db;
    private CollectionReference notesRef;
    private SimpleAdapter list_simpleAdapter;
    private List<Map<String , Object>> screenList;
    private Double lat;
    private Double lng;
    private ListView lst_notes_related;
    private List<String> noteidList;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        db=FirebaseFirestore.getInstance();
        notesRef = db.collection("notes");
        notesList = new ArrayList<>();
        screenList = new ArrayList<>();
        noteidList = new ArrayList<>();
        lst_notes_related = getActivity().findViewById(R.id.lst_notes_related);

        list_simpleAdapter = new SimpleAdapter(getActivity(),screenList,R.layout.item_fragment_list,
                new String[]{"userImage", "title", "nickname"},
                new int[]{R.id.image_item_list, R.id.txt_item_fragment_list_title, R.id.txt_item_fragment_list_author});

        lst_notes_related.setAdapter(list_simpleAdapter);

        getCurrentLocation();


        notesRef.whereGreaterThan("lon",lng - 0.5).whereLessThan("lon",lng + 0.5).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // filter latitude locally since firestore doesn't provide compound query on multi fields, can try to change data model in firestore
                        if (document.getDouble("lat") < lat + 0.5 && document.getDouble("lat") > lat - 0.5)
                        {
                            //Toast.makeText(getContext(),"Add marker", Toast.LENGTH_SHORT).show();
                            Map<String, Object> newMap = new HashMap<>();
                            newMap.put("title",document.get("title"));
                            newMap.put("content",document.get("content"));
                            newMap.put("userid",document.getString("userid"));
                            newMap.put("timestamp",document.get("timestamp"));
                            newMap.put("userImage", R.drawable.image_list);
                            newMap.put("lon",document.get("lon"));
                            newMap.put("lat",document.get("lat"));
                            newMap.put("address",document.get("address"));
                            //newMap.put("noteid",document.getId());
                            newMap.put("nickname",document.getString("nickname"));
                            Note newNote = new Note(newMap);
                            screenList.add(newMap);
                            notesList.add(newNote);
                            noteidList.add(document.getId());
                            list_simpleAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


        lst_notes_related.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),ViewNoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("noteid",noteidList.get(i));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });


        // ---------------- auto complete fragment, implement methods after selecting places ------------------------
        autocompleteFragment = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.list_place_autocomplete_fragment);
        // change getFragmentManager into getChildFragmentManager since its a fragment inside list fragment

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO：Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
               // Toast.makeText(getActivity().getApplicationContext(), "Place: "+place.getName(),Toast.LENGTH_SHORT).show();

                screenList.clear();

                notesRef.whereGreaterThan("lon",place.getLatLng().longitude - 0.5).whereLessThan("lon",place.getLatLng().longitude + 0.5).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {


                                Map<String, Object> newMap = new HashMap<>();
                                newMap.put("title",document.get("title"));
                                newMap.put("content",document.get("content"));
                                newMap.put("userid",document.get("userid"));
                                newMap.put("timestamp",document.get("timestamp"));
                                newMap.put("userImage", R.drawable.image_list);
                                newMap.put("lon",document.get("lon"));
                                newMap.put("lat",document.get("lat"));
                                newMap.put("address",document.get("address"));
                                newMap.put("noteid",document.getId());
                                newMap.put("nickname",document.getString("nickname"));
                                Note newNote = new Note(newMap);
                                notesList.add(newNote);
                                screenList.add(newMap);

                            }
                            list_simpleAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

            }

            @Override
            public void onError(Status status) {
                // TODO:Handle the error.
                Log.i(TAG, "An error occurred: " + status);
                Toast.makeText(getActivity().getApplicationContext(), "An error occurred: " + status,Toast.LENGTH_SHORT).show();

            }
        });
        // ---------------- auto complete fragment ends ------------------------


    }


    public void getCurrentLocation()
    {
        String serviceString = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(serviceString);
        String provider = LocationManager.GPS_PROVIDER;
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(provider);
        lat = location.getLatitude();
        lng = location.getLongitude();
        Toast.makeText(getContext(), String.valueOf(lat) + String.valueOf(lng), Toast.LENGTH_SHORT).show();

    }

}
