package com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.bikesharev2.davidsen.bikeshareassignment2.MainActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// Used to find the devices current location and
// tried to find the address of the location
public class LocationFinder {

    // Method for getting the current location
    public static Location getCurrentLocation(LocationManager mLocationManager, Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(new Criteria(), true));
    }

    // tries to get the address based on current location
    public static String geocoder(Activity activity,double lat, double lon){
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

        String address = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0){
                address = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.d("geocode", "Could not find address");
        }

        return address;
    }

}
