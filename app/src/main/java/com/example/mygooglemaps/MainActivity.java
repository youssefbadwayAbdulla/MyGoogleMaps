package com.example.mygooglemaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.mygooglemaps.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private final int Request_Code_Location = 100;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final String TAG = "main";
    private FirebaseFirestore firestore=FirebaseFirestore.getInstance();
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest=LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_Code_Location);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_Code_Location);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
           // getLastLocation();
            checkSettingAndStartLocationUpdate();
        } else {
            askForPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Request_Code_Location) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               checkSettingAndStartLocationUpdate();
                // getLastLocation();
            }
        }

    }


    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    Log.i(TAG, "onSuccess:location>> "+location.toString());
                    Log.i(TAG, "onSuccess: latitude>>"+location.getLatitude());
                    Log.i(TAG, "onSuccess: longtude>>"+location.getLongitude());
                    firestore.collection("locations")
                            .document("UserId")
                            .collection("LastLocation")
                            .document(String.valueOf(System.currentTimeMillis()))
                            .set(location);
                }else {
                    Log.i(TAG, "onSuccess: Location was null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage=e.getLocalizedMessage();
                Log.i(TAG, "onFailure:errorMessage>> "+errorMessage);
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();

            }
        });

    }

    //Location update....

    private void checkSettingAndStartLocationUpdate(){
        LocationSettingsRequest locationSettingsRequest=new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        SettingsClient settingsClient=LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> locationSettingsRequestTask=settingsClient.checkLocationSettings(locationSettingsRequest);
        locationSettingsRequestTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
           StartLocationUpdate();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage=e.getLocalizedMessage();
                Log.i(TAG, "onFailure:errorMessage>> "+errorMessage);
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();

            }
        });

    }

    @SuppressLint("MissingPermission")
    private void StartLocationUpdate() {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }
    private void StopLocationUpdate(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
    LocationCallback locationCallback=new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {


           if(locationResult==null){
               Log.i(TAG, "onLocationResult: null");
               return;
           }
            for (Location location:locationResult.getLocations()) {

                Log.i(TAG, "onLocationResult: >>>"+location.toString());
                String MSG="Late>>"+location.getLatitude()+",Long>>"+location.getLongitude();
                binding.tv.setText(MSG);
                Map<String,String>map=new HashMap<>();
                map.put("LastLocation",location.getLatitude()+" , "+location.getLongitude());

                firestore.collection("locations")
                        .document("Youssef")
                        .set(new ServicesLocation(location.getLatitude(),location.getLatitude()));
            }

        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        StopLocationUpdate();
    }
}