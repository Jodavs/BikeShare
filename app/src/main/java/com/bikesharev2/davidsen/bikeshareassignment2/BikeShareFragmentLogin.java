package com.bikesharev2.davidsen.bikeshareassignment2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

import static com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.Constants.AUTH_URL;

// Class for the login fragment
public class BikeShareFragmentLogin extends Fragment {
    private Button mLoginBtn;
    private FloatingActionButton mAddUser;
    private TextView mUserNameNew, mPasswordCreate, mPasswordUser, mUserName;
    private ConstraintLayout mConstraintLayout;
    private ProgressBar mProgressBar;
    private RidesDB sRidesDB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sRidesDB = RidesDB.get(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        // Wiring the text fields and buttons and progress bar
        mUserName = v.findViewById(R.id.user_name);
        mPasswordUser = v.findViewById(R.id.user_pass);
        mLoginBtn = v.findViewById(R.id.login_btn);
        mConstraintLayout = v.findViewById(R.id.login_form);
        mProgressBar = v.findViewById(R.id.progressBar);

        // Checks if there is a valid internet connection if not the login button is disabled
        if (!((MainActivity)getActivity()).isNetworkAvailable()){
            PictureUtils.toaster(getContext(), "There does not seem to be any valid internet connection");
            mLoginBtn.setEnabled(false);
        }

        // Adding a click listener to the login button
        mLoginBtn.setOnClickListener(v1 -> attemptLogin(false));

        // Wiring the add user button and adding a listener to it
        mAddUser = v.findViewById(R.id.add_userbtn);
        mAddUser.setOnClickListener(view ->{
            // Creating a dialog view
            View dialogView = inflater.inflate(R.layout.create_user, null);
            mUserNameNew = dialogView.findViewById(R.id.create_user_name);
            mPasswordCreate = dialogView.findViewById(R.id.create_user_pass);
            new AlertDialog.Builder(getContext())
                    .setTitle("Create New User")
                    .setView(dialogView)
                    .setPositiveButton("Add", (dialog, which) -> attemptLogin(true))
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        });

        return v;
    }

    // Method for login in use or creating new user
    private void attemptLogin(boolean createUser){
        mUserName.setError(null);

        // Gets username and password for login or create new user
        String userName = createUser ? mUserNameNew.getText().toString() : mUserName.getText().toString();
        String pass = createUser ? mPasswordCreate.getText().toString() : mPasswordUser.getText().toString();

        // Checks if both the password and username are filled
        if (userName.length() == 0 || pass.length() == 0) {
            PictureUtils.toaster(getContext(), "Please fill both User name and password");
            return;
        }
        // Shows loading bar
        showProgress(true);

        // Checking login or create user credentials
        SyncCredentials credentials = SyncCredentials.usernamePassword(userName, pass, createUser);
        SyncUser.logInAsync(credentials, AUTH_URL, new SyncUser.Callback<SyncUser>() {
            @Override
            public void onSuccess(SyncUser result) {
                // Clears the back stack, so the the user is not take back to the login screen
                // on back press
                ((MainActivity) getActivity()).clearBackStack(0);
                if (createUser){
                    PictureUtils.toaster(getContext(), "New User Created");
                    setUpRealmAndGoToMainView(createUser, result);
                } else {
                    setUpRealmAndGoToMainView(false, result);
                }
            }

            @Override
            public void onError(ObjectServerError error) {
                // Hide loading bar
                showProgress(false);
                // Give user error message
                mUserName.setError("Something went wrong!, please try another username");
                mUserName.requestFocus();
                Log.e("Login error", error.toString());
            }
        });
    }

    // Method for showing or hiding the progress bar
    // Used while the program tries to login or create new user
    private void showProgress(final boolean show){
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

    // Setting up Realm an goes the main view
    private void setUpRealmAndGoToMainView(boolean creatNewUser, SyncUser user) {
        Realm.setDefaultConfiguration(SyncConfiguration.automatic(user));
        BikeShareFragmentMain main = new BikeShareFragmentMain();
        // If a new use is created the main fragments need to know to perform some actions
        if (creatNewUser) {
            Bundle args = new Bundle();
            args.putInt("index", 555);
            main.setArguments(args);
        }
        // Setting up the main view
        ((MainActivity) getActivity()).setUpFragment(main, R.id.fragment_container);
    }

}
