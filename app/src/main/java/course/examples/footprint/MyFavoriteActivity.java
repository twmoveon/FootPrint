package course.examples.footprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;



public class MyFavoriteActivity extends AppCompatActivity {
    private ListView listView_myNotes;
    private List<Note> myNotesList;
    private List<Map<String, Object>> screenList;
    private FirebaseFirestore fs;
    private CollectionReference notesRef;
    private CollectionReference favoriteRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        user = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(getApplicationContext(), user.getUid(), Toast.LENGTH_SHORT).show();
        fs = FirebaseFirestore.getInstance();
        favoriteRef = fs.collection("favorite");
        favoriteRef.document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() || task.getResult().get("0") != null) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> noteList = document.getData();
                    updateList(noteList);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void updateList(Map<String, Object> noteList) {
        for (Object noteid : noteList.values()) {
            Log.v("note info", "note ID:" + (String) noteid);
            myNotesList = new ArrayList<>();
            screenList = new ArrayList<>();
            listView_myNotes = (ListView) findViewById(R.id.lst_my_notes);
            final SimpleAdapter adapter_myNotes = new SimpleAdapter(getApplicationContext(), screenList, R.layout.item_fragment_list,
                    new String[]{"userImage", "title"},
                    new int[]{R.id.image_item_list, R.id.txt_item_fragment_list_title});
            DocumentReference notesRef=FirebaseFirestore.getInstance().collection("notes").document(String.valueOf(noteid));
            notesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if (task.isSuccessful()) {
                        Map<String, Object> newMap = new HashMap<>();
                        newMap.put("title", document.get("title"));
                        newMap.put("content", document.get("content"));
                        newMap.put("userid", document.get("userid"));
                        newMap.put("timestamp", document.get("timestamp"));
                        newMap.put("userImage", R.drawable.image_list);
                        newMap.put("lon", document.get("lon"));
                        newMap.put("lat", document.get("lat"));
                        newMap.put("address", document.get("address"));
                        newMap.put("noteid", document.getId());
                        newMap.put("nickname", document.get("nickname"));
                        Note newNote = new Note(newMap);
                        screenList.add(newMap);
                        myNotesList.add(newNote);

                        listView_myNotes.setAdapter(adapter_myNotes);

                        listView_myNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                Intent intent = new Intent(getApplicationContext(), ViewNoteActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("currentNote", myNotesList.get(i));
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });

                    }
                }
            });
        }
    }
}
