package course.examples.footprint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;



public class AddNoteActivity extends AppCompatActivity {

    private EditText edt_title;
    private EditText edt_content;

    private GridView gridView;
    private Button btn_add_pic;
    private TextView btn_pick_location;

    private FirebaseUser user;

    //database
    private FirebaseFirestore mDatabase;

    //storage
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private Note newNote;
    private String noteId;
    private Activity _this = this;

    //a list store paths of picked picture.
    private ArrayList<String> photoPaths;
    private Place place;
    private static int MAP_ADDRESS_PICKER_CODE = 608;
    private static final int PLACE_PICKER_REQUEST = 181;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        user = FirebaseAuth.getInstance().getCurrentUser();
        initCloudDatabase();
        initCloudStorage();

        edt_title = findViewById(R.id.edt_title);
        edt_content = findViewById(R.id.edt_content);
        gridView = findViewById(R.id.grid_view);
        btn_add_pic = findViewById(R.id.btn_add_picture);
        btn_pick_location = findViewById(R.id.btn_pick_location_add);

        View.OnClickListener addImage = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> photoPaths = new ArrayList<>();
                FilePickerBuilder.getInstance().setMaxCount(9)
                        .setSelectedFiles(photoPaths)
                        .setActivityTheme(R.style.AppTheme)
                        .pickPhoto(_this);
            }
        };

        View.OnClickListener addLocation = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(_this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        };

        btn_add_pic.setOnClickListener(addImage);
        btn_pick_location.setOnClickListener(addLocation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuaddnote, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.btnMenuAdd: {
                // title is empty
                if (edt_title.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Title must not be empty!", Toast.LENGTH_SHORT).show();
                }
                // content is empty
                else if (edt_content.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Content must not be empty!", Toast.LENGTH_SHORT).show();
                }else if (place==null){
                    Toast.makeText(this, "Place must not be empty!", Toast.LENGTH_SHORT).show();
                }
                // add notes
                else {
                    // add notes...
                    initMap();
                    addNote();
                    Toast.makeText(this, "Notes added", Toast.LENGTH_SHORT).show();
                }


                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    //activity callbacks
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    addPhotoToView();
                }
                break;
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    place = PlacePicker.getPlace(data, this);
                    if (place!= null) {
                        String showMsg = String.format("%s", place.getName());
                        btn_pick_location.setText(showMsg + "\n Click to reset");
                        Toast.makeText(this, showMsg, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void addPhotoToView() {
        gridView.setAdapter(new ImageListAdapter(AddNoteActivity.this, photoPaths));
    }

    private void initMap(){
        Map<String, Object> dataMap;
        dataMap = new HashMap<>();
        dataMap.put("title", edt_title.getText().toString());
        dataMap.put("content", edt_content.getText().toString());
        dataMap.put("timestamp", getTimeStamp());
        dataMap.put("userid", user.getUid());
        dataMap.put("nickname", user.getDisplayName());
        dataMap.put("lon", place.getLatLng().longitude);
        dataMap.put("lat", place.getLatLng().latitude);
        dataMap.put("address", place.getAddress());
       // dataMap.put("nickname",user.getDisplayName());
       // dataMap.put("noteid",noteId);

        newNote = new Note(dataMap);
    }

    private void addNote() {

        mDatabase.collection("notes")
                .add(newNote)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.v("dbtest", "success with ID: "+documentReference.getId());
                        noteId = documentReference.getId();
                        if (!photoPaths.isEmpty())
                            uploadMultiPic();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("dbtest", "Error writing document", e);
                    }
                });
        finish();
    }

    private void uploadMultiPic() {
        for (String path : photoPaths)
            uploadPic(path, photoPaths.indexOf(path));
    }

    private void uploadPic(String path, int picNum) {
        Uri file = Uri.fromFile(new File(path));
        //use the local file name as the name online.
//        StorageReference riversRef = storageRef.child(user.getUid()+"/"+noteId+"/"+file.getLastPathSegment());
        StorageReference riversRef = storageRef.child(user.getUid()+"/"+noteId+"/"+picNum+".jpg");
        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.v("storagetest", "upload FAIL!", exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Log.v("storagetest", "upload success downloadURL: "+taskSnapshot.getDownloadUrl());
                Toast.makeText(_this, "Picture Upload Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Location getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location==null)
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location==null)
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        return location;
    }

    private Long getTimeStamp() {
        long time = System.currentTimeMillis() / 1000;
        return time;
    }

    //cloud storage
    private void initCloudStorage() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://footprint-rutgers.appspot.com/note");

    }
    //database
    private void initCloudDatabase() {
        mDatabase = FirebaseFirestore.getInstance();
    }

}
