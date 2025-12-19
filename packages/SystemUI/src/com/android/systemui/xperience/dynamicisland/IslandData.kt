/*
 *  Copyright (C) 2025 The XPerience Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.android.systemui.xperience.dynamicisland


// Este es el único objeto que contendrá TODOS los datos de la Dynamic Island.
// Se recomienda usar Parcelize para que pueda ser pasado fácilmente si fuera necesario,
// aunque en este caso se usará para el StateFlow.

data class IslandData(
    val state: IslandState = IslandState.Idle,
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val duration: Long = 0L,
    val position: Long = 0L,
    val artworkBytes: ByteArray? = null,
    val packageName: String = ""
)
