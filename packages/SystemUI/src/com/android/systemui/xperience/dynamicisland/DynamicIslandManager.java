package com.android.systemui.xperience.dynamicisland;

import android.content.Context;
import android.util.Log;

/**
 * Manager class for Dynamic Island feature
 * Provides the main entry point for SystemUI integration
 */
public class DynamicIslandManager {
    private static final String TAG = "DynamicIslandManager";

    private final Context mContext;
    private DynamicIslandController mController;
    private DynamicIslandView mView;

    public DynamicIslandManager(Context context) {
        mContext = context;
        mController = new DynamicIslandController(context);
    }

    public void start() {
        Log.d(TAG, "Starting Dynamic Island Manager");
        mController.start();
    }

    public void setView(DynamicIslandView view) {
        mView = view;
        mController.setView(view);
        Log.d(TAG, "Dynamic Island view attached to manager");
    }

    public void destroy() {
        if (mController != null) {
            mController.destroy();
        }
        Log.d(TAG, "Dynamic Island Manager destroyed");
    }
}
