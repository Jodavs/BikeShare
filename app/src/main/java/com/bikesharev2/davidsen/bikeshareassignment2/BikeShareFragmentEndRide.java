package com.bikesharev2.davidsen.bikeshareassignment2;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.LocationFinder;
import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

// Class for the end ride fragment
public class BikeShareFragmentEndRide extends Fragment {
    private Button mEndRideBtn;
    private Spinner mActiveRidesSpinner;
    private ImageView mImageView;
    private TextView mEndRideLoc;
    private ArrayAdapter<String> mSpinnerAdapter;
    private List<String> mBikeName;
    private ArrayList<Ride> mActiveRides;
    private String mBikeId;
    private int mSpinnerPos;
    private long mTimeDiff;
    private long mDaysDiff;
    private String mAddress;

    private BikeUser mBikeUser;
    private Ride mRide;

    private RidesDB sRidesDB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sRidesDB = RidesDB.get(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_end_ride, container, false);

        mEndRideBtn = v.findViewById(R.id.end_ride_btn);

        // Gets the devices current location
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = LocationFinder.getCurrentLocation(manager, getContext());

        // If Location permissions have been revoked disable the end ride button
        if (location == null){
            PictureUtils.toaster(getContext(), "Please, give permissions");
            mEndRideBtn.setEnabled(false);
        } else {
            // Tries to get the address based on the current location
            String address = LocationFinder.geocoder(getActivity(), location.getLatitude(), location.getLongitude());
            if (address.length() != 0){
                mAddress = LocationFinder.geocoder(getActivity(),location.getLatitude(), location.getLongitude());
            }
        }

        mActiveRidesSpinner = v.findViewById(R.id.end_ride_spinner);
        mImageView = v.findViewById(R.id.end_ride_image);
        mEndRideLoc = v.findViewById(R.id.end_ride_loc);

        // Lists for the name of the bikes and for the Rides
        mBikeName = new ArrayList<>();
        mActiveRides = new ArrayList<>();

        // Adding all the active rides from the database to the ride list
        mActiveRides.addAll(sRidesDB.getActiveRides());

        // Gets the names of all the active rides and adds them to the bike name list
        mBikeUser = sRidesDB.getUser();
        for(Ride ride : mActiveRides){
            mBikeName.add(ride.getBikeName());
        }

