package com.bikesharev2.davidsen.bikeshareassignment2.Tabs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bikesharev2.davidsen.bikeshareassignment2.BikeUser;
import com.bikesharev2.davidsen.bikeshareassignment2.R;
import com.bikesharev2.davidsen.bikeshareassignment2.Ride;
import com.bikesharev2.davidsen.bikeshareassignment2.RidesDB;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

// Fragment where the Payment list in check bike is created
public class TabFragmentPaymentList extends Fragment {

    private RecyclerView mRecyclerView;
    private RidesDB sRidesDB;
    private PaymentAdapter mPaymentAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sRidesDB = RidesDB.get(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_three, container, false);

        mRecyclerView = v.findViewById(R.id.recycler_payment);

        // Getting the relevant rides sorted by start date, descending order
        RealmResults<Ride> rides = sRidesDB.getRidesSorted();
        // setting the ride adapter
        mPaymentAdapter = new PaymentAdapter(rides);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mPaymentAdapter);

        return v;

    }

    // Holder for recycler view
    public static class PaymentHolder extends RecyclerView.ViewHolder{
        // Text fields for the table information
        TextView mPaymentBikeName, mDuration, mPrice;

        public Ride data;

        public PaymentHolder(View itemView){
            super(itemView);

            // Wiring the text fields
            mPaymentBikeName = itemView.findViewById(R.id.list_name_payment);
            mDuration = itemView.findViewById(R.id.list_duration_payment);
            mPrice = itemView.findViewById(R.id.list_price_payment);
        }
    }

    // Using the RealmRecycler view
    // Hope that it counts as a recycler view
    public static class PaymentAdapter extends RealmRecyclerViewAdapter<Ride, PaymentHolder>{

        public PaymentAdapter(RealmResults<Ride> rides){super(rides,true);}

        @NonNull
        @Override
        public PaymentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PaymentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_payments_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PaymentHolder holder, int position) {
            // Getting the ride at position
            final Ride ride = getItem(position);
            holder.data = ride;

            // Setting the text fields of the table
            holder.mPaymentBikeName.setText(ride.getBikeName());
            String t = String.format(Locale.getDefault() ,"DKK %.2f", ride.getRidePrice());
            holder.mPrice.setText(t);
            if (ride.getDuration() != null){
                holder.mDuration.setTypeface(null, Typeface.NORMAL);
                holder.mDuration.setText(ride.getDuration());
            } else{
                holder.mDuration.setText("Still Active");
            }

        }
    }

}