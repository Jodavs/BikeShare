package com.bikesharev2.davidsen.bikeshareassignment2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.LocationFinder;
import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;

import java.io.File;
import java.util.List;

// Class for register bike fragment
public class BikeShareFragmentRegister extends Fragment {
    private Button mRegisterBtn;
    private ImageView mBikeImage;
    private TextView mBikeName, mBikeType, mBikeRentPrice, mBikeStartLoc;
    private RidesDB sRidesDB;

    // Bitmap for the image and image file
    private Bitmap mBitmap;
    private File mImageFile;

    private static final int REQUEST_PHOTO = 0;

    // Is used to get the photo
    private Bike mBike = new Bike();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sRidesDB = RidesDB.get(getContext());
        mImageFile = RidesDB.get(getActivity()).getPhotoFile(mBike);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register_bike, container, false);

        // Wiring the text fields and buttons
        mRegisterBtn = v.findViewById(R.id.bike_register);
        mBikeName = v.findViewById(R.id.bike_name);
        mBikeType = v.findViewById(R.id.bike_type);
        mBikeRentPrice = v.findViewById(R.id.bike_rent_price);
        mBikeStartLoc = v.findViewById(R.id.bike_start_loc);

        // Boolean used to turn of the take image button
        boolean offImage = true;

        // Gets the current device location
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = LocationFinder.getCurrentLocation(manager, getContext());

        // If Location permissions have been revoked disable the register button
        // and take picture button
        if (location == null){
            PictureUtils.toaster(getContext(), "Please, give permissions");
            mRegisterBtn.setEnabled(false);
            offImage = false;
        } else {
            // Tries to get the address based on the current location
            String address = LocationFinder.geocoder(getActivity(), location.getLatitude(), location.getLongitude());
            if (address.length() != 0){
                mBikeStartLoc.setText(address);
            }
        }

        // Adding an click listener to the register button
        mRegisterBtn.setOnClickListener(v1 -> {
            // Getting the texts from the text fields
            String bikeName = mBikeName.getText().toString().trim();
            String bikeType = mBikeType.getText().toString().trim();
            String bikeLoc = mBikeStartLoc.getText().toString().trim();
            String bikeRentPrice = mBikeRentPrice.getText().toString().trim();

            // Checking if the image is take and all the text fields are filled
            if (checkFields(bikeName, bikeType, bikeRentPrice, bikeLoc, mBitmap)){
                // Creates an new bike to add to the database
                Bike bike = new Bike(mBike.getId());
                // Setting the relevant information for the new bike
                bike.setBikeName(bikeName);
                bike.setBikeType(bikeType);
                bike.setBikeLocation(bikeLoc);
                bike.setBikeImage(PictureUtils.convertBitmapToByteArray(mBitmap));

                bike.setLat(location.getLatitude());
                bike.setLon(location.getLongitude());
                bike.setRentPrice(Double.valueOf(bikeRentPrice));

                // Adding the bike to the database
                sRidesDB.registerBike(bike);

                // Resetting all the text fields
                mBikeName.setText("");
                mBikeType.setText("");
                mBikeRentPrice.setText("");

                // Creating a new empty bike
                // and updating the photo view
                mBike = new Bike();
                mImageFile = sRidesDB.getPhotoFile(bike);
                updatePhotoView();
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Checking if the phone can take photos and disables the imageView button if it cannot
        Boolean canTakePhoto = mImageFile != null && captureImage.resolveActivity(packageManager) != null;
        mBikeImage = v.findViewById(R.id.bike_image);
        mBikeImage.setEnabled(canTakePhoto && offImage);

        // Add a click listener for the image view
        mBikeImage.setOnClickListener(v12 -> {
            Uri uri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                    "com.bikesharev2.davidsen.bikeshareassignment2.fileprovider",
                    mImageFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo activity : cameraActivities){
                getActivity().grantUriPermission(activity.activityInfo.packageName,
                        uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            startActivityForResult(captureImage, REQUEST_PHOTO);
        });
        return v;
    }

    // Method used to check if all of the text field are filled
    // and returns false if not
    private boolean checkFields(String name, String type, String price,String startLoc, Bitmap bitmap){
        boolean fieldNotEmpty = true;
        if (name.length() == 0){
            fieldNotEmpty = false;
            mBikeName.setError("Please give a name");
            mBikeName.requestFocus();
        }
        if (type.length() == 0) {
            fieldNotEmpty = false;
            mBikeType.setError("Please give a type");
            mBikeType.requestFocus();
        }
        if (price.length() == 0){
            mBikeRentPrice.setError("Please set a rent price");
            mBikeRentPrice.requestFocus();
        }
        if (bitmap == null){
            fieldNotEmpty = false;
            PictureUtils.toaster(getContext(), "Please take a picture of the bike");
        }
        if (startLoc.length() == 0){
            fieldNotEmpty = false;
            mBikeStartLoc.setError("Please give a start location");
            mBikeStartLoc.requestFocus();
        }
        return fieldNotEmpty;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        // Gets the uri for the file
        if (requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getContext(), "com.bikesharev2.davidsen.bikeshareassignment2.fileprovider",
                    mImageFile);

            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    // Method use for updating the photo view
    // if an image exists it is set to that image otherwise set to null
    private void updatePhotoView(){
        if (mImageFile == null || !mImageFile.exists()){
            mBikeImage.setImageDrawable(null);
        } else {
            mBitmap = PictureUtils.getScaledBitmap(mImageFile.getPath(), mBikeImage.getWidth(), mBikeImage.getHeight());
            // Deleting the image after it has been scaled
            mImageFile.delete();
            mBikeImage.setImageBitmap(mBitmap);
        }
    }
}
