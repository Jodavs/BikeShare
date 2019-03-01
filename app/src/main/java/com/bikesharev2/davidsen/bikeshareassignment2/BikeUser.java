package com.bikesharev2.davidsen.bikeshareassignment2;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

// User object stored in Realm
public class BikeUser extends RealmObject {
    @PrimaryKey
    @Required
    private String mUserId;

    // List of all the Bikes a user owns
    private RealmList<Bike> mBikes;

    // List of all the rides a user has taken
    // Not really used because I wanted the rides sorted by date
    // which i am unable to do with RealmList
    private RealmList<Ride> mRides;

    // How much money a user has on his account
    private double mTokens;

    // Constructor, sets the userId to the Realm
    // SyncUser.current.getIdentity
    public BikeUser(String id){
        mUserId = id;
    }

    public BikeUser(){}

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public RealmList<Bike> getBikes() {
        return mBikes;
    }

    public void setBikes(RealmList<Bike> bikes) {
        mBikes = bikes;
    }

    public RealmList<Ride> getRides() {
        return mRides;
    }

    public void setRides(RealmList<Ride> rides) {
        mRides = rides;
    }

    public double getTokens() {
        return mTokens;
    }

    public void setTokens(double tokens) {
        mTokens = tokens;
    }
}
