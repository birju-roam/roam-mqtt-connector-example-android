package com.roam.mqttconnector.services;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;


import com.roam.mqttconnector.util.Constants;
import com.roam.sdk.models.RoamError;
import com.roam.sdk.models.RoamLocation;
import com.roam.sdk.models.RoamLocationReceived;
import com.roam.sdk.models.events.RoamEvent;
import com.roam.sdk.service.RoamReceiver;

import java.util.List;

public class LocationReceiver extends RoamReceiver {

    @Override
    public void onLocationUpdated(Context context, List<RoamLocation> list) {
        super.onLocationUpdated(context, list);
        Log.e("TAG", "Location: "+"Lat: "+list.get(0).getLocation().getLatitude() +" Lng: "+list.get(0).getLocation().getLongitude());
        Toast.makeText(context, "Location: "+"Lat: "+list.get(0).getLocation().getLatitude() +" Lng: "+list.get(0).getLocation().getLongitude(), Toast.LENGTH_SHORT).show();
        broadcastLocation(context,list.get(0).getLocation());
    }


    @Override
    public void onError(Context context, RoamError roamError) {
        Log.e("onError: ", roamError.getMessage());
       // Toast.makeText(context, "onError: "+roamError.getMessage(), Toast.LENGTH_SHORT).show();

    }

    private void broadcastLocation(Context context, Location location) {
        try {
            Intent intent = new Intent();
            intent.setAction(Constants.CURRENT_LOCATION_INTENT);
            intent.putExtra(Constants.ROAM_LOCATION_INTENT, location);
            context.sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

