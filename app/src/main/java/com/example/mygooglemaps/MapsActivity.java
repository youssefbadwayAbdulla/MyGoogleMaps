package com.example.mygooglemaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mygooglemaps.databinding.ActivityMapsBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String TAG = "map";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Geocoder geocoder;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
   //     getServicesLocations();
      //  mMap.setMyLocationEnabled(true);
        getYoussefLocations();

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setTrafficEnabled(true);
        try {
            List<Address> addresses = geocoder.getFromLocationName("Cairo", 1);
            Address address = addresses.get(0);
            Log.i(TAG, "onMapReady: address>>>" + address.toString());
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(address.getLocality());
            mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));


        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "onMapReady: error" + e);
        }
        //30.3180274,31.7120501,16z
//        LatLng latLng = new LatLng(30.3192024,31.7124578);
//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLng)
//                .title("ده بيتي بقا..")
//                .snippet("طب والله انا جامد...");
//        mMap.addMarker(markerOptions);
//        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngZoom(latLng,16);


//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        getAddress(latLng);
    }

    private void getAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, new Locale("ar", "eg"));
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            String title = addressList.get(0).getAddressLine(0);
            System.out.println(title);
            Log.i(TAG, "getAddress: " + title);

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(title));//how to make marker
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getServicesLocations() {
        firestore.collection("ServicesLocation")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            ServicesLocation servicesLocation = snapshot.toObject(ServicesLocation.class);
                            Log.i(TAG, "onEvent: servicesLocations>>>>" + servicesLocation);

                            LatLng latLng = new LatLng(servicesLocation.getLat(), servicesLocation.getLng());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng);

                            mMap.addMarker(markerOptions);


                        }
                    }
                });

    }

    private void getYoussefLocations() {


        firestore.collection("locations")
                .document("Youssef")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                        ServicesLocation servicesLocation = documentSnapshot.toObject(ServicesLocation.class);
                        if (servicesLocation == null) { return; }
                        //    Log.i(TAG, "onEvent: servicesLocations>>>>"+servicesLocation.toString());
                        LatLng latLng = new LatLng(servicesLocation.getLat(), servicesLocation.getLng());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng);
                        mMap.addMarker(markerOptions);
                    }
                });

    }
}


