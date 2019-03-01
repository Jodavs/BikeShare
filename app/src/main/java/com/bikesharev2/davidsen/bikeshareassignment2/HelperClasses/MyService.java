package com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bikesharev2.davidsen.bikeshareassignment2.RidesDB;

import io.realm.SyncUser;

// Used when the app is killed unexpectedly to close realm
// seeing as the main activities on destroy is not called
public class MyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("ClearFromRecentService", "Service Destroyed");
        //RidesDB.CloseRealm();
        stopSelf();
    }
}
