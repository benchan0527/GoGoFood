package com.group14.foodordering.util;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class to get unique device identifier for customer identification
 */
public class DeviceIdHelper {
    private static final String TAG = "DeviceIdHelper";
    private static final String PREFIX = "device_";

    /**
     * Get unique device ID for customer identification
     * Uses Android ID as the unique identifier
     */
    public static String getDeviceId(Context context) {
        try {
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId == null || androidId.isEmpty()) {
                // Fallback: use a combination of device info
                androidId = "device_" + System.currentTimeMillis();
                Log.w(TAG, "Android ID not available, using fallback: " + androidId);
            }
            return PREFIX + androidId;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get device ID", e);
            // Fallback: use timestamp
            return PREFIX + System.currentTimeMillis();
        }
    }
}

