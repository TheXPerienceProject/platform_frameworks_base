/*
 * Copyright (C) 2024 The LeafOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.android.server.gmscompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.gmscompat.AttestationHooks;
import com.android.server.SystemService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AttestationService extends SystemService {
    private static final String TAG = AttestationService.class.getSimpleName();

    private static final long INITIAL_DELAY = 0;
    private static final long INTERVAL = 5;

    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final boolean SPOOF_GMS =
            SystemProperties.getBoolean("persist.sys.spoof.gms", true);

    private final Context mContext;
    private final ScheduledExecutorService mScheduler;

    public AttestationService(Context context) {
        super(context);
        mContext = context;
        mScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onStart() {}

    @Override
    public void onBootPhase(int phase) {
        if (SPOOF_GMS
                && isAppInstalled("com.google.android.gms")
                && phase == PHASE_BOOT_COMPLETED) {
            Log.i(TAG, "Scheduling the service");
            mScheduler.scheduleAtFixedRate(
                    new UpdateCertifiedProps(), INITIAL_DELAY, INTERVAL, TimeUnit.MINUTES);
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, packageName + " is not installed");
            return false;
        }
    }

    private void dlog(String message) {
        if (DEBUG) Log.d(TAG, message);
    }

    private class UpdateCertifiedProps implements Runnable {
        @Override
        public void run() {
            try {
                dlog("UpdateCertifiedProps started");
                AttestationHooks.updateCertifiedProps(mContext);
                dlog("UpdateCertifiedProps completed");
            } catch (Exception e) {
                Log.e(TAG, "Error in UpdateCertifiedProps", e);
            }
        }
    }
}
