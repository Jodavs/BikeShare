package com.bikesharev2.davidsen.bikeshareassignment2;

import android.location.Location;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

// Bike object stored in Realm
public class Bike extends RealmObject {
    @PrimaryKey
    @Required
    private String mId;

    // Stores the name of the bike, the type and start location name
    @Required
    private String mBikeName, mBikeType, mBikeLocation;

    // Latitude and longitude of the bike's location
    private double lat, lon;

    // Tells if the bike is currently booked or not
    private boolean mActive;

    // Stores the image for the bike
    private byte[] mBikeImage;

    // Rent price per hour for the biek
    private double mRentPrice;

    // Constructor, that sets the bike id to a random string
    // when a new bike is created
    public Bike(){
        mId = (UUID.randomUUID()).toString();
    }

    // Constructor, used when if want to change some values for a
    // already existing bike
    public Bike(String id){mId = id;}

    public String getBikeLocation(){return mBikeLocation;}

    public void setBikeLocation(String bikeLocation){this.mBikeLocation = bikeLocation;}

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getBikeName() {
        return mBikeName;
    }

    public void setBikeName(String bikeName) {
        mBikeName = bikeName;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public byte[] getBikeImage() {
        return mBikeImage;
    }

    public void setBikeImage(byte[] bikeImage) {
        mBikeImage = bikeImage;
    }

    public String getBikeType() {
        return mBikeType;
    }

    public void setBikeType(String bikeType) {
        mBikeType = bikeType;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public double getRentPrice() {
        return mRentPrice;
    }

    public void setRentPrice(double rentPrice) {
        mRentPrice = rentPrice;
    }

    public String getPhotoFilename(){
        return "IMG_" + getId() + ".jpg";
    }
}
