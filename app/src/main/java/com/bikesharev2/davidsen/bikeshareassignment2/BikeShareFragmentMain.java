package com.bikesharev2.davidsen.bikeshareassignment2;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncUser;

// The main view fragment
public class BikeShareFragmentMain extends Fragment {
    // The buttons for the main view fragment
    private Button mRegisterBikeBtn, mFindBikeBtn, mEndRideBtn, mLogoutBtn, mCheckBikeBtn;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private RidesDB mRidesDB;
    private MenuItem mTokenks;
    RealmResults<BikeUser> mBikeUsers;
    private ConstraintLayout mConstraintLayout;
    private ProgressBar mProgressBar;
    // Used when user revokes permissions to prevent crash
    private boolean mPerDenied;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mRidesDB = RidesDB.get(getActivity());
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        // Subscribes to the relevant data
        subToData();

        // Shows the action bar
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        // Wiring the constraint layout and progress bar
        mConstraintLayout = v.findViewById(R.id.main_layout);
        mProgressBar = v.findViewById(R.id.progressBar_main);

        // This is run every time a new user is created
        Bundle args = getArguments();
        if (args != null && args.getInt("index") == 555){
            // Subscribes to the relevant data
            // run into some weird problems every time i try to change this with
            // synchronization to the cloud database :|
            subToData();
            //creates the new user
            BikeUser user = new BikeUser();
            // gets and sets the id of the new user
            user.setUserId(SyncUser.current().getIdentity());
            // every new user is given 100 account balance
            user.setTokens(100);
            // Adds the user to the database
            mRidesDB.addUser(user);
            // removes the key from the bundle
            args.remove("index");
        }

        // wiring end ride button and setting a listener
        mEndRideBtn = v.findViewById(R.id.end_ride);
        mEndRideBtn.setOnClickListener(v12 -> ((MainActivity) getActivity()).setUpFragment(new BikeShareFragmentEndRide(), R.id.fragment_container));

        // Wiring register bike button and setting a listener
        mRegisterBikeBtn = v.findViewById(R.id.register_bike);
        mRegisterBikeBtn.setOnClickListener(v13 -> ((MainActivity) getActivity()).setUpFragment(new BikeShareFragmentRegister(), R.id.fragment_container));

        // Wiring find bike button and setting a listener
        mFindBikeBtn = v.findViewById(R.id.find_bike);
        mFindBikeBtn.setOnClickListener(v14 -> ((MainActivity) getActivity()).setUpFragment(new BikeShareFragmentFindBike(), R.id.fragment_container));

        // Wiring logout button and setting a listener
        mLogoutBtn = v.findViewById(R.id.logout_btn);
        mLogoutBtn.setOnClickListener(v1 -> logoutUser());

        // Wiring check Bike button and setting a listener
        mCheckBikeBtn = v.findViewById(R.id.check_bike);
        mCheckBikeBtn.setOnClickListener((v1 -> ((MainActivity) getActivity()).setUpFragment(new BikeShareFragmentCheckBike(), R.id.fragment_container)));

        // Show the progress bar while database is syncing
        showProgress(true);

        // Sets the that permissions are given to true
        mPerDenied = true;

        // Checks if the location permissions are still valid
        // if not it disables the some features to prevent app crash
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PictureUtils.toaster(getContext(), "Please, enable location permissions and refresh app, some features are disabled");
            mRegisterBikeBtn.setEnabled(false);
            mFindBikeBtn.setEnabled(false);
            mEndRideBtn.setEnabled(false);
            mCheckBikeBtn.setEnabled(false);
            mPerDenied = false;
            showProgress(false);
        }
        return v;
    }

    // Method for showing or hiding the progress bar
    // Used when the database when a new use is created
    private void showProgress(final boolean show){
        if (show) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mConstraintLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        mConstraintLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mConstraintLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    // Subscribing to the relevant that in the Realm database
    // This should not be in this class but i run into som weird
    // synchronization problems every time that i try to move it some were else
    private void subToData() {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.where(BikeUser.class).equalTo("mUserId", SyncUser.current().getIdentity()).findAllAsync();
            realm.where(Bike.class).findAllAsync();
            realm.where(Ride.class).equalTo("mUserId", SyncUser.current().getIdentity()).findAllAsync();
        }
    }

    // Method for getting the user tokens and adds a change listener to keep them
    // update to date
    private void getUserTokens(){
        mBikeUsers = mRidesDB.subToUser();
        mBikeUsers.addChangeListener(bikeUsers -> {
            showProgress(false);
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            for (BikeUser users : bikeUsers){
                String temp = String.format(Locale.ENGLISH, "%.2f DKK", (users.getTokens()));
                mTokenks.setTitle(temp);
            }
        });
    }

    // Method for login out a user
    protected void logoutUser(){
        SyncUser syncUser = SyncUser.current();
        if (syncUser != null){
            syncUser.logOut();
            // Clears the back stack an recreates the app
            // this seem like the simplest solution not entirely sure
            // that it is the right one
            ((MainActivity) getActivity()).clearBackStack(1);
            getActivity().recreate();
        }
    }

    @Override
    public void onStop() {
        // Removes the change listener from bike user when
        // it is not null
        if(mBikeUsers != null){
            mBikeUsers.removeAllChangeListeners();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // Removes the change listener from bike user when
        // it is not null
        if (mBikeUsers != null){
            mBikeUsers.removeAllChangeListeners();
        }
        mRidesDB.CloseRealm();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        // updates the realm instance
        mRidesDB.updateRealm();
        super.onStart();
    }

    @Override
    public void onResume() {
        // Checks to see if the there is connection to the internet
        // Probably better to have added a listener, up did not due to time
        // constraints
        if(!((MainActivity)getActivity()).isNetworkAvailable()){
            PictureUtils.toaster(getContext(),
                    "Seems like there is no internet connection");
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items, menu);
        // Creates the options menu if the permissions are given
        if (mPerDenied){
            mTokenks = menu.findItem(R.id.tokens);
            getUserTokens();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                mRidesDB.addTokens(100);
        }
        return super.onOptionsItemSelected(item);
    }


}
