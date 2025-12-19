/*
 * Copyright (C) 2025 The XPerience Project
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

package com.android.systemui.xperience.dynamicisland.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.android.systemui.xperience.dynamicisland.DynamicIslandView
import com.android.systemui.xperience.dynamicisland.IslandState

@Composable
fun DynamicIslandOverlay(
    state: IslandState,
    title: String,
    artist: String,
    duration: Long = 0L,
    position: Long = 0L,
    isPlaying: Boolean = true,
    artworkBytes: ByteArray? = null,
    currentPackageName: String = "",
    onMediaAction: (String) -> Unit = {},
    onToggleExpansion: (Boolean) -> Unit
) {
    DynamicIslandView(
        state = state,
        title = title,
        artist = artist,
        duration = duration,
        position = position,
        isPlaying = isPlaying,
        artworkBytes = artworkBytes,
        currentPackageName = currentPackageName,
        onMediaAction = onMediaAction,
        onToggleExpansion = onToggleExpansion
    )
}

@Composable
fun HotspotContent() {
    Text("📡 Compartiendo Internet", color = Color.White)
}

@Composable
fun UsbTetherContent() {
    Text("🔌 USB Tethering activo", color = Color.White)
}
