/*
 * Copyright (C) 2011-2025 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Display;

/**
 * Controller for monitoring and displaying refresh rate information in status bar
 * Similar to Redmagic devices refresh rate indicator
 */
public class RefreshRateIndicatorController implements DisplayManager.DisplayListener {

    private final Context mContext;
    private final Handler mHandler;
    private final DisplayManager mDisplayManager;
    private Callback mCallback;
    private boolean mEnabled;
    private boolean mListening = false;

    public interface Callback {
        void onRefreshRateChanged(float refreshRate);
    }

    public RefreshRateIndicatorController(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mDisplayManager = mContext.getSystemService(DisplayManager.class);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * @return true if the controller is currently listening for display changes
     */
    public boolean isListening() {
        return mListening;
    }

    /**
     * Start listening for display refresh rate changes
     */
    public void startListening() {
        mDisplayManager.registerDisplayListener(this, mHandler);
        updateEnabled();
        mListening = true;
        // Get current refresh rate and notify immediately
        Display display = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
        if (display != null && mEnabled && mCallback != null) {
            float refreshRate = display.getRefreshRate();
            mCallback.onRefreshRateChanged(refreshRate);
        }
    }

    /**
     * Stop listening for display changes
     */
    public void stopListening() {
        mListening = false;
        mDisplayManager.unregisterDisplayListener(this);
    }

    /**
     * Update enabled state based on system settings
     */
    private void updateEnabled() {
        mEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                "show_refresh_rate", 0, android.os.UserHandle.USER_CURRENT) == 1;
    }

    @Override
    public void onDisplayAdded(int displayId) {
        // No implementation needed for default display monitoring
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        // No implementation needed for default display monitoring
    }

    @Override
    public void onDisplayChanged(int displayId) {
        // Only respond to changes on the default display
        if (displayId == Display.DEFAULT_DISPLAY && mEnabled && mCallback != null) {
            Display display = mDisplayManager.getDisplay(displayId);
            float refreshRate = display.getRefreshRate();
            mCallback.onRefreshRateChanged(refreshRate);
        }
    }

    /**
     * Format refresh rate value for display purposes
     * @param refreshRate the refresh rate value to format
     * @return formatted string representation (e.g., "120Hz")
     */
    public String getFormattedRefreshRate(float refreshRate) {
        int roundedRate = Math.round(refreshRate);
        switch (roundedRate) {
            case 144: return "144Hz";
            case 120: return "120Hz";
            case 90: return "90Hz";
            case 60: return "60Hz";
            default: return roundedRate + "Hz";
        }
    }
}