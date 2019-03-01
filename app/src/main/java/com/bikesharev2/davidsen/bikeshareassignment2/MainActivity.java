package com.bikesharev2.davidsen.bikeshareassignment2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.MyService;
import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;

// Main Activity used to as a fragment container for all
// for all the layouts
public class MainActivity extends AppCompatActivity {
    private FragmentManager fm;
    private static final String STATE_COUNTER = "counter";

    // used to know which setup to run
    // meaning if the logout button is pressed or not
    private int mCounter = 1;

    // Used for the permission requests
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final String[] PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting support manger
        fm = getSupportFragmentManager();

        // checking the savedInstance for the
        // counter to see if the logout button is pressed
        // (Last minute solution)
        if (savedInstanceState != null){
            mCounter = savedInstanceState.getInt(STATE_COUNTER);
            savedInstanceState.clear();
        }

        // seeing what setup to run
        if (mCounter == 1) {
            mCounter = 0;
            if (SyncUser.current() != null) {
                Realm.setDefaultConfiguration(SyncConfiguration.automatic(SyncUser.current()));
                setUpFragment(new BikeShareFragmentMain(), R.id.fragment_container);
            } else {
                setUpFragment(new BikeShareFragmentLogin(), R.id.fragment_container);
            }
        }

        // running request permission if build version is
        // higher than M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions();
        }

        // starting myService, work around because off realm.
        // if the user kills the app, the onDestroy is not called.
        // So, it closest the realm instance
        // Last Minute change does not seem like i need it :(
        //startService(new Intent(getBaseContext(), MyService.class));
    }

    // Clearing the fragment back stack.
    // Called when successful login or logout button
    // or pressed.
    protected void clearBackStack(int id){
        // Setting the counter to one if logout button is pressed
        if (id == 1){
            mCounter = 1;
        }
        // clearing fragment back stack
        if (fm.getBackStackEntryCount() > 0){
            for (int i = 0; i < fm.getBackStackEntryCount(); i++){
                fm.popBackStack();
            }
        }
    }

    // Copied from the bluetooth test given at week 9
    // prompting the user for permissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions(){

        boolean hasPermissions = true;
        for (String permission : PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                hasPermissions = false;
            }
        }

        if (hasPermissions){
            Log.d("BikeShare", "Permissions granted");
        } else {
            boolean shouldShowRequest = true;
            for (String permissions : PERMISSIONS){
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions)){
                    shouldShowRequest = false;
                }
            }

            if (shouldShowRequest) {
                new AlertDialog.Builder(this)
                        .setTitle("Location")
                        .setMessage("You need to give Location permissions")
                        .setPositiveButton("OK", (dialog, which) ->
                                MainActivity.this.requestPermissions(PERMISSIONS, REQUEST_CODE_LOCATION_PERMISSION))
                        .setNegativeButton("CANCEL", ((dialog, which) ->
                            dialog.dismiss()))
                        .create()
                        .show();
            } else {
                requestPermissions(PERMISSIONS, REQUEST_CODE_LOCATION_PERMISSION);
            }
        }
    }

    // Used to setting up the different fragments
    protected void setUpFragment(Fragment newFragment, int res) {
        Fragment fragment = fm.findFragmentById(res);
        if (fragment == null){
            fragment = newFragment;
            fm.beginTransaction()
                    .add(res, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            fm.beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Closest the app if on more fragments are
        // in the back stack
        if (fm.getBackStackEntryCount() == 0){
            finish();
        }
    }

    // Most of the this is copied from bluetooth test
    // Used for the results for the permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    PictureUtils.toaster(this, "Permission granted");
                } else {
                    PictureUtils.toaster(this, "Location permission denied");

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Please enable permission from settings", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("OK", view -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        MainActivity.this.startActivityForResult(intent, 501);
                    });
                    snackbar.show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Used to see if the device as any active internet connection.
    // Disables some the login button if it is not or tells the user
    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Saves the counter state to see which setup to run
        outState.putInt(STATE_COUNTER, mCounter);
    }
}
