package com.bikesharev2.davidsen.bikeshareassignment2.Tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bikesharev2.davidsen.bikeshareassignment2.Bike;
import com.bikesharev2.davidsen.bikeshareassignment2.BikeUser;
import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;
import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PopupDelete;
import com.bikesharev2.davidsen.bikeshareassignment2.R;
import com.bikesharev2.davidsen.bikeshareassignment2.RidesDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;

// Fragment where the bike list in check bike is created
public class TabFragmentBikeList extends Fragment {

    private RecyclerView mRecyclerView;
    private BikeAdapter mAdapter;
    private ConstraintLayout mConstraintLayout;
    private RidesDB sRidesDB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sRidesDB = RidesDB.get(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_one, container, false);

        mRecyclerView = v.findViewById(R.id.recycler_bike);
        mConstraintLayout = v.findViewById(R.id.bike_list_con);

        // getting the user
        BikeUser user = sRidesDB.getUser();

        // getting the users RealmList of bikes

        if (user != null){
            RealmList<Bike> bikes = user.getBikes();
            mAdapter = new BikeAdapter(bikes);

            // Setting layout manager for the recycler view and setting the adapter
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mAdapter);
        }

        return v;
    }

    // Holder for the recycler view
    public class BikeHolder extends RecyclerView.ViewHolder{
        TextView mBikeName, mBikeLocation, mBikePrice, mBikeStatus;
        public Bike data;
        private RidesDB mRidesDB;

        public BikeHolder(View itemView){
            super(itemView);
            mRidesDB = RidesDB.get(itemView.getContext());

            // Wiring the text fields
            mBikeName = itemView.findViewById(R.id.list_bike_name);
            mBikeLocation = itemView.findViewById(R.id.list_bike_loc);
            mBikePrice = itemView.findViewById(R.id.list_bike_price);
            mBikeStatus = itemView.findViewById(R.id.list_bike_status);
        }

        // adding item click listener
        public void bind(final Bike bike){
            itemView.setOnClickListener(v -> {
                // Making the user unable to delete booked bikes
                if (bike.isActive()){
                    PictureUtils.toaster(itemView.getContext(), "Bike "+bike.getBikeName()+" is currently active, unable to delete");
                } else {
                    PopupDelete.setPopupWindow(bike, itemView.getContext(), mConstraintLayout, mRidesDB);
                }
            });
        }

    }

    // Using the RealmRecycler view
    // Hope that it counts as a recycler view
    public class BikeAdapter extends RealmRecyclerViewAdapter<Bike, BikeHolder>{
        public BikeAdapter(RealmList<Bike> bikes){
            super(bikes, true);
        }

        @NonNull
        @Override
        public BikeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BikeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_bikes_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BikeHolder holder, int position) {
            // Getting the bike at position
            final Bike bike = getItem(position);
            holder.data = bike;
            holder.bind(bike);

            // Setting the text fields of the table
            holder.mBikeName.setText(bike.getBikeName());
            holder.mBikeLocation.setText(bike.getBikeLocation());
            String t = String.format(Locale.getDefault() ,"DKK %.2f", bike.getRentPrice());
            holder.mBikePrice.setText(t);
            holder.mBikeStatus.setText(bike.isActive()? "Booked":"Free");
        }
    }
}

