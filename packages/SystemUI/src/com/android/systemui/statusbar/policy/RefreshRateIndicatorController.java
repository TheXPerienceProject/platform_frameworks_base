/*
 * Copyright (C) 2011-2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.Display;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.statusbar.phone.RefreshRateIconGenerator;
import com.android.systemui.statusbar.phone.ui.StatusBarIconController;

import javax.inject.Inject;

/** Displays the current refresh rate as a compact status-bar indicator. */
@SysUISingleton
public class RefreshRateIndicatorController implements DisplayManager.DisplayListener {
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final DisplayManager mDisplayManager;
    private final StatusBarIconController mStatusBarIconController;

    private final ContentObserver mSettingsObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            updateIndicator(false);
        }
    };

    private boolean mListening;
    private boolean mVisible;
    private int mLastRefreshRate = Integer.MIN_VALUE;

    @Inject
    public RefreshRateIndicatorController(
            Context context,
            StatusBarIconController statusBarIconController) {
        mContext = context;
        mDisplayManager = context.getSystemService(DisplayManager.class);
        mStatusBarIconController = statusBarIconController;
    }

    public void startListening() {
        if (mListening || mDisplayManager == null) {
            return;
        }

        mListening = true;
        mDisplayManager.registerDisplayListener(this, mHandler);
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SHOW_REFRESH_RATE),
                false,
                mSettingsObserver,
                UserHandle.USER_ALL);
        updateIndicator(true);
    }

    /** Rebuild the glyph after density/font/resource changes. */
    public void onConfigurationChanged() {
        if (mListening) {
            updateIndicator(true);
        }
    }

    private boolean isEnabled() {
        return Settings.System.getIntForUser(
                mContext.getContentResolver(),
                Settings.System.SHOW_REFRESH_RATE,
                0,
                UserHandle.USER_CURRENT) == 1;
    }

    private void updateIndicator(boolean force) {
        if (!isEnabled()) {
            if (mVisible) {
                mStatusBarIconController.setIconVisibility(
                        StatusBarIconController.SLOT_REFRESH_RATE, false);
                mVisible = false;
            }
            mLastRefreshRate = Integer.MIN_VALUE;
            return;
        }

        final Display display = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
        if (display == null) {
            return;
        }

        final int refreshRate = Math.round(display.getRefreshRate());
        if (!force && mVisible && refreshRate == mLastRefreshRate) {
            return;
        }

        mLastRefreshRate = refreshRate;
        final Bitmap bitmap = RefreshRateIconGenerator.generateRefreshRateIcon(
                mContext, refreshRate);
        final Drawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);

        mStatusBarIconController.setResourceIcon(
                StatusBarIconController.SLOT_REFRESH_RATE,
                mContext.getPackageName(),
                0,
                drawable,
                refreshRate + " Hz",
                StatusBarIcon.Shape.WRAP_CONTENT);
        mStatusBarIconController.setIconVisibility(
                StatusBarIconController.SLOT_REFRESH_RATE, true);
        mVisible = true;
    }

    @Override
    public void onDisplayAdded(int displayId) {}

    @Override
    public void onDisplayRemoved(int displayId) {}

    @Override
    public void onDisplayChanged(int displayId) {
        if (displayId == Display.DEFAULT_DISPLAY) {
            updateIndicator(false);
        }
    }
}
