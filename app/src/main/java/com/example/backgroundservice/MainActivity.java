package com.example.backgroundservice;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    Intent mServiceIntent;
    private YourService mYourService;
    private static final int MY_PERMISSIONS_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runtimePermission();
        if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)) {
            BatteryOptimizationHelper.requestIgnoringBatteryOptimizations(this);
        }
//        mYourService = new YourService();
//        mServiceIntent = new Intent(this, mYourService.getClass());
//        if (!isMyServiceRunning(mYourService.getClass())) {
//            startService(mServiceIntent);
//        }
    }

    private void runtimePermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the request code matches our permission request
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            // Check if the permission is granted
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                // Execute the service class that is YourService.class
                mYourService = new YourService();
                mServiceIntent = new Intent(this, mYourService.getClass());
                if (!isMyServiceRunning(mYourService.getClass())) {
                    startService(mServiceIntent);
                }
            } else {

            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Uncomment the line below if you want to stop the service when the activity is destroyed.
        // stopService(mServiceIntent);
        // Instead of stopping the service, send a broadcast to restart it.
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }
}