package com.bikesharev2.davidsen.bikeshareassignment2;

import android.content.res.Resources;
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

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;

import java.util.Date;
import java.util.Locale;

import io.realm.SyncUser;

// Class for the start ride fragment
public class BikeShareFragmentAddRide extends Fragment {
    private RidesDB mRidesDB;

    private TextView mBikeName, mStartLoc;
    private ImageView mImageView;
    private Button mAddRideBtn;
    private Spinner mPriceSpinner;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    private float mRentPrice;

    private Bike mBike;
    private BikeUser mBikeUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRidesDB = RidesDB.get(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start_ride, container, false);

        // Wiring the relevant button and text fields and image view
        mBikeName = v.findViewById(R.id.add_ride_bike_name);
        mStartLoc = v.findViewById(R.id.ride_start_location);
        mImageView = v.findViewById(R.id.bike_image_start);
        mPriceSpinner = v.findViewById(R.id.price_spinner);
        mAddRideBtn = v.findViewById(R.id.add_ride_btn);

        // Gets the arguments passed from find bike about
        // to get the id of the bike selected
        String args = getArguments().getString("id");
        if (args != null){
            mBike = mRidesDB.getBike(args);
            mBikeUser = mRidesDB.getUser();
        }

        // Setup spinner to calculate an estimated cost for the ride
        Resources res = getResources();
        mSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, res.getStringArray(R.array.rent_price));
        mPriceSpinner.setAdapter(mSpinnerAdapter);

        // Add and item select listener to the spinner
        mPriceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] temp = mSpinnerAdapter.getItem(position).toString().split(" ");
                updateRentPrice(temp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set the text for the bike name and bike location and image of the bike
        mBikeName.setText(mBike.getBikeName());
        mStartLoc.setText(mBike.getBikeLocation());
        mImageView.setImageBitmap(PictureUtils.convertByteArrayToBitmap(mBike.getBikeImage()));

        // Add click listener to add ride button
        mAddRideBtn.setOnClickListener(v1 -> {
            String startLoc = mStartLoc.getText().toString().trim();
            // Check if start locations is set
            if (checkFields(startLoc)){
                // Create a new Ride object to add to the database
                Ride ride = new Ride();
                // setting the relevant fields of the ride object
                ride.setUserId(SyncUser.current().getIdentity());
                ride.setStartLocationName(startLoc);
                ride.setStartDate(new Date());
                ride.setUserId(mBikeUser.getUserId());
                ride.setBikeId(mBike.getId());
                ride.setLatStart(mBike.getLat());
                ride.setLonStart(mBike.getLon());
                ride.setBikeName(mBike.getBikeName());
                ride.setActive(true);

                // Creating a new bike object, used to update bike in the database
                Bike bike = new Bike();
                bike.setId(mBike.getId());
                bike.setActive(true);
                bike.setLat(mBike.getLat());
                bike.setLon(mBike.getLon());

                // Update the bike and add the ride to the database
                mRidesDB.updateBike(bike);
                mRidesDB.startRide(ride);

                // tell the use that the ride has started and pop the stack
                // bringing the program back to find bike fragment
                PictureUtils.toaster(getContext(), "Selected Ride Started");
                getFragmentManager().popBackStack();
            }
        });

        return v;
    }

    // Method for checking if field is filled
    private boolean checkFields(String startLoc){
        boolean fieldNotEmpty = true;
        if (startLoc.length() == 0){
            fieldNotEmpty = false;
            mStartLoc.setError("Please give a start location");
            mStartLoc.requestFocus();
        }
        return fieldNotEmpty;
    }

    // Method used to calculate and estimate of the ride price
    // based on some pre-defined times.
    private void updateRentPrice(String[] strings){
        int time = Integer.parseInt(strings[0]);
        switch (strings[1]){
            case "minutes":
                mRentPrice = (float) (mBike.getRentPrice()/2);
                break;
            case "hours":
                switch (time){
                    case 2:
                        mRentPrice = (float) (mBike.getRentPrice()*2);
                        break;
                    case 6:
                        mRentPrice = (float) (mBike.getRentPrice()*6);
                        break;
                    case 12:
                        mRentPrice = (float) (mBike.getRentPrice()*12);
                        break;
                }
                break;
            case "day":
                mRentPrice = (float) (mBike.getRentPrice()*24);
                break;
            case "days":
                PictureUtils.toaster(getContext(), Integer.toString(time));
                break;
        }
        String price = String.format(Locale.ENGLISH, "Rent now (exp. Price %.2f)", mRentPrice);
        mAddRideBtn.setText(price);
    }
}
