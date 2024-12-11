/*
 * Copyright (C) 2023-2024 the risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.notifications.ui

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.View

import com.android.systemui.Dependency
import com.android.systemui.plugins.statusbar.StatusBarStateController
import com.android.systemui.statusbar.policy.ConfigurationController
import com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
import com.android.systemui.statusbar.NotificationListener

class PeekDisplayViewController private constructor() : 
    ConfigurationController.ConfigurationListener,
    StatusBarStateController.StateListener,
    NotificationListener.NotificationHandler {

    private lateinit var mView: View
    private lateinit var mPeekDisplayView: PeekDisplayView
    private lateinit var mContext: Context

    private val configurationController: ConfigurationController = Dependency.get(ConfigurationController::class.java)
    private val statusBarStateController: StatusBarStateController = Dependency.get(StatusBarStateController::class.java)

    private var mDozing = false
    private var mNotifListenerRegistered = false

    private val settingsObserver: ContentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            mPeekDisplayView.updatePeekDisplayState()
        }
    }

    fun setPeekDisplayView(view: View) {
        mView = view
        mPeekDisplayView = mView as PeekDisplayView
        mContext = mView.context
    }

    fun registerCallbacks() {
        if (!mNotifListenerRegistered) {
            mPeekDisplayView.notificationListener.addNotificationHandler(this)
            mNotifListenerRegistered = true
        }
        configurationController.addCallback(this)
        statusBarStateController.addCallback(this)
        onDozingChanged(statusBarStateController.isDozing())
        mContext.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor("peek_display_notifications"), false, settingsObserver)
        mContext.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(
                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS), 
                false, settingsObserver)
    }

    fun unregisterCallbacks() {
        configurationController.removeCallback(this)
        statusBarStateController.removeCallback(this)
        mContext.contentResolver.unregisterContentObserver(settingsObserver)
    }

    override fun onStateChanged(newState: Int) {}

    override fun onDozingChanged(dozing: Boolean) {
        if (mDozing == dozing) {
            return
        }
        mDozing = dozing
        if (mDozing) {
            hidePeekDisplayView()
            mPeekDisplayView.resetNotificationShelf()
        } else {
            showPeekDisplayView()
        }
    }

    override fun onUiModeChanged() {
        mPeekDisplayView.updateViewColors()
    }
    
    override fun onThemeChanged() {
        mPeekDisplayView.updateViewColors()
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification,
        rankingMap: NotificationListenerService.RankingMap
    ) {
        mPeekDisplayView.currentRankingMap = rankingMap
        mPeekDisplayView.updateNotificationShelf(mPeekDisplayView.notificationListener.getActiveNotifications().toList())
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: NotificationListenerService.RankingMap,
        reason: Int
    ) {
        mPeekDisplayView.currentRankingMap = rankingMap
        mPeekDisplayView.updateNotificationShelf(mPeekDisplayView.notificationListener.getActiveNotifications().toList())
    }
    
    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: NotificationListenerService.RankingMap
    ) {
        mPeekDisplayView.currentRankingMap = rankingMap
        mPeekDisplayView.updateNotificationShelf(mPeekDisplayView.notificationListener.getActiveNotifications().toList())
    }

    override fun onNotificationRankingUpdate(rankingMap: NotificationListenerService.RankingMap) {
        mPeekDisplayView.currentRankingMap = rankingMap
    }
    override fun onNotificationsInitialized() {}

    fun hidePeekDisplayView() {
        mView.visibility = View.GONE
        mPeekDisplayView.hideNotificationCard()
    }

    fun showPeekDisplayView() {
        if (!mPeekDisplayView.isPeekDisplayEnabled) return
        mView.visibility = View.VISIBLE
    }
    
    companion object {
        @Volatile
        private var instance: PeekDisplayViewController? = null

        fun getInstance(): PeekDisplayViewController {
            return instance ?: synchronized(this) {
                instance ?: PeekDisplayViewController().also { instance = it }
            }
        }
    }
}
