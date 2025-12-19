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

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.unit.dp
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity

@Composable
fun DynamicIsland(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    state: IslandState,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val densityDpi = LocalContext.current.resources.displayMetrics.densityDpi
    val isHighDpi = densityDpi >= 540
    val isIdle = state == IslandState.Idle

    // obtain the cutout (punch-hole)
    val displayCutout = WindowInsets.displayCutout.getTop(LocalDensity.current)
    val resources = LocalContext.current.resources

    // Calculate the Y position of the punch-hole (approximately)
    val punchHoleOffset = if (displayCutout > 0) {
        // Use the actual cutout height
        (displayCutout / LocalDensity.current.density).dp
    } else {
        // Fallback: approximate position for most devices without a top cutout
        (-25).dp
    }

    // Animaciones para IDLE
    val width by animateDpAsState(
        targetValue = when {
            isIdle -> 8.dp // ← Círculo mini en idle
            expanded -> 300.dp
            else -> 120.dp
        },
        animationSpec = tween(durationMillis = 400),
        label = "Width"
    )

    val height by animateDpAsState(
        targetValue = when {
            isIdle -> 8.dp // ← Círculo mini en idle
            expanded -> 83.dp
            else -> if (isHighDpi) 42.dp else 38.dp
        },
        animationSpec = tween(durationMillis = 400),
        label = "Height"
    )

    val cornerRadius by animateDpAsState(
        targetValue = when {
            isIdle -> 4.dp // ← Círculo perfecto en idle
            expanded -> 20.dp
            else -> 18.dp
        },
        animationSpec = tween(durationMillis = 300),
        label = "CornerRadius"
    )

    // OFFSET para moverlo al punch-hole en idle
    val islandOffset by animateDpAsState(
        targetValue = if (isIdle) punchHoleOffset else 0.dp,
        animationSpec = tween(durationMillis = 400),
        label = "Offset"
    )

    Box(
        modifier = modifier
            .padding(vertical = if (isHighDpi) 6.dp else 0.dp)
            .offset(y = islandOffset)
            .size(width, height)
            .clip(RoundedCornerShape(cornerRadius))
            .shadow(
                elevation = if (isIdle) 0.dp else if (expanded) 20.dp else 10.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isIdle
            ) { if (!isIdle) onToggle(!expanded) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .customBlur(if (expanded) 45f else 25f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = if (expanded) 0.50f else 0.75f),
                            Color.Black.copy(alpha = if (expanded) 0.80f else 0.95f)
                        )
                    ),
                    RoundedCornerShape(cornerRadius)
                )
        )
        if (!isIdle) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }

    }
}

fun Modifier.customBlur(radius: Float): Modifier = this.then(
    graphicsLayer {
        if (radius > 0f) {
            renderEffect = RenderEffect
                .createBlurEffect(
                    radius,
                    radius,
                    Shader.TileMode.CLAMP
                )
                .asComposeRenderEffect()
        }
    }
)
