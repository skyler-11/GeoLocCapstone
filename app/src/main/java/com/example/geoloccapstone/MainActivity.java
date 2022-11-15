package com.example.geoloccapstone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FrameLayout frameLayout;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    private Button mLogout;

    String  ssuID;

     ActionBar actionBar;


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Home");



        initComponents();
        checkLoggedIn();
        checkLocationPermission();
        dtrQuery();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                    new MapsFragment()).commit();
        }

//        mLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                mAuth.signOut();
////                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                mAuth.signOut();
//                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//                finish();
//            }
//        });

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                        new MapsFragment()).commit();
                switch (item.getItemId()) {
                    case R.id.nav_map:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                                new MapsFragment()).commit();
                        break;
                    case R.id.nav_report:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                                new ReportFragment()).commit();
                        break;
                }
                return true;
            }
        });
    }


    private void dtrQuery() {
        db.collection("appVerify")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        ssuID = task.getResult().getString("ssuID");

                    }else {
                        Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

      if(item.getItemId() == R.id.logout) {
//          Date c = Calendar.getInstance().getTime();
//          SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.getDefault());
//          final String formattedDate = df.format(c);

          Map<String, Object> logout = new HashMap<>();
          logout.put("dtrLogout", FieldValue.serverTimestamp());
          logout.put("ssuID", ssuID);

          db.collection("timeRecord").document(mAuth.getUid()).set(logout, SetOptions.merge())
                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  Toast.makeText(MainActivity.this, "Log-out Recorded",Toast.LENGTH_LONG).show();
                              }
                          }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                      }
                  });
          mAuth.signOut();
          startActivity(new Intent(MainActivity.this, LoginActivity.class));
          finish();
      }
        return super.onOptionsItemSelected(item);
    }

    private void initComponents() {

//      mLogout = (Button) findViewById(R.id.buttonLogout);
        bottomNav = findViewById(R.id.main_bottom_navigation);
        frameLayout = findViewById(R.id.main_framelayout);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                                //buildGoogleApiClient();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }

    }

    private void checkLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (mAuth.getCurrentUser() != null){
            if (user != null) {
                if (user.isEmailVerified()) {
//                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                else{
                    mAuth.signOut();
                    Toast.makeText(getApplicationContext(), "Please verify your account!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        }
        else{
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                   case R.id.nav_map:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                               new MapsFragment()).commit();
                       break;
                    case R.id.nav_report:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                                new ReportFragment()).commit();
                        break;
                }
                return true;
            }
        });
    }

}