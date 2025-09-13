/*
 * Copyright (C) 2011-2025 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.phone.ui.StatusBarIconController;
import com.android.systemui.res.R;

import javax.inject.Inject;

/**
 * Manager for handling custom status bar icons like refresh rate etc.
 * Generates dynamic icons for display information.
 */
public class IconManager {
    private static final String TAG = "IconManager";
    private final Context mContext;
    private final StatusBarIconController mStatusBarIconController;
    private final Handler mHandler;

    @Inject
    public IconManager(Context context, StatusBarIconController statusBarIconController) {
        mContext = context;
        mStatusBarIconController = statusBarIconController;
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Sets the refresh rate indicator icon in the status bar
     * @param refreshRate the current refresh rate value to display
     */
    public void setRefreshRateIndicator(int refreshRate) {
        Runnable updateRunnable = () -> {
            if (refreshRate <= 0) {
                hideRefreshRateIndicator();
                return;
            }
    
            String slot = StatusBarIconController.SLOT_REFRESH_RATE;
            
            // Generate dynamic icon with bold text and adaptive colors
            Bitmap iconBitmap = RefreshRateIconGenerator.generateRefreshRateIcon(mContext, refreshRate);
            Drawable drawable = new BitmapDrawable(mContext.getResources(), iconBitmap);
            
            String contentDescription = refreshRate + "Hz";

            Log.d(TAG, "Setting refresh rate icon: " + refreshRate + "Hz");

            // Use setResourceIcon with preloaded Drawable for dynamic bitmap support
            mStatusBarIconController.setResourceIcon(
                slot, 
                mContext.getPackageName(), // resPackage
                0, // iconResId (0 since we use preloadedIcon)
                drawable, // preloadedIcon - the dynamically generated bitmap
                contentDescription,
                StatusBarIcon.Shape.WRAP_CONTENT // Use wrap content for proper spacing
            );
            
            mStatusBarIconController.setIconVisibility(slot, true);
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            updateRunnable.run();
        } else {
            mHandler.post(updateRunnable);
        }
    }

    /**
     * Hides the refresh rate indicator from the status bar
     */
    public void hideRefreshRateIndicator() {
        mHandler.post(() -> {
            mStatusBarIconController.setIconVisibility(StatusBarIconController.SLOT_REFRESH_RATE, false);
        });
    }
}