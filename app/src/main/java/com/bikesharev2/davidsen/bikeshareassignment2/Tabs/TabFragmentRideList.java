package com.bikesharev2.davidsen.bikeshareassignment2.Tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bikesharev2.davidsen.bikeshareassignment2.Bike;
import com.bikesharev2.davidsen.bikeshareassignment2.BikeUser;
import com.bikesharev2.davidsen.bikeshareassignment2.R;
import com.bikesharev2.davidsen.bikeshareassignment2.Ride;
import com.bikesharev2.davidsen.bikeshareassignment2.RidesDB;

import java.text.DateFormat;
import java.util.Locale;

import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;

// Fragment where the Ride list in check bike is created
public class TabFragmentRideList extends Fragment {

    private RecyclerView mRecyclerView;
    private RidesDB sRidesDB;
    private RideAdapter mRideAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sRidesDB = RidesDB.get(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_two, container, false);

        mRecyclerView = v.findViewById(R.id.recycler_ride);

        // Getting the relevant rides sorted by start date, descending order
        RealmResults<Ride> rides = sRidesDB.getRidesSorted();
        // setting the ride adapter
        mRideAdapter = new RideAdapter(rides);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mRideAdapter);

        return v;
    }

    // Holder for recycler view
    public static class RideHolder extends RecyclerView.ViewHolder{
        // Text fields for the table information
        TextView mRideName, mStartLocation, mEndLocation, mEndTime, mStartTime, mRidePrice;

        public Ride data;

        public RideHolder(View itemView) {
            super(itemView);

            // Wiring the text fields
            mRideName = itemView.findViewById(R.id.list_name_ride);
            mStartLocation = itemView.findViewById(R.id.list_location_start_ride);
            mEndLocation = itemView.findViewById(R.id.list_location_end_ride);
            mStartTime = itemView.findViewById(R.id.list_ride_start_time);
            mEndTime = itemView.findViewById(R.id.list_ride_end_time);
            mRidePrice = itemView.findViewById(R.id.list_ride_price);
        }
    }

    // Using the RealmRecycler view
    // Hope that it counts as a recycler view
    public static class RideAdapter extends RealmRecyclerViewAdapter<Ride, RideHolder>{

        public RideAdapter(RealmResults<Ride> rides){
            super(rides, true);
        }

        @NonNull
        @Override
        public RideHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RideHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_rides_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RideHolder holder, int position) {
            // Getting the ride at position
            final Ride ride = getItem(position);
            holder.data = ride;

            // Setting the text fields of the table
            holder.mRideName.setText(ride.getBikeName());
            holder.mStartLocation.setText(ride.getStartLocationName());
            holder.mEndLocation.setText(ride.getEndLocationName());
            String t = String.format(Locale.getDefault() ,"DKK %.2f", ride.getRidePrice());
            holder.mRidePrice.setText(t);
            holder.mStartTime.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(ride.getStartDate()));
            if (ride.getEndDate() != null){
                holder.mEndTime.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(ride.getEndDate()));
            } else {
                holder.mEndTime.setText("Still Active");
            }
        }

    }
}
