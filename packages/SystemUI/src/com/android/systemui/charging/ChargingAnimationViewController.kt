/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * Copyright (C) 2026 The XPerience Project
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
package com.android.systemui.charging

import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.FrameLayout
import com.android.settingslib.Utils
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.statusbar.commandline.Command
import com.android.systemui.statusbar.commandline.CommandRegistry
import com.android.systemui.statusbar.policy.BatteryController
import com.android.systemui.util.ScrimUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.PrintWriter
import javax.inject.Inject

@SysUISingleton
class ChargingAnimationViewController @Inject constructor(
    private val context: Context,
    private val batteryController: BatteryController,
    private val commandRegistry: CommandRegistry,
) : ScrimUtils.ScrimEventListener, BatteryController.BatteryStateChangeCallback {

    private val chargingView = ChargingAnimationView(context)
    private val settingsRepo = ChargingAnimationSettingsRepository(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var currentSettings = settingsRepo.currentSettings()
    private var settingsJob: Job? = null

    private var isKeyguardShowing: Boolean = false
    private var isDozing: Boolean = false
    private var wasPluggedIn: Boolean = false
    private var lastShowTime: Long = 0L

    init {
        INSTANCE = this

        try {
            ScrimUtils.get()?.addListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding ScrimUtils listener", e)
        }

        batteryController.addCallback(this)
        commandRegistry.registerCommand("charging-animation") { ChargingAnimationCommand() }
        observeSettings()
    }

    fun getChargingView(): FrameLayout = chargingView

    fun isCustomAnimationActive(): Boolean {
        return currentSettings.isEnabled &&
                currentSettings.animationStyle != ChargingAnimationView.STYLE_AOSP_RIPPLE
    }

    fun showAnimation(batteryLevel: Int) {
        if (!currentSettings.isEnabled) {
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastShowTime < DEBOUNCE_MS) {
            return
        }
        lastShowTime = now

        updateViewWithSettings(currentSettings)
        chargingView.show(batteryLevel)
    }

    fun hideAnimation() {
        chargingView.hide()
    }

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = scope.launch {
            settingsRepo.settingsFlow
                .catch { e ->
                    Log.e(TAG, "Error observing settings", e)
                }
                .collect { settings ->
                    currentSettings = settings
                    updateViewWithSettings(settings)
                }
        }
    }

    private fun updateViewWithSettings(settings: ChargingAnimationSettings) {
        val accent = Utils.getColorAccentDefaultColor(context)
        chargingView.apply {
            accentColor = accent
            colorMode = settings.colorMode
            animationStyle = settings.animationStyle
            rippleOpacity = settings.rippleOpacity
            glowIntensity = settings.glowIntensity
            arcCount = settings.arcCount
        }
    }

    override fun onBatteryLevelChanged(level: Int, pluggedIn: Boolean, charging: Boolean) {
        val justPlugged = !wasPluggedIn && pluggedIn
        wasPluggedIn = pluggedIn

        if (justPlugged && currentSettings.isEnabled) {
            val shouldShow = when {
                isDozing -> currentSettings.showOnAod
                isKeyguardShowing -> currentSettings.showOnLockscreen
                else -> false
            }

            if (shouldShow && isCustomAnimationActive()) {
                showAnimation(level)
            }
        }
    }

    override fun onKeyguardShowingChanged(showing: Boolean) {
        isKeyguardShowing = showing
        if (!showing) {
            hideAnimation()
        }
    }

    override fun onDozingChanged(dozing: Boolean) {
        isDozing = dozing
    }

    override fun setPulsing(pulsing: Boolean) {
        if (!pulsing) {
            // Screen might be fading
        }
    }

    override fun onKeyguardFadingAwayChanged(fadingAway: Boolean) {
        if (fadingAway) {
            hideAnimation()
        }
    }

    override fun onKeyguardGoingAwayChanged(goingAway: Boolean) {
        if (goingAway) {
            hideAnimation()
        }
    }

    inner class ChargingAnimationCommand : Command {
        override fun execute(pw: PrintWriter, args: List<String>) {
            val level = args.getOrNull(0)?.toIntOrNull() ?: 86
            val styleOverride = args.getOrNull(1)?.toIntOrNull()
            chargingView.post {
                updateViewWithSettings(currentSettings)
                if (styleOverride != null) {
                    chargingView.animationStyle = styleOverride
                }
                chargingView.show(level)
            }
            pw.println("Showing charging animation (level: $level, style: ${styleOverride ?: currentSettings.animationStyle})")
        }

        override fun help(pw: PrintWriter) {
            pw.println("Usage: adb shell cmd statusbar charging-animation [batteryLevel] [style]")
        }
    }

    companion object {
        private const val TAG = "ChargingAnimViewController"
        private const val DEBOUNCE_MS = 2500L

        @Volatile
        private var INSTANCE: ChargingAnimationViewController? = null

        @JvmStatic
        fun get(context: Context): ChargingAnimationViewController {
            return INSTANCE ?: throw IllegalStateException(
                "ChargingAnimationViewController not initialized"
            )
        }
    }
}
