package com.roam.mqttconnector.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.roam.mqttconnector.R;
import com.roam.mqttconnector.databinding.ActivityMapBinding;
import com.roam.mqttconnector.util.Constants;
import com.roam.mqttconnector.util.GrantPermissions;
import com.roam.mqttconnector.util.Preferences;
import com.roam.sdk.Roam;


import com.roam.sdk.builder.RoamPublish;
import com.roam.sdk.builder.RoamTrackingMode;
import com.roam.sdk.callback.PublishCallback;
import com.roam.sdk.callback.RoamCallback;
import com.roam.sdk.callback.SubscribeCallback;
import com.roam.sdk.callback.TrackingCallback;
import com.roam.sdk.models.RoamError;
import com.roam.sdk.models.RoamUser;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    ActivityMapBinding binding;
    private GoogleMap mMap;
    private Marker currentMarker;
    private Marker marker;
    private CurrentLocationReceiver locationReceiver;

    //battery optimization
    //stationary location method - 300 secs
    //tracking mode -  active

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
    }

    private void initialize() {


        //battery optimization
        Roam.disableBatteryOptimization();

        //enable accuracy engine
       // Roam.enableAccuracyEngine();

        //register broadcast receiver
        if (locationReceiver == null) {
            locationReceiver = new CurrentLocationReceiver();
            IntentFilter intent = new IntentFilter();
            intent.addAction(Constants.CURRENT_LOCATION_INTENT);
            registerReceiver(locationReceiver, intent);
        }

        //initialise map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        //check user id
        checkUserId();

        binding.btLogout.setOnClickListener(this);
    }

    private void checkUserId() {
        if(TextUtils.isEmpty(Preferences.getUserId(this))){
            tracking();
        }else {
            utilityMethods();
        }
    }

    private void utilityMethods() {
        //enable events
        Roam.toggleEvents(true, true, true, true, new RoamCallback() {
            @Override
            public void onSuccess(RoamUser roamUser) {
                //enable listener
                Roam.toggleListener(true, true, new RoamCallback() {
                    @Override
                    public void onSuccess(RoamUser roamUser) {
                        //enable publish

                        RoamPublish roamPublish1 = new RoamPublish.Builder()
                                .build();
                        Roam.publishAndSave(roamPublish1, new PublishCallback() {
                            @Override
                            public void onSuccess(String message) {
                                Log.e("TAG", "onSuccess: "+ message );
                                //subscribe location
                                Roam.subscribe(Roam.Subscribe.LOCATION, Preferences.getUserId(MapActivity.this), new SubscribeCallback() {
                                    @Override
                                    public void onSuccess(String message, String userId) {
                                        Log.e("TAG", "onSuccess: "+userId+" "+message );
                                    }

                                    @Override
                                    public void onError(RoamError error) {
                                        Log.e("TAG", "onFailure: "+ new Gson().toJson(error) );
                                    }
                                });

                                tracking();
                            }

                            @Override
                            public void onError(RoamError error) {
                                Log.e("TAG", "onFailure: "+ new Gson().toJson(error) );
                            }
                        });
                    }

                    @Override
                    public void onFailure(RoamError roamError) {
                        Log.e("TAG", "onFailure: "+roamError.getMessage() );
                    }
                });
            }

            @Override
            public void onFailure(RoamError error) {
                Log.e("TAG", "onFailure: "+error.getMessage() );
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void updateCurrentLocationMarker(Location location) {

        //clear map
        //mMap.clear();

        if (currentMarker != null) {
            currentMarker.remove();
        }


//        // Add a marker for the current location
//        BitmapDescriptor locMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker);
//        LatLng locations = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOption = new MarkerOptions()
//                .position(locations)
//                .icon(locMarker);
//        marker = mMap.addMarker(markerOption);


        // Add a marker for the current location
        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location_marker);
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(currentLocation)
                .title("Current Location")
                .icon(customMarker);
        currentMarker = mMap.addMarker(markerOptions);

        // Move the camera to the current location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btLogout:
                stopTracking();
                break;
        }
    }

    private void tracking() {
        if (GrantPermissions.isAbove29()) {
            checkPermissionsQ();
        } else {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        if (!Roam.checkLocationPermission()) {
            Roam.requestLocationPermission(this);
        } else if (!Roam.checkLocationServices()) {
            Roam.requestLocationServices(this);
        } else {
            startTracking();
        }
    }

    private void checkPermissionsQ() {
        if (!Roam.checkLocationPermission()) {
            Roam.requestLocationPermission(this);
        } else if (!Roam.checkBackgroundLocationPermission()) {
            Roam.requestBackgroundLocationPermission(this);
        } else if (!Roam.checkLocationServices()) {
            Roam.requestLocationServices(this);
        } else {
            startTracking();
        }
    }

    private void startTracking() {

        //mock location
        Roam.allowMockLocation(true);

        //foreground notification
        Roam.setForegroundNotification(true,"Roam Rapid Deploy App","Click here to redirect the app",
                R.drawable.ic_launcher_background,"com.roam.mqttconnector.ui.MapActivity", "com.roam.mqttconnector.services.ImplicitService");

        //stationary location update
        Roam.updateLocationWhenStationary(5);


        //start tracking
                Roam.startTracking(RoamTrackingMode.ACTIVE, new TrackingCallback() {

                    @Override
                    public void onSuccess(String s) {
                        showMsg("Tracking started");
                    }

                    @Override
                    public void onError(RoamError roamError) {
                        showMsg(roamError.getMessage());
                    }
                });
    }

    private void stopTracking() {
        mMap.clear();
        Roam.stopTracking(new TrackingCallback() {
            @Override
            public void onSuccess(String s) {
                showMsg("Tracking stopped");
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }

            @Override
            public void onError(RoamError roamError) {
                showMsg(roamError.getMessage());
            }
        });
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public class CurrentLocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (intent != null && intent.getAction().equals(Constants.CURRENT_LOCATION_INTENT)) {
                    Location location = intent.getExtras().getParcelable(Constants.ROAM_LOCATION_INTENT);
                    //Log.d("TAG", "onReceive: LAT: " + location.getLatitude() + " LON: " + location.getLongitude());
                    updateCurrentLocationMarker(location);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Roam.REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tracking();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showMsg("Location permission required");
                }
                break;
            case Roam.REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tracking();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showMsg("Background Location permission required");
                }
                break;

            case Roam.REQUEST_CODE_ACTIVITY_RECOGNITION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tracking();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // showMsg("Activity permission required");
                    startTracking();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Roam.REQUEST_CODE_LOCATION_ENABLED) {
            tracking();
        }
    }

}