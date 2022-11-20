package com.example.geoloccapstone;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapsFragment extends Fragment implements OnMapReadyCallback,GeoQueryEventListener {

    private List<LatLng> checkPoints;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private GeoQuery geoQuery;
    private GeoFire geoFire;
    private FirebaseAuth mAuth;
    private DatabaseReference myLocationRef;
    private String currentUsr;
    private FirebaseFirestore db;
    private String ssuID, firstName, middleName, lastName;
    Notification notification;

    public MapsFragment() {

    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUsr = mAuth.getCurrentUser().getUid();

        dataQuery();
        initArea();
        settingGeoFire();
        buildLocationRequest();
        buildLocationCallback();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.Map);

        supportMapFragment.getMapAsync(MapsFragment.this);

        return view;
    }


    private void dataQuery() {
        db = FirebaseFirestore.getInstance();
        db.collection("appVerify")
                .document(currentUsr)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        ssuID = task.getResult().getString("ssuID");
                        firstName = task.getResult().getString("firstName");
                        middleName = task.getResult().getString("middleName");
                        lastName = task.getResult().getString("lastName");

                    }else {
                        Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null) {
                    //instance of an object
                    geoFire.setLocation(ssuID, new GeoLocation(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (currentUser != null) {
                                currentUser.remove();
                                currentUser = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                                locationResult.getLastLocation().getLongitude()))
                                        .title("You"));
                            } else {
                                currentUser = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                                locationResult.getLastLocation().getLongitude()))
                                        .title("You"));
                            }
                            mMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(currentUser.getPosition(), 17.0f));
                        }
                    });
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.getMinUpdateDistanceMeters();
    }

    private void settingGeoFire() {

        myLocationRef = FirebaseDatabase.getInstance().getReference("Geo Fencing - Letran").child(currentUsr);
        geoFire = new GeoFire(myLocationRef);
    }

    private void initArea() {
        checkPoints = new ArrayList<>();
        checkPoints.add(new LatLng(14.190125,121.164985));
        checkPoints.add(new LatLng(14.189719,121.165591));
        checkPoints.add(new LatLng(14.189461,121.165995));
        checkPoints.add(new LatLng(14.189325,121.166210));
        checkPoints.add(new LatLng(14.189055,121.166599));
        checkPoints.add(new LatLng(14.188734,121.167028));
        checkPoints.add(new LatLng(14.188318,121.167632));
        checkPoints.add(new LatLng(14.187411,121.166008));
        checkPoints.add(new LatLng(14.187487,121.165900));
        checkPoints.add(new LatLng(14.187933,121.165372));
        checkPoints.add(new LatLng(14.188363,121.164988));
        checkPoints.add(new LatLng(14.188695,121.164738));
        checkPoints.add(new LatLng(14.188966,121.164322));
        checkPoints.add(new LatLng(14.189505,121.163846));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotif(String.format("%s Entered a Patrol Area",key));
    }
    @Override
    public void onKeyExited(String key) {
        sendNotif(String.format("%s Exited a Patrol Area",key));
    }
    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotif(String.format("%s Moved within a Patrol Area",key));
    }
    @Override
    public void onGeoQueryReady() {

    }
    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void sendNotif(String content) {

        String NOTIFICATION_CHANNEL_ID = "GeoLocApp";
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.setDescription(("Channel Description"));
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
        notificationChannel.enableVibration(true);
        notificationManager.createNotificationChannel(notificationChannel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle("GeoLocApp")
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));

        notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);

        Map<String, Object> notif = new HashMap<>();
        notif.put("locUpdate", content);
        notif.put("timeStamp", FieldValue.serverTimestamp());
        notif.put("firstName", firstName);
        notif.put("middleName", middleName);
        notif.put("lastName", lastName);

        db.collection("guardLocation").document(currentUsr).set(notif)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(), ""+content, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null){
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        for(LatLng latLng : checkPoints)
        {
            mMap.addCircle(new CircleOptions().center(latLng)
                    .radius(15)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f));

            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.010f);
            geoQuery.addGeoQueryEventListener(MapsFragment.this);
        }
    }
}

