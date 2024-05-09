package com.example.backgroundservice;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

public class BatteryOptimizationHelper {

    public static boolean isIgnoringBatteryOptimizations(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true; // For devices before Marshmallow, assume it's not an issue
    }

    public static void requestIgnoringBatteryOptimizations(Context context) {
        if (!isIgnoringBatteryOptimizations(context)) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }
}