        // Sets up the spinner adapter
        mSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mBikeName);
        mActiveRidesSpinner.setAdapter(mSpinnerAdapter);

        // Adds an item click listener to the spinner
        mActiveRidesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // if an item is selected get the bike at that position in the active rides list
                // and sets the bike picture and current end address if found
                mSpinnerPos = position;
                Ride ride = mActiveRides.get(mSpinnerPos);
                mBikeId = ride.getBikeId();
                mImageView.setImageBitmap(PictureUtils.convertByteArrayToBitmap(sRidesDB.getBike(ride.getBikeId()).getBikeImage()));
                if (mAddress.length() != 0){
                    mEndRideLoc.setText(mAddress);
                }
                mRide = ride;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // tell the user that there are no more active rides
                PictureUtils.toaster(getContext(), "No More Active Rides");
            }
        });

        // Adding a click listener to end ride button
        mEndRideBtn.setOnClickListener((view) -> {
            if (mRide == null){
                PictureUtils.toaster(getContext(), "There are no active Rides");
                return;
            }
            // Gets the relevant data
            String endRideLoc = mEndRideLoc.getText().toString();
            Date date = new Date();
            String duration = duration(mRide.getStartDate(), date);
            double ridePrice = ridePrice();
            double tokens = sRidesDB.getUser().getTokens();
            // checks if the user has filled the end ride location
            // and if he has enough tokens to pay for the ride
            if(checkFields(endRideLoc, tokens, ridePrice)){
                // Creates a new ride object with the id of and existing one
                Ride ride = new Ride(mRide.getRideId());
                // sets all the relevant fields of the ride object
                ride.setActive(false);
                ride.setLatEnd(location.getLatitude());
                ride.setLonEnd(location.getLongitude());
                ride.setEndLocationName(endRideLoc);
                ride.setEndDate(date);
                ride.setDuration(duration);
                ride.setRidePrice(ridePrice);

                // Creates a new Bike object if the id of an existing one
                Bike bike = new Bike(mBikeId);
                // Sets the relevant fields
                bike.setActive(false);
                bike.setLat(location.getLatitude());
                bike.setLon(location.getLongitude());

                // Creates a new Bike User object and subtracts the tokens from the ride price
                BikeUser user = new BikeUser(ride.getUserId());
                user.setTokens(tokens - ridePrice);

                // Updates the bike user, bike and ride in the database
                sRidesDB.updateUser(user);
                sRidesDB.updateBike(bike);
                sRidesDB.endRide(ride);

                // sets end location text to empty
                mEndRideLoc.setText("");

                // Removes bike name from the bike name list
                mBikeName.remove(mSpinnerPos);
                // Removes the ride from the active rides list
                mActiveRides.remove(mSpinnerPos);

                // Workaround because the onItemSelect in the spinner listener is not called
                // If it was item at position 0 that was removed from the list
                if (mSpinnerPos == 0 && !mActiveRides.isEmpty()){
                    Ride ride1 = mActiveRides.get(mSpinnerPos);
                    mBikeId = ride1.getBikeId();
                    mImageView.setImageBitmap(PictureUtils.convertByteArrayToBitmap(sRidesDB.getBike(ride1.getBikeId()).getBikeImage()));
                    mRide = ride1;
                    if (mAddress.length() != 0){
                        mEndRideLoc.setText(mAddress);
                    }
                    mSpinnerAdapter.notifyDataSetChanged();
                    PictureUtils.toaster(getContext(), "Ride Ended");
                } else {
                    PictureUtils.toaster(getContext(), "Ride Ended");
                    mSpinnerAdapter.notifyDataSetChanged();
                }
                if (mActiveRides.isEmpty()){
                    mImageView.setImageDrawable(null);
                }
            }
        });

        return v;
    }

    // Calculates the duration of the ride
    private String duration(Date start, Date end){
        String duration;
        mTimeDiff = end.getTime() - start.getTime();

        mDaysDiff = mTimeDiff / (24 * 60 * 60 * 1000);

        start.setTime(mTimeDiff);
        Date d = new Date();

        d.setTime(mTimeDiff);
        if (mDaysDiff == 0){
            duration = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMAN).format(d);
        } else {
            duration = String.format(Locale.getDefault(), "%d Days%n", mDaysDiff);
            duration = duration+DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMAN).format(d);
        }

        return duration;
    }

    // Calculates the price of the ride
    private double ridePrice(){
        double price = sRidesDB.getBike(mBikeId).getRentPrice()/60;

        long diffSec = mTimeDiff / 1000 % 60;
        long diffMin = mTimeDiff / (60 * 1000) % 60;
        long diffHours = mTimeDiff / (60 * 60 * 1000) % 24;

        if (diffSec >= 30) diffMin += 1;
        long minutes = diffMin+(diffHours*60)+(mDaysDiff*24*60);

        return Math.round(price*minutes);
    }

    // Method for checking if all the relevant data are set or not empty
    private boolean checkFields(String endLoc, double tokens, double ridePrice){
        boolean fieldNotEmpty = true;
        if (endLoc.length() == 0){
            fieldNotEmpty = false;
            mEndRideLoc.setError("Please give a end location");
            mEndRideLoc.requestFocus();
        }
        if (mActiveRides.isEmpty()){
            fieldNotEmpty = false;
        }
        if (tokens < ridePrice){
            fieldNotEmpty = false;
            PictureUtils.toaster(getContext(), "You don't have enough tokens");
        }
        return fieldNotEmpty;
    }
}
