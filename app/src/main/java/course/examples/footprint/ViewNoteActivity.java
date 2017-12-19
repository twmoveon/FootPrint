package course.examples.footprint;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ViewNoteActivity extends AppCompatActivity {

    private TextView txt_view_title;
    private TextView txt_view_author;
    private TextView txt_view_content;
    private ImageView img_view1;
    private ImageView img_view2;
    private ImageView img_view3;
    private ImageView img_view4;
    private ImageView img_view5;
    private ImageView img_view6;
    private ImageView img_view7;
    private ImageView img_view8;
    private ImageView img_view9;
    private Button btnAddComment;
    private EditText editText;
    private Note currentNote;
    StorageReference storageRef;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private CollectionReference noteRef;

    private FirebaseUser user;
    private RequestOptions glideOptions;
    private FirebaseFirestore mDatabase;
    private Map<String,Object> commentMap;
    private String noteid;
    private Note passedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        Bundle bundle = getIntent().getExtras();
        noteid = bundle.getString("noteid");
        //currentNote = (Note)bundle.getSerializable("currentNote");

        txt_view_title = findViewById(R.id.txt_view_title);
        txt_view_author = findViewById(R.id.txt_view_author);
        txt_view_content = findViewById(R.id.txt_veiw_content);
        img_view1 = findViewById(R.id.img_view1);
        img_view2 = findViewById(R.id.img_view2);
        img_view3 = findViewById(R.id.img_view3);
        img_view4 = findViewById(R.id.img_view4);
        img_view5 = findViewById(R.id.img_view5);
        img_view6 = findViewById(R.id.img_view6);
        img_view7 = findViewById(R.id.img_view7);
        img_view8 = findViewById(R.id.img_view8);
        img_view9 = findViewById(R.id.img_view9);
        editText = findViewById(R.id.edt_comment);
        btnAddComment = findViewById(R.id.btn_addComm);


        db = FirebaseFirestore.getInstance();
        noteRef = db.collection("notes");

        noteRef.whereEqualTo(FieldPath.documentId(),noteid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Toast.makeText(getApplicationContext(),"IDview is "+ document.getId(),Toast.LENGTH_SHORT).show();


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
                        passedNote = new Note(newMap);

                        txt_view_title.setText(passedNote.title);
                        txt_view_author.setText(passedNote.nickname);
                        txt_view_content.setText(passedNote.content);
                        break;

                    }
                } else {
                    Log.d(TAG, "Error getting marker documents: ", task.getException());
                }
            }

        });

        mDatabase = FirebaseFirestore.getInstance();
        glideOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(this,noteid,Toast.LENGTH_SHORT).show();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://footprint-rutgers.appspot.com/");

            StorageReference newRef = storage.getReferenceFromUrl("gs://footprint-rutgers.appspot.com/note");
            newRef.child( user.getUid() + "/" +noteid+"/"+ "0.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Glide.with(getApplicationContext())
                            .load(bytes)
                            .apply(glideOptions)
                            .into(img_view1);
                    img_view1.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                        }
                    });


            newRef.child( user.getUid() + "/" +noteid+"/"+ "1.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Glide.with(getApplicationContext())
                            .load(bytes)
                            .apply(glideOptions)
                            .into(img_view2);
                    img_view2.setVisibility(View.VISIBLE);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        newRef.child( user.getUid() + "/" +noteid+"/"+ "2.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(getApplicationContext())
                        .load(bytes)
                        .apply(glideOptions)
                        .into(img_view3);
                img_view3.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        newRef.child( user.getUid() + "/" +noteid+"/"+ "3.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(getApplicationContext())
                        .load(bytes)
                        .apply(glideOptions)
                        .into(img_view4);
                img_view4.setVisibility(View.VISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        newRef.child( user.getUid() + "/" +noteid+"/"+ "4.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(getApplicationContext())
                        .load(bytes)
                        .apply(glideOptions)
                        .into(img_view5);
                img_view5.setVisibility(View.VISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        newRef.child( user.getUid() + "/" +noteid+"/"+ "5.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(getApplicationContext())
                        .load(bytes)
                        .apply(glideOptions)
                        .into(img_view6);
                img_view6.setVisibility(View.VISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        newRef.child( user.getUid() + "/" +noteid+"/"+ "6.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(getApplicationContext())
                        .load(bytes)
                        .apply(glideOptions)
                        .into(img_view7);
                img_view7.setVisibility(View.VISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
        newRef.child( user.getUid() + "/" +noteid+"/"+ "7.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(getApplicationContext())
                        .load(bytes)
                        .apply(glideOptions)
                        .into(img_view8);
                img_view8.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
        newRef.child( user.getUid() + "/" +noteid+"/"+ "8.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Glide.with(getApplicationContext())
                        .load(bytes)
                        .apply(glideOptions)
                        .into(img_view9);
                img_view9.setVisibility(View.VISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });



     /*   btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText()==null)
                    Toast.makeText(getApplicationContext(),"Comment can't be empty!",Toast.LENGTH_SHORT).show();
                else {
                    String comments = editText.getText().toString();

                    commentMap = new HashMap<>();
                    commentMap.put("content", comments);
                    commentMap.put("userid", user.getUid());
                    commentMap.put("noteid", currentNote.noteid);
                    commentMap.put("noteuserid", currentNote.userid);
                    mDatabase.collection("comments").add(commentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getApplicationContext(), "Add comment", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        */




    }
}
