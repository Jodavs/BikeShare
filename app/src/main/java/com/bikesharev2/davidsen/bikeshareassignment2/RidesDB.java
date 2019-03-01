package com.bikesharev2.davidsen.bikeshareassignment2;

import android.content.Context;

import com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses.PictureUtils;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.SyncUser;

// Singleton class used to update the database
public class RidesDB{
    private static RidesDB sRidesDB;
    private Realm sRealm;

    private Context mContext;

    // Constructor for first time initialisation
    private RidesDB(Context context){
        sRealm = Realm.getDefaultInstance();
        mContext = context.getApplicationContext();
    }

    // Creates a new RidesDB if none exists
    // otherwise return the existing one
    public static RidesDB get(Context context){
        if (sRidesDB == null){
            sRidesDB = new RidesDB(context);
        }
        return sRidesDB;
    }

    // Returns all the vacant bikes from Realm
    // Used by find bike to draw the markers on the map
    public synchronized RealmResults<Bike> getVacantBikes(){
        RealmResults<Bike> bikes = sRealm.where(Bike.class)
                .equalTo("mActive", false)
                .findAllAsync();
        return bikes;
    }

    // Returns all the active rides for the current user
    // Used by end ride to populate the active rides spinner
    public synchronized RealmResults<Ride> getActiveRides(){
        RealmResults<Ride> rides = sRealm.where(Ride.class)
                .equalTo("mUserId", SyncUser.current().getIdentity())
                .and()
                .equalTo("mIsActive", true)
                .findAll();
        return rides;
    }

    // Adds a new bike to the Realm database
    // This transaction is async only to test the difference.
    // It makes a toast upon success or error.
    public synchronized void registerBike(final Bike bike){
        sRealm.executeTransactionAsync(realm -> {
            realm.where(BikeUser.class).equalTo("mUserId", SyncUser.current()
                    .getIdentity())
                    .findFirst()
                    .getBikes().add(bike);
        }, () -> PictureUtils.toaster(mContext, "Bike "+bike.getBikeName()+" Registered"),
                error -> PictureUtils.toaster(mContext, "Something went wrong with the bike registration"));
    }

    // Updates the given bike of the Realm database
    public synchronized void updateBike(Bike bike){
        sRealm.executeTransaction(realm -> {
            Bike tempBike = realm.where(Bike.class).equalTo("mId", bike.getId()).findFirst();
            tempBike.setActive(bike.isActive());
            tempBike.setLat(bike.getLat());
            tempBike.setLon(bike.getLon());
            realm.insertOrUpdate(tempBike);
        });
    }

    // Fetches a bike from Realm and return it
    public synchronized Bike getBike(String id){
        return sRealm.where(Bike.class)
                .equalTo("mId", id)
                .findFirst();
    }

    // Fetches the current user from Realm and returns the results
    public synchronized RealmResults<BikeUser> subToUser(){
        return sRealm
                .where(BikeUser.class)
                .equalTo("mUserId", SyncUser.current().getIdentity())
                .findAllAsync();
    }

    // Fetches the current user from Realm and returns it
    public synchronized BikeUser getUser(){
        return sRealm.where(BikeUser.class).findFirst();
    }

    // Takes a ride and adds it to the current user in Realm
    public synchronized void startRide(Ride ride){
        sRealm.executeTransaction(realm -> {
            String user = SyncUser.current().getIdentity();
            realm.where(BikeUser.class)
                    .equalTo("mUserId", user)
                    .findFirst()
                    .getRides().add(ride);
        });
    }

    // Takes a ride and updates the ride of the current user in Realm
    public void endRide(Ride ride){
        sRealm.executeTransaction(realm -> {
            Ride tempRide = realm.where(Ride.class).equalTo("mRideId", ride.getRideId()).findFirst();
            tempRide.setActive(ride.isActive());
            tempRide.setLatEnd(ride.getLatEnd());
            tempRide.setLonEnd(ride.getLonEnd());
            tempRide.setEndLocationName(ride.getEndLocationName());
            tempRide.setEndDate(ride.getEndDate());
            tempRide.setDuration(ride.getDuration());
            tempRide.setRidePrice(ride.getRidePrice());

            realm.insertOrUpdate(tempRide);
        });
    }

    // Deletes the given bike from Realm
    public synchronized void deleteBike(final Bike bike){
        sRealm.executeTransaction(realm -> bike.deleteFromRealm());
    }

    // Fetches the take photo from local storage and returns the file
    public File getPhotoFile(Bike bike){
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, bike.getPhotoFilename());
    }

    // Takes the given bike user and updates Realm
    public synchronized void updateUser(BikeUser bikeUser){
        try(Realm realm = Realm.getDefaultInstance()){
            realm.executeTransaction(realm1 -> {
                BikeUser tempUser = getUser();
                tempUser.setTokens(bikeUser.getTokens());
                realm1.insertOrUpdate(tempUser);
            });
        }
    }

    // Adds a new user to Realm
    public synchronized void addUser(BikeUser user){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> realm.insertOrUpdate(user));
        } finally {
            realm.close();
        }
    }

    // Adds 100 tokens to the current user
    // For some reason if i set this to synchronized the tokens on
    // the cloud database will not sync
    public void addTokens(double tokens){
        sRealm.executeTransaction(realm -> {
            BikeUser bikeUser = realm.where(BikeUser.class).findFirst();
            bikeUser.setTokens(bikeUser.getTokens() + tokens);
            realm.insertOrUpdate(bikeUser);
        });
    }

    // Fetches the Rides sorted by start date from Realm
    public synchronized RealmResults<Ride> getRidesSorted(){
        RealmResults<Ride> rides = sRealm.where(Ride.class)
                .equalTo("mUserId", SyncUser.current().getIdentity())
                .sort("mStartDate", Sort.DESCENDING)
                .findAllAsync();
        return rides;
    }

    // Updates the realm instance
    public void updateRealm(){
        sRealm = Realm.getDefaultInstance();
    }

    // Closes the realm instance
    public void CloseRealm(){
        sRealm.close();
    }

}
