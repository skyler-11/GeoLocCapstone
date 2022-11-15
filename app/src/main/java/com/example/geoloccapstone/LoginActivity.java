package com.example.geoloccapstone;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;

    private Button mLogin;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //private DatabaseReference mDatabase;

    private ProgressDialog mProgress;
    private FirebaseFirestore db;
    private TextView fpass;
    String ssuID;

//    //Shared-Pref
//    Intent in;
//    public static final String MyPREFERENCES = "MyPrefs" ;
//    public static final String Emails = "emailKey";
//    public static final String Password1 = "passKey";
//    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponent();
        //queryeID();

        if (mAuth != null) {
            currentUser = mAuth.getCurrentUser();
        }

        fpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordReset = new AlertDialog.Builder(v.getContext());
                passwordReset.setTitle("Reset Password ?");
                passwordReset.setMessage("Enter Your Email to Receive Reset Link.");
                passwordReset.setView(resetMail);

                passwordReset.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LoginActivity.this, "Reset Link has been Sent to your email!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Reset Email is not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordReset.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });
                passwordReset.create().show();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }
    private void initComponent() {
        mEmail = (EditText) findViewById(R.id.editTextEmail);
        mPassword = (EditText) findViewById(R.id.editTextPassword);

        mLogin = (Button) findViewById(R.id.buttonLogin);
        fpass = findViewById(R.id.fpassw);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        mProgress = new ProgressDialog(this);

//        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    private void login() {
        mProgress.setTitle("Logging in");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.show();

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            mEmail.setError("Required field!");
            mProgress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(password)){
            mPassword.setError("Required field!");
            mProgress.dismiss();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    if (!mAuth.getCurrentUser().isEmailVerified()) {
                        Toast.makeText(getApplicationContext(), "Please verify your account through your email!", Toast.LENGTH_SHORT).show();
                    }

                    String dtrI = UUID.randomUUID().toString();

                    Map<String, Object> dtr = new HashMap<>();
                    dtr.put("dtrID", dtrI);
                    dtr.put("dtrLogin", FieldValue.serverTimestamp());
                    dtr.put("email", email);

                    db.collection("timeRecord").document(mAuth.getUid()).set(dtr)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(LoginActivity.this, "Log-in Recorded",Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    mProgress.dismiss();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
//                    in = new Intent(LoginActivity.this,MainActivity.class);
//                    startActivity(in);
                }
                else{
                    mProgress.dismiss();
                    Toast.makeText(getApplicationContext(), "Incorrect Email or Password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
//    private void queryeID() {
//        db.collection("appVerify")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if(task.isSuccessful()){
//                            for(DocumentSnapshot documentSnapshot: task.getResult()){
//                                ssuID = documentSnapshot.getString("eID");
//                            }
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
    }