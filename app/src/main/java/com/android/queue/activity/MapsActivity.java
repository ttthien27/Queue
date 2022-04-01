package com.android.queue.activity;

import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.android.queue.databinding.ActivityMapsBinding;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Geocoder geocoder;
    private String lastMarkerAddress;
    private LatLng lastMarkerLatLng;
    private SessionManager sessionManager;

    //Init needed view in this layout
    private MaterialButton locateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Init session manager
        sessionManager = new SessionManager(this);
        //Find locate button
        locateBtn = findViewById(R.id.locateBtn);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Init geo decoder
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        //Set on locate btn click listener to receive data from map activity
        locateBtn.setOnClickListener(v -> {
            sessionManager.putHostRoomLocation(lastMarkerLatLng, lastMarkerAddress);
            finish();
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng hutech = new LatLng(10.801959752147356D, 106.71444878465739D);
        mMap.addMarker(new MarkerOptions().position(hutech).title("Hutech Điện Biên Phủ"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hutech, 17));
        lastMarkerLatLng = hutech;
        lastMarkerAddress = "Hutech Điện Biên Phủ";

        //Set on map click and get address from clicked point
        mMap.setOnMapClickListener(latLng -> {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            mMap.clear();
            mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            String address = getAddress(latLng);

            lastMarkerLatLng = latLng;
            markerOptions.title(address);
            lastMarkerAddress = address;

        });
    }

    private String getAddress(LatLng point) {
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        android.location.Address address = addresses.get(0);
        return address.getAddressLine(0);


    }
}