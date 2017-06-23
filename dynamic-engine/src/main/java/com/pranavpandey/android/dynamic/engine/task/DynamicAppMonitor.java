/*
 * Copyright (C) 2017 Pranav Pandey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranavpandey.android.dynamic.engine.task;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;

import com.pranavpandey.android.dynamic.engine.model.DynamicAppInfo;
import com.pranavpandey.android.dynamic.engine.service.DynamicEngine;
import com.pranavpandey.android.dynamic.engine.utils.DynamicEngineUtils;
import com.pranavpandey.android.dynamic.utils.DynamicVersionUtils;

/**
 * AsyncTask to monitor foreground to provide app specific functionality.
 * <br /><br />
 * Package must be granted {@link android.Manifest.permission#PACKAGE_USAGE_STATS}
 * permission to detect foreground app on Android L and above devices.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DynamicAppMonitor extends AsyncTask<Void, DynamicAppInfo, Void> {

    /**
     * Context constant for usage stats service.
     *
     * @see Context#USAGE_STATS_SERVICE
     */
    private static final String USAGE_STATS = "usagestats";

    /**
     * Default usage stats interval.
     */
    private static final int USAGE_STATS_INTERVAL = 50;

    /**
     * Default thread sleep interval.
     */
    private static final int THREAD_SLEEP_INTERVAL = 250;

    /**
     * DynamicEngine object to initialize usage stats service.
     */
    private DynamicEngine mDynamicEngine;

    /**
     * {@code true} if this task is running.
     */
    private boolean isRunning;

    /**
     * DynamicAppInfo for the foreground package.
     */
    private DynamicAppInfo mDynamicAppInfo;

    /**
     * ActivityManager to detect foreground package activities.
     */
    private ActivityManager mActivityManager;

    /**
     * UsageStatsManager to detect foreground package on Android L
     * and above devices.
     * <br /><br />
     * Package must be granted {@link android.Manifest.permission#PACKAGE_USAGE_STATS}
     * permission to detect foreground app on Android L and above devices.
     */
    private UsageStatsManager mUsageStatsManager;

    /**
     * Constructor to initialize DynamicAppMonitor for the gove DynamicEngine.
     */
    public DynamicAppMonitor(DynamicEngine dynamicEngine) {
        this.mDynamicEngine = dynamicEngine;
        this.mActivityManager = (ActivityManager)
                dynamicEngine.getSystemService(Context.ACTIVITY_SERVICE);

        if (DynamicVersionUtils.isLollipop()) {
            if (DynamicVersionUtils.isLollipopMR1()) {
                this.mUsageStatsManager = (UsageStatsManager)
                        dynamicEngine.getSystemService(Context.USAGE_STATS_SERVICE);
            }

            if (mUsageStatsManager == null) {
                mUsageStatsManager = (UsageStatsManager)
                        dynamicEngine.getSystemService(USAGE_STATS);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mDynamicAppInfo = null;
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(isRunning()) {
            try {
                DynamicAppInfo dynamicAppInfo = getForegroundAppInfo();
                if (dynamicAppInfo != null && dynamicAppInfo.getPackageName() != null) {
                    if ((getCurrentAppInfo() == null
                            || !getCurrentAppInfo().equals(dynamicAppInfo))) {
                        setCurrentAppInfo(dynamicAppInfo);
                        publishProgress(dynamicAppInfo);
                    }
                }

                Thread.sleep(THREAD_SLEEP_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(DynamicAppInfo... dynamicAppInfo) {
        super.onProgressUpdate(dynamicAppInfo);

        mDynamicEngine.getSpecialEventListener().onAppChange(dynamicAppInfo[0]);
    }

    @Override
    protected void onPostExecute(Void param) {
        super.onPostExecute(param);

        mDynamicAppInfo = null;
    }

    /**
     * Getter for {@link #isRunning}.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Getter for {@link #mDynamicAppInfo}.
     */
    public DynamicAppInfo getCurrentAppInfo() {
        return mDynamicAppInfo;
    }

    /**
     * Setter for {@link #mDynamicAppInfo}.
     */
    private void setCurrentAppInfo(DynamicAppInfo dynamicAppInfo) {
        this.mDynamicAppInfo = dynamicAppInfo;
    }

    /**
     * Setter for {@link #isRunning}.
     */
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * Get DynamicAppInfo from the foreground package name.
     */
    private DynamicAppInfo getForegroundAppInfo() {
        String packageName = null;
        DynamicAppInfo dynamicAppInfo = null;

        if (DynamicVersionUtils.isLollipop()) {
            packageName = getForegroundPackage(
                    System.currentTimeMillis(), USAGE_STATS_INTERVAL);
        } else {
            @SuppressWarnings("deprecation")
            ActivityManager.RunningTaskInfo runningTaskInfo =
                    mActivityManager.getRunningTasks(1).get(0);
            if (runningTaskInfo.topActivity != null) {
                packageName = runningTaskInfo.topActivity.getPackageName();
            }
        }

        if (packageName != null) {
            dynamicAppInfo = DynamicEngineUtils.getAppInfoFromPackage(mDynamicEngine, packageName);
        }

        return dynamicAppInfo;
    }

    /**
     * Get foreground package name on Android L and above devices.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String getForegroundPackage(long time, long interval) {
        String packageName = null;

        UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - interval * 1000, time);
        UsageEvents.Event event = new UsageEvents.Event();

        // get last event
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                packageName = event.getPackageName();
            }
        }

        return packageName;
    }
}
