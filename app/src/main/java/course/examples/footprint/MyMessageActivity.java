package course.examples.footprint;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

public class MyMessageActivity extends AppCompatActivity {

    private ListView listView;
    private List<Map<String, Object>> messageList;
    private FirebaseFirestore fs;
    private CollectionReference notesRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_message);

        fs = FirebaseFirestore.getInstance();
        notesRef = fs.collection("comments");
        user = FirebaseAuth.getInstance().getCurrentUser();
        listView=findViewById(R.id.lv_message);
        messageList = new ArrayList();


        final SimpleAdapter baseAdapter = new SimpleAdapter(this,messageList,R.layout.item_mymessage_list,new String[]{"commenter","content"},new int[]
                {R.id.tv_message_Commenter,R.id.tv_message_content});

        notesRef.whereEqualTo("noteuserid",user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot document : task.getResult()) {
                    Map<String, Object> newMap = new HashMap<>();
                    newMap.put("content",document.get("content"));
                    newMap.put("commenter",document.get("userid"));
                    messageList.add(newMap);
                    baseAdapter.notifyDataSetChanged();
                }
            }
        });

        listView.setAdapter(baseAdapter);





    }
}
