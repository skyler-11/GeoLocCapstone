package com.example.geoloccapstone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {


    public ReportFragment() {
        // Required empty public constructor
    }
    public static final String TAG = "TAG";
    EditText eTitle, pDes;
    TextView emID, empfName, curLoc;
    Button Upload;
    String userID;
    String eID, firName, lasName, midName;

    ProgressDialog pd;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currUser;

    DatabaseReference databaseReference;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);



        //Edit Text
        eTitle = view.findViewById(R.id.pTitle);
        emID = view.findViewById(R.id.empID);
        empfName = view.findViewById(R.id.empName);
        curLoc = view.findViewById(R.id.curLoc);
        pDes = view.findViewById(R.id.pDesc);

        //Button
        Upload = view.findViewById(R.id.pUpload);
        //Progress Dialog
        pd = new ProgressDialog(getActivity());
        //Firestore instance
        db = FirebaseFirestore.getInstance();
        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        currUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = currUser.getUid();



        queryData();
        realtimeQuery();

        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = eTitle.getText().toString().trim();
                String empID = emID.getText().toString().trim();
                String fullName = empfName.getText().toString().trim();
                String curLoc1 = curLoc.getText().toString().trim();
                String pDes1 = pDes.getText().toString().trim();

                uploadData(title,empID,fullName,curLoc1,pDes1);
            }
        });

        return view;
    }

    private void realtimeQuery() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Geo Fencing - Letran").child(mAuth.getCurrentUser().getUid());
         databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot: datasnapshot.getChildren()){
                    DataSnapshot latSnap = snapshot.child("l");
                    Double lat = latSnap.child("0").getValue(Double .class);
                    Double lng = latSnap.child("1").getValue(Double.class);
                    curLoc.setText(lat + ", " + lng);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void queryData() {
        db.collection("appVerify")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                 if(task.isSuccessful() && task.getResult() != null){
                     eID = task.getResult().getString("ssuID");
                     firName = task.getResult().getString("firstName");
                     midName = task.getResult().getString("middleName");
                     lasName = task.getResult().getString("lastName");


                     empfName.setText(lasName + ", " + firName + " " + midName);
                     emID.setText(eID);

                }else {
                     Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                 }
                 });
    }



    private void uploadData(String title, String empID, String fullName, String curLoc1, String pDes1) {
        pd.setTitle("Sending Report to Database");
        pd.show();
//        Date c = Calendar.getInstance().getTime();
//        SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.getDefault());
//        final String formattedDate = df.format(c);

        String reportID = UUID.randomUUID().toString();

        Map<String, Object> reports = new HashMap<>();
        reports.put("reportID", reportID);
        reports.put("eventTitle", title);
        reports.put("ssuID", empID);
        reports.put("fullName", fullName);
        reports.put("coords", curLoc1);
        reports.put("eventDetails", pDes1);
        reports.put("dateTime", FieldValue.serverTimestamp());

        db.collection("reports").document(reportID).set(reports)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),"Report Sent", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        db.collection("reportsArchive").document(reportID).set(reports)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}