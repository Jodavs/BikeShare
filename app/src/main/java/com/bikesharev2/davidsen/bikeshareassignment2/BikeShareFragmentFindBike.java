package com.bikesharev2.davidsen.bikeshareassignment2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

// Class for the find ride fragment
public class BikeShareFragmentFindBike extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    private Button mFindClosestBtn;
    private Location mLocation;
    private RidesDB sRidesDB;

    private static final int LOCATION_UPDATE_MIN_DISTSNCE = 1;
    private static final int LOCATION_UPDATE_MIN_TIME = 1000;

    RealmResults<Bike> bikes;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private List<Marker> mMarkers;
    private Map<String, Bike> mMarkerBikeMap;
    private Marker mCurrent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sRidesDB = RidesDB.get(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_find_ride, container, false);
        // New hashMap for the markers
        mMarkerBikeMap = new HashMap<>();
        // new ArrayList used to find the closest marker
        mMarkers = new ArrayList<>();

        mMapView = v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Hiding the action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        // Wiring find closest button and adding a click listener
        mFindClosestBtn = v.findViewById(R.id.find_closestBtn);
        mFindClosestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gets the closest marker from the current device location
                Marker marker = getClosestMarker();
                // If no markers are found tell the user
                if (marker == null) {
                    PictureUtils.toaster(getContext(), "There does not seem to be any free bikes");
                } else {
                    // Change the colour of the closest marker to blue and move camera to it
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                    marker.showInfoWindow();
                    PictureUtils.toaster(getContext(), "Click marker to rent bike");
                }

            }
        });

        // Adding listener for the location listener
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    drawMarker(location);
                    //mLocationManager.removeUpdates(mLocationListener);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Gets the map async
        mMapView.getMapAsync(this);
        return v;
    }

    // Method for getting the closest marker
    private Marker getClosestMarker() {
        LatLng currentPost = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        double closest = -1;
        Marker marker = null;

        for (Marker markers : mMarkers) {
            double checking = SphericalUtil.computeDistanceBetween(currentPost, markers.getPosition());
            if (checking < closest || closest == -1) {
                closest = checking;
                marker = markers;
            }
        }
        return closest == -1 ? null : marker;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Setting up some settings for google map
        mGoogleMap.setIndoorEnabled(true);
        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        getCurrentLocation();

        // Check if permissions have been revoked and return if they have
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PictureUtils.toaster(getContext(), "Please, enable location permissions and refresh app, some features are disabled");
            return;
        }

        // Enables google's location
        mGoogleMap.setMyLocationEnabled(true);

        // Gets all the vacant bikes from the database
        // and add a change listener to them
        bikes = sRidesDB.getVacantBikes();
        bikes.addChangeListener(bikes -> {
            // Clearing and redrawing the markers up every change :(
            int i = 0;
            mGoogleMap.clear();
            mCurrent = null;
            drawMarker(mLocation);
            mMarkers.clear();
            mMarkerBikeMap.clear();
            for (Bike bike : bikes) {
                LatLng latLng = new LatLng(bike.getLat(), bike.getLon());
                mMarkers.add(mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(bike.getBikeName())));
                mMarkerBikeMap.put(mMarkers.get(i++).getId(), bike);
            }

        });
        // Adding click listener to the markers
        mGoogleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Checking if it is the marker for your current location
        if (marker.getId().equals("m0") || marker.getId().equals(mCurrent.getId())) {
            return false;
        }
        // Creates a new bundle at passes the id of the bike for the marker as arguments
        // to the add ride fragment
        Bundle args = new Bundle();
        args.putString("id", mMarkerBikeMap.get(marker.getId()).getId());
        BikeShareFragmentAddRide frag = new BikeShareFragmentAddRide();
        frag.setArguments(args);
        ((MainActivity) getActivity()).setUpFragment(frag, R.id.fragment_container);
        return false;
    }

    // Method used for getting the current location
    private void getCurrentLocation() {
        // Checking which providers are enabled
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Checking if the permissions have been revoked
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PictureUtils.toaster(getContext(), "Please, enable location permissions and refresh app, some features are disabled");
            return;
        }

        // If one or both of the providers are enabled request the location.
        mLocation = null;
        if (!(isGPSEnabled || isNetworkEnabled)) {
            PictureUtils.toaster(getContext(), "Error unable to get location");
        } else {
            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTSNCE, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTSNCE, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }

        // Draw a marker on the current location if not null
        if (mLocation != null){
            drawMarker(mLocation);
        }
    }

    // Method for drawing a current location marker on the map or moving it if already drawn
    private void drawMarker(Location location){
        if (mGoogleMap != null){
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            if (mCurrent != null){
                mCurrent.setPosition(gps);
                return;
            }
            mCurrent = mGoogleMap.addMarker(new MarkerOptions()
                    .position(gps)
                    .title("Current Position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hiding the action bar
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        mMapView.onResume();
        // Update the current location
        getCurrentLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        // Removing the location listener
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the bikes change listener if set
        if (bikes != null){
            bikes.removeAllChangeListeners();
        }
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
