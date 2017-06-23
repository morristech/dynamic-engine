package com.pranavpandey.android.dynamic.engine.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.pranavpandey.android.dynamic.engine.model.DynamicAppInfo;
import com.pranavpandey.android.dynamic.engine.service.DynamicEngine;

/**
 * Collection of useful functions used by the DynamicEngine.
 *
 * @see DynamicEngine
 */
public class DynamicEngineUtils {

    /**
     * Intent action constant for the on call state.
     */
    public static final String ACTION_ON_CALL =
            "com.pranavpandey.android.dynamic.engine.ACTION_ON_CALL";

    /**
     * Intent action constant for the call idle state.
     */
    public static final String ACTION_CALL_IDLE =
            "com.pranavpandey.android.dynamic.engine.ACTION_CALL_IDLE";

    /**
     * Constant for the package scheme.
     */
    private static final String PACKAGE_SCHEME = "package";

    /**
     * Get DynamicAppInfo from the package name.
     *
     * @param context Context to get {@link PackageManager}.
     * @param packageName Package name to build the {@link DynamicAppInfo}.
     */
    public static DynamicAppInfo getAppInfoFromPackage(
            @NonNull Context context, String packageName) {
        if (packageName != null) {
            DynamicAppInfo dynamicAppInfo = new DynamicAppInfo();
            try {
                dynamicAppInfo.setApplicationInfo(context.getPackageManager().getApplicationInfo(
                        packageName, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            dynamicAppInfo.setPackageName(packageName);
            dynamicAppInfo.setLabel(dynamicAppInfo.getApplicationInfo().
                    loadLabel(context.getPackageManager()).toString());

            return dynamicAppInfo;
        }

        return null;
    }

    /**
     * Get intent filter to register a broadcast receiver which can
     * listen special actions of the DynamicEngine.
     *
     * @see DynamicEngine
     */
    public static IntentFilter getEventsIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(Intent.ACTION_DOCK_EVENT);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(ACTION_ON_CALL);
        intentFilter.addAction(ACTION_CALL_IDLE);

        return intentFilter;
    }

    /**
     * Get intent filter to register a broadcast receiver which can
     * listen package added or removed broadcasts.
     */
    public static IntentFilter getPackageIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme(PACKAGE_SCHEME);

        return intentFilter;
    }
}
