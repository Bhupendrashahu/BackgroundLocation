package com.example.backgroundservice;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public class YourService extends Service {
    public int counter = 0;
    private Thread backgroundThread;
    private static final String NOTIFICATION_CHANNEL_ID = "example.permanence";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseReference = FirebaseDatabase.getInstance().getReference("location");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    private void startMyOwnForeground() {
        String channelName = "Background Service";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLightColor(Color.BLUE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        startBackgroundThread();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startBackgroundThread();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  " + (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); // Every 1 second
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("TAG", "onTaskRemoved: ");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmService != null) {
            alarmService.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, restartServicePendingIntent);
        }
        super.onTaskRemoved(rootIntent);
    }

    private void startBackgroundThread() {
        backgroundThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i("Count", "=========  " + (counter++));
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        backgroundThread.start();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000) // Update interval in milliseconds
                .setFastestInterval(1000) // Fastest update interval in milliseconds
                .setSmallestDisplacement(1); // Minimum displacement in meters for location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    // Handle location updates here
                    for (android.location.Location location : locationResult.getLocations()) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        // Do something with latitude and longitude
                        updateFirebaseLocation(latitude, longitude);
                        Log.d("TAG", "onLocationResult: " + latitude + "," + longitude);
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission check
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateFirebaseLocation(double latitude, double longitude) {
        LocationModel locationData = new LocationModel(latitude, longitude);
        String key = databaseReference.push().getKey();
        databaseReference.child(key).setValue(locationData);
    }
}
