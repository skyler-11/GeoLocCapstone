package com.example.geoloccapstone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DtrFragment extends Fragment {

    private Button timeIn, timeOut, logout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    String ssuID, firName, lasName, midName, email;

    public DtrFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dtr, container, false);

        String dtrI = UUID.randomUUID().toString();

        timeIn = view.findViewById(R.id.timeIn);
        timeOut = view.findViewById(R.id.timeOut);
        logout = view.findViewById(R.id.logOut);
        initComp();
        query();


        timeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Map<String, Object> dtr = new HashMap<>();

                dtr.put("dtrLogin", FieldValue.serverTimestamp());
                dtr.put("email", email);
                dtr.put("ssuID", ssuID);
                dtr.put("firstName", firName);
                dtr.put("middleName", midName);
                dtr.put("lastName", lasName);

                db.collection("timeRecord").document(dtrI).set(dtr)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Time-in Recorded", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        timeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> logout = new HashMap<>();
                logout.put("dtrLogout", FieldValue.serverTimestamp());

                db.collection("timeRecord").document(dtrI).set(logout, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Time-out Recorded",Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    private void initComp() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    private void query() {
        db.collection("appVerify")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        ssuID = task.getResult().getString("ssuID");
                        firName = task.getResult().getString("firstName");
                        lasName = task.getResult().getString("lastName");
                        midName =  task.getResult().getString("middleName");
                        email = task.getResult().getString("email");


                    }else {
                        Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();;
                    }
                });
    }
}