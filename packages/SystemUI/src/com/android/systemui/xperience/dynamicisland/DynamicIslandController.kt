package com.android.systemui.xperience.dynamicisland

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.android.systemui.dagger.SysUISingleton
import javax.inject.Inject

@SysUISingleton
class DynamicIslandController @Inject constructor(
    private val context: Context
) {
    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private var overlay: View? = null

        fun start() {
            Log.i("DynamicIslandController", "✅ DynamicIsland started!")

            if (overlay != null) {
                Log.i("DynamicIslandController", "⚠️ Already started, skipping...")
                return
            }

            Handler(Looper.getMainLooper()).post {
                try {
                    val textView = TextView(context).apply {
                        text = "🟢 DynamicIsland"
                        setBackgroundColor(Color.argb(180, 0, 0, 0))
                        setTextColor(Color.WHITE)
                        setPadding(20, 10, 20, 10)
                    }

                    val container = FrameLayout(context)
                    container.addView(textView)

                    val layoutParams = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                            else
                                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                                PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        x = 0
                        y = 100
                    }

                    windowManager.addView(container, layoutParams)
                    overlay = container
                    Log.i("DynamicIslandController", "✅ Overlay added successfully!")
                } catch (e: Exception) {
                    Log.e("DynamicIslandController", "❌ Failed to add overlay: ${e.message}", e)
                }
            }
        }

        fun remove() {
            overlay?.let { view ->
                Handler(Looper.getMainLooper()).post {
                    try {
                        windowManager.removeView(view)
                        overlay = null
                        Log.i("DynamicIslandController", "✅ Overlay removed")
                    } catch (e: Exception) {
                        Log.e("DynamicIslandController", "❌ Failed to remove overlay: ${e.message}", e)
                    }
                }
            }
        }
}
