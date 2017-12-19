package course.examples.footprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyNotesActivity extends AppCompatActivity {
    private ListView listView_myNotes;
    private List<Note> myNotesList;
    private List<Map<String, Object>> screenList;
    private FirebaseFirestore fs;
    private CollectionReference notesRef;
    private FirebaseUser user;
    private SimpleAdapter adapter_myNotes;
    private List<String> noteidList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        myNotesList = new ArrayList<>();
        screenList = new ArrayList<>();
        noteidList = new ArrayList<>();
        listView_myNotes = (ListView)findViewById(R.id.lst_my_notes) ;


        adapter_myNotes = new SimpleAdapter(getApplicationContext(), screenList,R.layout.item_fragment_list,
                new String[]{"userImage","title"},
                new int[]{R.id.image_item_list, R.id.txt_item_fragment_list_title});
        user = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(getApplicationContext(),user.getUid(),Toast.LENGTH_SHORT).show();
        fs=FirebaseFirestore.getInstance();
        notesRef=fs.collection("notes");
        notesRef.whereEqualTo("userid",user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Map<String, Object> newMap = new HashMap<>();
                        newMap.put("title", document.getString("title"));
                        newMap.put("content", document.getString("content"));
                        newMap.put("userid", document.getString("userid"));
                        newMap.put("timestamp", document.get("timestamp"));
                        newMap.put("userImage", R.drawable.image_list);
                        newMap.put("lon", document.get("lon"));
                        newMap.put("lat", document.get("lat"));
                        newMap.put("address", document.get("address"));
                       // newMap.put("noteid", document.getId());
                        newMap.put("nickname", document.getString("nickname"));
                        Note newNote = new Note(newMap);
                        noteidList.add(document.getId());
                        screenList.add(newMap);
                        myNotesList.add(newNote);
                    }


                    listView_myNotes.setAdapter(adapter_myNotes);


                    listView_myNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                            Intent intent = new Intent(MyNotesActivity.this, ViewNoteActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("noteid", noteidList.get(i));
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });


                }
            }


        });
    }
}
