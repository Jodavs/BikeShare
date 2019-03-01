package com.bikesharev2.davidsen.bikeshareassignment2;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

// Ride object stored in Realm
public class Ride extends RealmObject {
    @PrimaryKey
    @Required
    private String mRideId;

    // Saving the id's of Bike and User
    @Required
    private String  mBikeId, mUserId;

    // Starting location for the ride
    private String mStartLocationName;

    // End location for the ride
    private String mEndLocationName;

    // Duration of the ride
    private String mDuration;

    // The name of the bike for the ride
    private String mBikeName;

    // If the ride is still active or has finished
    private boolean mIsActive;

    // Latitude and longitude for the start and end locations
    private double mLatStart, mLatEnd, mLonStart, mLonEnd;

    // Start and end date for the ride
    private Date mStartDate, mEndDate;

    // Price for the ride
    private double mRidePrice;

    // Constructor, generates a random id for the ride
    public Ride(){
        mRideId = (UUID.randomUUID()).toString();
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }

    public String getBikeName() {
        return mBikeName;
    }

    public void setBikeName(String bikeName) {
        mBikeName = bikeName;
    }

    public Ride(String id){
        mRideId = id;
    }

    public double getLatStart() {
        return mLatStart;
    }

    public void setLatStart(double latStart) {
        mLatStart = latStart;
    }

    public double getLatEnd() {
        return mLatEnd;
    }

    public void setLatEnd(double latEnd) {
        mLatEnd = latEnd;
    }

    public double getLonStart() {
        return mLonStart;
    }

    public void setLonStart(double lonStart) {
        mLonStart = lonStart;
    }

    public double getLonEnd() {
        return mLonEnd;
    }

    public void setLonEnd(double lonEnd) {
        mLonEnd = lonEnd;
    }

    public double getRidePrice() {
        return mRidePrice;
    }

    public void setRidePrice(double ridePrice) {
        mRidePrice = ridePrice;
    }

    public String getRideId() {
        return mRideId;
    }

    public void setRideId(String rideId) {
        mRideId = rideId;
    }

    public String getBikeId() {
        return mBikeId;
    }

    public void setBikeId(String bikeId) {
        mBikeId = bikeId;
    }

    public String getStartLocationName() {
        return mStartLocationName;
    }

    public void setStartLocationName(String startLocationName) {
        mStartLocationName = startLocationName;
    }

    public String getEndLocationName() {
        return mEndLocationName;
    }

    public void setEndLocationName(String endLocationName) {
        mEndLocationName = endLocationName;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean active) {
        mIsActive = active;
    }
}
