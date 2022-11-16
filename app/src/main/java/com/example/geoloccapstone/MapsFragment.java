package com.example.geoloccapstone;


import static android.os.Build.VERSION_CODES.M;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GeoQueryEventListener {

    private List<LatLng> checkPoints;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private GeoFire geoFire;
    private FirebaseAuth mAuth;
    private DatabaseReference myLocationRef;
    private FirebaseUser currentUsr;

    public MapsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.Map);

        supportMapFragment.getMapAsync(MapsFragment.this);
        return view;
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null) {
                    //instance of an object
                    geoFire.setLocation(currentUsr.getUid(), new GeoLocation(locationResult.getLastLocation().getLatitude(),
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

        myLocationRef = FirebaseDatabase.getInstance().getReference("Geo Fencing - Letran").child("Logs");
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
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotif("GeoLocApp", String.format("%s Entered a Checkpoint",key));
    }


    @Override
    public void onKeyExited(String key) {
        sendNotif("GeoLocApp", String.format("%s Exited a Checkpoint",key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotif("GeoLocApp", String.format("%s Moved within a Checkpoint",key));
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void sendNotif(String title, String content) {

        Toast.makeText(getActivity(), ""+content, Toast.LENGTH_SHORT).show();

        String NOTIFICATION_CHANNEL_ID = "GeoLocApp";
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription(("Channel Description"));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mAuth = FirebaseAuth.getInstance();
        mMap = googleMap;


        initArea();
        settingGeoFire();
        buildLocationRequest();
        buildLocationCallback();

        if (mAuth != null) {
            currentUsr = mAuth.getCurrentUser();
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null){
            if (Build.VERSION.SDK_INT >= M) {
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
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

            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.015f);
            geoQuery.addGeoQueryEventListener(MapsFragment.this);
        }
}
}

