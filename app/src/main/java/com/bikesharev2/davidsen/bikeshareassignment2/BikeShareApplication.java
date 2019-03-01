package com.bikesharev2.davidsen.bikeshareassignment2;

import android.app.Application;

import io.realm.Realm;

public class BikeShareApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Realm.init(this);
    }
}
