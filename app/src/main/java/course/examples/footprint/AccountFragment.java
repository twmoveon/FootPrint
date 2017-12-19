package course.examples.footprint;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private Button logoutBtn;
    private Button btn_myNotes_account;
    private Button btn_favorite_account;
    private Button btn_myMessage_account;
    private TextView email;
    private TextView nickname;
    private ImageView profilePicture;

    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference profilePicRef;
    private static final int LOCAL_ALBUM_CODE = 128;
    private static final int CUT_PIC_CODE = 646;
    private static final int HEAD_CALL = 548;
    private static final int USERNAME_CALL = 685;
    private String imagePath;
    private Uri downloadUri;
    private UploadTask uploadTask;
    private RequestOptions glideOptions;
    private Activity activity = getActivity();

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        if (activity==null)
            activity = getActivity();
        while (user==null){
            user = FirebaseAuth.getInstance().getCurrentUser();
        }

        email = activity.findViewById(R.id.tv_email_account);
        logoutBtn = activity.findViewById(R.id.btn_logout);
        profilePicture = activity.findViewById(R.id.image_account);
        nickname = activity.findViewById(R.id.tv_username_account);
        btn_favorite_account = activity.findViewById(R.id.btn_myFavorite_account);
        btn_myNotes_account = activity.findViewById(R.id.btn_myNotes_account);
        btn_myMessage_account = activity.findViewById(R.id.btn_myMessage_account);
        email.setText(user.getEmail());
        nickname.setText(user.getDisplayName());

        //current user information here, initialize database and storage
        initCloudStorage();

        glideOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);


        //get profile picture.
        refreshProfilePicFromDatabase();

        /* setting listeners */
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(activity, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(activity, "You have been logged out.", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(HEAD_CALL);
            }
        });


        btn_myNotes_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity,MyNotesActivity.class);
                startActivity(intent);
            }
        });

        btn_favorite_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MyFavoriteActivity.class);
                startActivity(intent);
            }
        });
        btn_myMessage_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MyMessageActivity.class);
                startActivity(intent);
            }
        });

        /* end of setting listeners */

    }


    //show the menu of change profile picture.
    private void showPopupWindow(int CALL_REQUEST) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();


        View view = null;
        switch (CALL_REQUEST){
            case HEAD_CALL:{
                view = View.inflate(activity, R.layout.popup_change_profile_picture, null);

                //set the position of the dialog
                Window dialogWindow = dialog.getWindow();
//                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();

                dialogWindow.setGravity(Gravity.BOTTOM);

                dialogWindow.setWindowAnimations(R.style.dialogStyle);
//                dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                dialogWindow.setAttributes(lp);

                Button changeProfilePicture = view.findViewById(R.id.btn_change_picture_account);
                Button changeNickname = view.findViewById(R.id.btn_change_nickname_account);
                Button cancel = view.findViewById(R.id.btn_cancel_picture_account);

                //cancel btn
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                //open album and choose picture
                changeProfilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_PICK, null);
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent, LOCAL_ALBUM_CODE);
                        dialog.dismiss();
                    }
                });
                //change the nickname
                changeNickname.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        final View dialogView = View.inflate(activity, R.layout.popup_change_nickname, null);
                        final EditText edtNickname = dialogView.findViewById(R.id.edt_nickname_account);
                        Button btnSubmit = dialogView.findViewById(R.id.btn_change_nickname_submit);
                        Button btnCancel = dialogView.findViewById(R.id.btn_change_nickname_cancel);
                        final AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                        final AlertDialog dialog1 = builder1.create();

                        edtNickname.setText(user.getDisplayName());

                        btnSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //submit the nickname
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(edtNickname.getText().toString()).build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(activity, "Update Nickname success!",Toast.LENGTH_SHORT).show();
                                            nickname.setText(user.getDisplayName());
                                            dialog1.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog1.dismiss();
                            }
                        });
                        builder.setMessage("Input your new fancy nickname here!").setTitle("change nickname");
                        dialog1.setView(dialogView);
                        dialog1.show();
                    }
                });
                break;
            }
        }
        dialog.setView(view);
        dialog.show();
    }

    //upload profile picture
    private void uploadPic(Bitmap bitmap) {
        //transfer the picture to bytes.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        uploadTask = profilePicRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.v("Image","upload fail!");
                Log.v("Image", String.valueOf(exception));
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri Url = taskSnapshot.getDownloadUrl();
                //save url here
                downloadUri = Url;
                Log.v("Image", "upload succeed!");
                Log.v("Image downloadUri", String.valueOf(downloadUri));
                refreshProfilePicFromDatabase();
            }
        });
    }

    @Override
    //activity callbacks
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCAL_ALBUM_CODE: {
                if (resultCode == RESULT_OK || data != null) {
                    Cursor cursor = activity.getContentResolver().query(data.getData(), null, null, null, null);
                    cursor.moveToFirst();
                    //get data called _data
                    imagePath = cursor.getString(cursor.getColumnIndex("_data"));
                    Log.v("ImagePath", imagePath);

                    cursor.close();

                    //cutting pic with system camera action
                    Intent intent1 = new Intent("com.android.camera.action.CROP");
                    Uri uri = data.getData();
                    intent1.setDataAndType(uri,"image/*");
                    intent1.putExtra("outputX", 72);
                    intent1.putExtra("outputY", 72);
                    intent1.putExtra("scale", true);
                    intent1.putExtra("return-data", true);
                    startActivityForResult(intent1, CUT_PIC_CODE);

                }
                break;
            }
            case CUT_PIC_CODE:{
                if(resultCode == RESULT_OK){
                    try{
                        if(data != null){
                            Bitmap bitmap = data.getExtras().getParcelable("data");
                            uploadPic(bitmap);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;

            }
        }
    }

    //refresh the profile picture
    private void refreshProfilePicFromDatabase(){
        //judge if the user's profile picture exist.
        final StorageReference profileRefLocal = storage.getReferenceFromUrl("gs://footprint-rutgers.appspot.com/profilePic");
        profileRefLocal.child(user.getUid()+".jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.v("picUrls", String.valueOf(profileRefLocal.child(user.getUid()+".jpg")));
                Glide.with(activity)
                        .load(bytes)
                        .apply(glideOptions)
                        .into(profilePicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profileRefLocal.child("default.png").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Glide.with(activity)
                            .load(bytes)
                            .apply(glideOptions)
                            .into(profilePicture);
                    }
                });


            }
        });
    }

    //cloud storage
    private void initCloudStorage() {
        storage = FirebaseStorage.getInstance();
        profilePicRef = storage.getReferenceFromUrl("gs://footprint-rutgers.appspot.com/profilePic/"+user.getUid()+".jpg");

    }

}
