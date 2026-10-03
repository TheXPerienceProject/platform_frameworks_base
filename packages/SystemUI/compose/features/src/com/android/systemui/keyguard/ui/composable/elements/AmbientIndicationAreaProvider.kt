/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.keyguard.ui.composable.elements

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.android.compose.animation.scene.ElementContentScope
import com.android.compose.animation.scene.ElementKey
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.nowplaying.NowPlayingViewController
import com.android.systemui.nowplaying.ambient.PixelAmbientIndicationDetector
import com.android.systemui.plugins.keyguard.ui.composable.elements.BaseLockscreenElement.ElementSource
import com.android.systemui.plugins.keyguard.ui.composable.elements.LockscreenElement
import com.android.systemui.plugins.keyguard.ui.composable.elements.LockscreenElementKeys
import com.android.systemui.plugins.keyguard.ui.composable.elements.LockscreenElementProvider
import com.android.systemui.plugins.keyguard.ui.composable.elements.LockscreenScope
import com.android.systemui.shade.ShadeDisplayAware
import dagger.Lazy
import javax.inject.Inject

@SysUISingleton
class AmbientIndicationAreaProvider
@Inject
constructor(
    @ShadeDisplayAware private val context: Context,
    private val nowPlayingViewControllerLazy: Lazy<NowPlayingViewController>? = null,
) : LockscreenElementProvider {

    override val elements: List<LockscreenElement> by lazy {
        listOf(AmbientIndicationAreaElement())
    }

    private inner class AmbientIndicationAreaElement : LockscreenElement {

        override val key: ElementKey = LockscreenElementKeys.AmbientIndicationArea

        private val isPixel by lazy {
            PixelAmbientIndicationDetector.shouldUseNativeAmbientIndication(this@AmbientIndicationAreaProvider.context)
        }

        @Composable
        override fun LockscreenScope<ElementContentScope>.LockscreenElement() {
            if (isPixel) {
                return
            }
            val controller = nowPlayingViewControllerLazy?.get() ?: return
            AndroidView(
                factory = {
                    val view = controller.getNowPlayingView()
                    (view.parent as? ViewGroup)?.removeView(view)
                    view
                },
                onRelease = { view ->
                    (view.parent as? ViewGroup)?.removeView(view)
                },
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            )
        }

        override val context = this@AmbientIndicationAreaProvider.context
        override val source = ElementSource.STANDARD
    }
}

