package com.example.khalid.mapandplacessample;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 4546;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 16f;

    //Views
    private EditText mSearchTxt;
    private ImageView mGps;
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSearchTxt = findViewById(R.id.input_search);

        getLocationPermission();
        mGps = findViewById(R.id.img_gps);


    }

    private void initMap() {


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        Toast.makeText(this, "Map is Ready ", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }


    private void init() {
        Log.d(TAG, "init: inintilizing..");
        mSearchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    //execute search method
                    geoLocate();
                }


                return false;
            }

            private void geoLocate() {
                Log.d(TAG, "geoLocate: geo locating");
                String searchString = mSearchTxt.getText().toString();
                Geocoder geocoder = new Geocoder(MapActivity.this);
                List<Address> list = new ArrayList<>();
                try {
                    list = geocoder.getFromLocationName(searchString, 1);
                    if (list.size() > 0) {
                        Address address = list.get(0);
                        Log.d(TAG, "geoLocate: Address is " + address.toString());
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        moveCamera(latLng, DEFAULT_ZOOM, "hello from " + address.getCountryName());
//                     Toast.makeText(MapActivity.this, "Address is "+address.toString(), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "geoLocate: IO Exception " + e.getMessage());

                }
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device current Location ");
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Log.d(TAG, "getDeviceLocation: getting last location ");
                Task location = mFusedLocationProvider.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: location is here ");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(
                                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "my location");
                        } else {
                            Log.d(TAG, "onComplete: last location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else {
                Log.d(TAG, "getDeviceLocation: ");
                Toast.makeText(this, "please allow the location permmission", Toast.LENGTH_SHORT).show();
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: error wuth getting the device location , e Msg: " + e.getMessage(), e);
        }

    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving  the camera to Lat= " + latLng.latitude + "Lon =" + latLng.longitude);

        //without animation
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        //with animate

        mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(latLng, zoom)));

        if (!title.equals("my location")) {

            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(options);
        }

    }


    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "getLocationPermission: Permission granted");
                mLocationPermissionGranted = true;
                //initialize the map
                initMap();

            } else {
                Log.d(TAG, "getLocationPermission: Requesting permission");
                ActivityCompat.requestPermissions(MapActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "getLocationPermission: Requesting permission");
            ActivityCompat.requestPermissions(MapActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {

                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionsResult: permission denied in " + permissions[i]);

                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted ");
                    mLocationPermissionGranted = true;
                    //initialize the map
                    initMap();
                }
            }
        }
    }


}
