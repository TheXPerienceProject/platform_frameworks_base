/*
 * Copyright (C) 2011-2025 The XPerience Project
 * Copyright (C) 2025 Carlos 'klozz' jesus <carlosj@klozz.dev>
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

/**
 * Controller for monitoring and displaying refresh rate information in status bar.
 * Provides automatic updates when display refresh rate or system settings change.
 * Similar to Redmagic devices refresh rate indicator implementation.
 */
public class RefreshRateIndicatorController implements DisplayManager.DisplayListener {

    private final Context mContext;
    private final Handler mHandler;
    private final DisplayManager mDisplayManager;
    private Callback mCallback;
    private boolean mEnabled;
    private boolean mListening = false;

    private ContentObserver mSettingsObserver;

    /**
     * Callback interface for refresh rate changes
     */
    public interface Callback {
        /**
         * Called when the refresh rate changes or settings are updated
         * @param refreshRate The current refresh rate in Hz, or 0 to hide the indicator
         */
        void onRefreshRateChanged(float refreshRate);
    }

    /**
     * Creates a new RefreshRateIndicatorController
     * @param context The context to use for resources and services
     */
    public RefreshRateIndicatorController(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mDisplayManager = mContext.getSystemService(DisplayManager.class);

        // Configure ContentObserver for settings changes
        mSettingsObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                updateEnabled();
                refreshIndicatorState();
            }
        };
    }

    /**
     * Sets the callback for refresh rate changes
     * @param callback The callback to notify when refresh rate changes
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * Check if the controller is currently active
     * @return true if the controller is listening for display changes
     */
    public boolean isListening() {
        return mListening;
    }

    /**
     * Start listening for display refresh rate changes and settings updates
     */
    public void startListening() {
        if (mListening) {
            return; // Already listening
        }

        mDisplayManager.registerDisplayListener(this, mHandler);
        
        // Register ContentObserver for automatic settings updates
        mContext.getContentResolver().registerContentObserver(
            Settings.System.getUriFor(Settings.System.SHOW_REFRESH_RATE),
            false,
            mSettingsObserver
        );
        
        updateEnabled();
        mListening = true;
        refreshIndicatorState();
        
        Log.d(TAG, "Refresh rate indicator controller started listening");
    }

    /**
     * Stop listening for display changes and settings updates
     */
    public void stopListening() {
        if (!mListening) {
            return;
        }

        mDisplayManager.unregisterDisplayListener(this);
        
        // Unregister ContentObserver
        if (mSettingsObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
        }
        
        mListening = false;
        Log.d(TAG, "Refresh rate indicator controller stopped listening");
    }

    /**
     * Update enabled state based on system settings
     */
    private void updateEnabled() {
        mEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.SHOW_REFRESH_RATE, 0, 
                android.os.UserHandle.USER_CURRENT) == 1;
        
        Log.d(TAG, "Refresh rate indicator setting updated: " + (mEnabled ? "enabled" : "disabled"));
    }

    /**
     * Refreshes the indicator state based on current settings and display state
     * This method is called when settings change or display configuration updates
     */
    private void refreshIndicatorState() {
        mHandler.post(() -> {
            if (mCallback != null) {
                Display display = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
                if (display != null) {
                    float refreshRate = display.getRefreshRate();
                    if (mEnabled) {
                        mCallback.onRefreshRateChanged(refreshRate);
                    } else {
                        mCallback.onRefreshRateChanged(0); // 0 = hide indicator
                    }
                }
            }
        });
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
        if (displayId == Display.DEFAULT_DISPLAY) {
            refreshIndicatorState();
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
    
    private static final String TAG = "RefreshRateIndicatorController";
}