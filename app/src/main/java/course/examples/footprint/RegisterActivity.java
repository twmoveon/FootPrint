package course.examples.footprint;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Intent intentToRegister;
    private Button btnReg;
    private EditText textEmail;
    private EditText textPwd;
    private EditText textPwdConfirm;

    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        btnReg = findViewById(R.id.btn_register);

        textEmail = findViewById(R.id.register_email);
        textPwd = findViewById(R.id.register_pwd);
        textPwdConfirm = findViewById(R.id.register_pwd_confirm);

        textEmail.setText("");
        textPwd.setText("");
        textPwdConfirm.setText("");

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = textEmail.getText().toString();
                String pwd = textPwd.getText().toString();
                String pwdC = textPwdConfirm.getText().toString();
                Map<String, String> userInfo = new HashMap<String, String>() ;
                userInfo.put("Email", email);
                userInfo.put("Pwd", pwd);
                userInfo.put("PwdConfirm", pwdC);

                if (checkForm(userInfo))
                    register(userInfo);
            }
        });



    }

    private boolean checkForm(Map userInfo) {
        String email = (String) userInfo.get("Email");
        String pwd = (String) userInfo.get("Pwd");
        String pwdC = (String) userInfo.get("PwdConfirm");

        if (email.equals("")||pwd.equals("")||pwdC.equals("")){
            Toast.makeText(RegisterActivity.this, "You should finish all the blanks first.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!pwd.equals(pwdC)){
            Toast.makeText(RegisterActivity.this, "Confirm password don't match with password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }

    private void register(Map userInfo) {
        String email = (String) userInfo.get("Email");
        String password = (String) userInfo.get("Pwd");
        final String TAG = "REGISTER";
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }
}
