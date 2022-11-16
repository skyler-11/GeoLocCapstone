package com.example.geoloccapstone;

import android.app.AlertDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private BottomNavigationView bottomNav;
    private FrameLayout frameLayout;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    private Button mLogout;

    String  ssuID;

     ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Guard Tour Panel");



        initComponents();
        checkLoggedIn();
        checkLocationPermission();
//        dtrQuery();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                    new DtrFragment()).commit();
        }


        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.nav_dtr:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,
                                new DtrFragment()).commit();
                        break;

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

      if(item.getItemId() == R.id.logout) {
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

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_DENIED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This Application needs Location permission, Please Enable Location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            }
        }else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
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
    }

}