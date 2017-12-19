package course.examples.footprint;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MarkerListActivity extends AppCompatActivity {

    private ListView lst_marker_content;
    private MyAdapter mAdapter;
    List<String> TitleStringList;
    List<String> AuthorStringList;

    List<String> IdStringList;
    List<Note> noteList;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private CollectionReference markerRef;
    private CollectionReference noteRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_list);

        // get passed address data
        Bundle bundle = getIntent().getExtras();
        String address = bundle.getString("address");
        noteList = new ArrayList<>();
        TitleStringList = new ArrayList<>();
        AuthorStringList = new ArrayList<>();
        IdStringList = new ArrayList<>();
        mAdapter = new MyAdapter(getApplicationContext(),TitleStringList,IdStringList, AuthorStringList);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        markerRef = db.collection("notes");
        noteRef = db.collection("notes");
        lst_marker_content = findViewById(R.id.lst_marker_content);

        // query notes about the specific address
        markerRef.whereEqualTo("address", address).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        TitleStringList.add(document.getString("title"));
                        AuthorStringList.add(document.getString("nickname"));
                        //Toast.makeText(getApplicationContext(),"title is "+ tmptitle,Toast.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();
                        lst_marker_content.setAdapter(mAdapter);
                        IdStringList.add(document.getId());
                    }
                } else {
                    Log.d(TAG, "Error getting marker documents: ", task.getException());
                }
            }
        });

        // -------------------  on click listener -----------------
        lst_marker_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                noteRef.whereEqualTo(FieldPath.documentId(), mAdapter.IdStringList.get(i)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Intent intent = new Intent(MarkerListActivity.this,ViewNoteActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("noteid", document.getId());
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            }
                        } else {
                            Log.d(TAG, "Error getting marker documents: ", task.getException());
                        }
                    }
                });
            }
        });
    }

}
