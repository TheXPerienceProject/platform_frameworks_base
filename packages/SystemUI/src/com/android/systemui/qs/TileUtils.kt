/*
 * Copyright (C) 2023-2024 the risingOS Android Project
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
package com.android.systemui.qs

import android.content.Context
import android.content.res.Configuration
import android.os.UserHandle
import android.provider.Settings

object TileUtils {

    private const val TILE_LABEL_SIZE = "qs_tile_label_size"
    private const val TILE_SUMMARY_SIZE = "qs_tile_summary_size"
    private const val COMPACT_MEDIA_PLAYER_MODE = "qs_compact_media_player_mode"
    private const val QS_WIDGETS_ENABLED = "qs_widgets_enabled"

    @JvmStatic
    private fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
    }

    @JvmStatic
    private fun getSetting(context: Context, key: String, defaultValue: Int): Int {
        return Settings.System.getIntForUser(
            context.contentResolver,
            key,
            defaultValue,
            UserHandle.USER_CURRENT
        )
    }

    @JvmStatic
    fun getTileLabelSize(context: Context, defaultValue: Float): Float {
        return getSetting(context, TILE_LABEL_SIZE, defaultValue.toInt()).toFloat()
    }

    @JvmStatic
    fun getTileSummarySize(context: Context, defaultValue: Float): Float {
        return getSetting(context, TILE_SUMMARY_SIZE, defaultValue.toInt()).toFloat()
    }

    @JvmStatic
    fun isCompactQSMediaPlayerEnforced(context: Context): Boolean {
        return getSetting(context, COMPACT_MEDIA_PLAYER_MODE, 0) != 0
    }

    @JvmStatic
    fun isQsWidgetsEnabled(context: Context): Boolean {
        return getSetting(context, QS_WIDGETS_ENABLED, 0) != 0
    }

    @JvmStatic
    fun canShowQsWidgets(context: Context): Boolean {
        return isQsWidgetsEnabled(context) && isPortrait(context)
    }
}
