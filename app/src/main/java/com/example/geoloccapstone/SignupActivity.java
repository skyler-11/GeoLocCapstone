package com.example.geoloccapstone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private Toolbar toolbar;
    private ImageButton imageButtonBack;

    private EditText editFirstname;
    private EditText editMiddlename;
    private EditText editLastname;
    private EditText suffix;
    private EditText editID;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editConfirmPassword;

    private Button btnRegister;

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initComponents();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LandingActivity.class));
            }
        });
    }

    private void initComponents() {
        toolbar = (Toolbar) findViewById(R.id.toolBarSignUp);
        imageButtonBack = (ImageButton) findViewById(R.id.imgBack);


        editFirstname =  findViewById(R.id.Firstname);
        editMiddlename = findViewById(R.id.Middlename);
        editLastname =  findViewById(R.id.Lastname);
        suffix = findViewById(R.id.Namesuff);
        editID =  findViewById(R.id.idNum);
        editEmail =  findViewById(R.id.Email);
        editPassword =  findViewById(R.id.Password);
        editConfirmPassword = findViewById(R.id.ConfirmPassword);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        mProgress = new ProgressDialog(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void registerUser() {
        mProgress.setTitle("Registering a user");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.show();

        String firstname = editFirstname.getText().toString().trim();
        String middleName = editMiddlename.getText().toString().trim();
        String lastname = editLastname.getText().toString().trim();
        String nameSuf = suffix.getText().toString().trim();
        String id = editID.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmpassword = editConfirmPassword.getText().toString().trim();

        if(mAuth.getCurrentUser() !=null){
            startActivity((new Intent(getApplicationContext(), MainActivity.class)));
        }

        if (TextUtils.isEmpty(firstname)) {
            editFirstname.setError("Required field!");
            mProgress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(middleName)) {
            editMiddlename.setError("Required field!");
            mProgress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(lastname)) {
            editLastname.setError("Required field!");
            mProgress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(nameSuf)) {
            suffix.setError("Required field, Type N/A if not Applicable!");
            mProgress.dismiss();
            return;
        }

        if (TextUtils.isEmpty(id)) {
            editID.setError("Required field!");
            mProgress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Required field!");
            mProgress.dismiss();
            return;
        }
        if (!email.contains("@")) {
            editEmail.setError("Invalid email!");
            mProgress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Required field!");
            mProgress.dismiss();
            return;
        }

        if (password.length() < 8) {
            editPassword.setError("Password Must be 8 characters long!");
        }

        if (TextUtils.isEmpty(confirmpassword)) {
            editConfirmPassword.setError("Required field!");
            mProgress.dismiss();
            return;
        }
        if (!password.equals(confirmpassword)) {
            editConfirmPassword.setError("Password should be equal to your password!");
            mProgress.dismiss();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                FirebaseUser user1 = mAuth.getCurrentUser();
                user1.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "Please verify your account in email", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Verification Email not sent!");
                    }
                });

                //Firestore DB reference

                userID = mAuth.getUid();
                DocumentReference documentReference = db.collection("appVerify").document(userID);

                Map<String, Object> user = new HashMap<>();
                user.put("lastName", lastname);
                user.put("middleName", middleName);
                user.put("firstName", firstname);
                user.put("nameEx", nameSuf);
                user.put("ssuID", id);
                user.put("email", email);

                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: You are Verified User:" + userID);
                    }
                });
                mProgress.dismiss();
                Intent mainIntent = new Intent(SignupActivity.this, LoginActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            }
            else{
                mProgress.dismiss();
                Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}