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

package com.android.systemui.xperience.dynamicisland

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import com.android.systemui.xperience.dynamicisland.ui.HotspotContent
import com.android.systemui.xperience.dynamicisland.ui.UsbTetherContent
import java.util.concurrent.TimeUnit

@Composable
fun DynamicIslandView(
    state: IslandState,
    title: String = "",
    artist: String = "",
    duration: Long = 0L,
    position: Long = 0L,
    isPlaying: Boolean = true,
    artworkBytes: ByteArray? = null,
    currentPackageName: String? = null,
    onMediaAction: (String) -> Unit = {},
                            onToggleExpansion: (Boolean) -> Unit
) {

    var localPosition by remember(title) { mutableStateOf(position) }
    var isExpanded by remember { mutableStateOf(false) }
    var waveAnimationPhase by remember { mutableStateOf(0) }
    var lastTitle by remember { mutableStateOf(title) }
    var lastState by remember { mutableStateOf(state) }

    var artworkBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current

    LaunchedEffect(title, artworkBytes) {
        artworkBitmap = null // reset primero

        // SOLO intentar decodificar si tenemos bytes válidos
        if (artworkBytes != null && artworkBytes.isNotEmpty()) {
            try {
                val decoded = withContext(Dispatchers.IO) { // Cambié a Dispatchers.IO para operaciones de archivo/red
                    BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size)
                }
                if (decoded != null && currentCoroutineContext().isActive) {
                    artworkBitmap = decoded
                }
            } catch (e: Exception) {
                Log.e("DynamicIsland", "Error decoding artwork", e)
                artworkBitmap = null
            }
        } else {
            // Si no hay artwork bytes, asegurarse de que sea null
            artworkBitmap = null
        }
    }

    // detectar el cambio de cancion y expandir automaticamente
    LaunchedEffect(title, state) {
        if (state == IslandState.Music && title.isNotEmpty() && title != lastTitle) {
            lastTitle = title
            isExpanded = true //expandir inmediatamente
            onToggleExpansion(true) // ✅ Notificar al servicio

            //contraer tras 1.5s
            delay(1500L)

            if (currentCoroutineContext().isActive){
                isExpanded = false
                onToggleExpansion(false) // ✅ Notificar al servicio
            }
        } else if (title.isEmpty()) {
            lastTitle = ""
        }
    }

    // Animación de progreso de la canción - CORREGIDA
    LaunchedEffect(isPlaying, duration, title) {
        if (isPlaying && duration > 0) {
            while (localPosition < duration && isPlaying) {
                delay(1000)
                localPosition += 1000
                if (localPosition >= duration) {
                    localPosition = duration
                    break
                }
            }
        }
    }

    // Animación de ondas de sonido - CORREGIDA
    LaunchedEffect(isPlaying) {
        while (true) {
            if (isPlaying) {
                waveAnimationPhase = (waveAnimationPhase + 1) % 4
            }
            delay(200)
        }
    }

    //comportaminento normal para otros estados
    LaunchedEffect(state) {
        if (state != lastState) {
            lastState = state

            when (state) {
                IslandState.Idle -> {
                    isExpanded = false
                    onToggleExpansion(false) // ✅ Notificar al servicio
                }
                IslandState.Music -> {
                    // Para música, mantener el estado actual de expansión
                    // El cambio de canción se maneja en el otro LaunchedEffect
                }
                else -> {
                    // Para otros estados, expandir inmediatamente
                    isExpanded = true
                    onToggleExpansion(true) // ✅ Notificar al servicio
                }
            }
        }
    }

    DynamicIsland(
        expanded = isExpanded,
        state = state,
        onToggle = {
            newState -> isExpanded = newState
            onToggleExpansion(newState) // ✅ Notificar al servicio en cada clic
        },
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    // Abrir app de música
                    currentPackageName?.let { pkg ->
                        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                }
            )
        }
    ) {
        when (state) {
            IslandState.Music -> {
                if (isExpanded) {
                    ExpandedMusicContent(
                        title = title,
                        artist = artist,
                        duration = duration,
                        position = localPosition,
                        isPlaying = isPlaying,
                        artworkBitmap = artworkBitmap,
                        onPlayPause = { // ← CREARLO AQUÍ USANDO onMediaAction
                            val action = if (isPlaying) "PAUSE" else "PLAY"
                            onMediaAction(action)
                        }
                    )
                } else {
                    CollapsedMusicContent(
                        showWave = isPlaying,
                        wavePhase = waveAnimationPhase,
                        artworkBitmap = artworkBitmap
                    )
                }
            }
            IslandState.Call -> {
                Text("📞 Llamada activa", color = Color.White)
            }
            IslandState.Navigation -> {
                Text("🚗 Navegando", color = Color.White)
            }
            IslandState.Hotspot -> HotspotContent()
            IslandState.UsbTethering -> UsbTetherContent()
            IslandState.Idle -> {
                IdleContent()
            }
        }
    }
}

@Composable
fun ExpandedMusicContent(
    title: String,
    artist: String,
    duration: Long,
    position: Long,
    isPlaying: Boolean,
    artworkBitmap: Bitmap?,
    onPlayPause: () -> Unit = {}
) {
    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Fila superior: Info de la canción
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Carátula del álbum - CON IMAGEN REAL
            if (artworkBitmap != null) {
                Box(
                    modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = artworkBitmap.asImageBitmap(),
                                                      contentDescription = "Carátula del álbum",
                                                      modifier = Modifier
                                                      .size(36.dp)
                                                      .clip(RoundedCornerShape(8.dp))
                    )
                }
            } else {
                // Placeholder si no hay carátula
                Box(
                    modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
                        )
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MusicNote,
                         contentDescription = "Álbum",
                         tint = Color.White,
                         modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Información de la canción
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title.ifEmpty { "Título desconocido" },
                     color = Color.White,
                     fontSize = 14.sp,
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist.ifEmpty { "Artista desconocido" },
                     color = Color.White.copy(alpha = 0.7f),
                     fontSize = 12.sp,
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                )
            }

            // Botón de play/pause
            /* Icon(
             *                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
             *                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
             *                tint = Color.White,
             *                modifier = Modifier
             *                    .size(20.dp)
             *                    .clickable {
             *                        val intent = Intent("PLAY_PAUSE")
             *                        context.sendBroadcast(intent)
        }
        )*/
            WaveAnimation(active = isPlaying)
        }

        // Barra de progreso - SOLO SI HAY DURACIÓN VÁLIDA
        if (position > 0 || duration > 0) {
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().height(1.5.dp),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.3f)
            )
            val remaining = if (duration > 0) {
                (duration - position).coerceAtLeast(0L)
            } else {
                0L // Si no hay duración, muestra 0
            }


            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //val remaining = (duration - position).coerceAtLeast(0L)
                Text(text = formatTime(position), color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                Text(
                    text = if (duration > 0) "-${formatTime(remaining)}" else "--:--",
                     color = Color.White.copy(alpha = 0.8f),
                     fontSize = 10.sp
                )

            }
        }
    }
}

@Composable
fun CollapsedMusicContent(
    showWave: Boolean,
    wavePhase: Int,
    artworkBitmap: Bitmap?
) {
    Row(
        modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Carátula del álbum pequeña
        if (artworkBitmap != null) {
            Box(
                modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .shadow(2.dp, RoundedCornerShape(6.dp))
            ) {
                androidx.compose.foundation.Image(
                    bitmap = artworkBitmap.asImageBitmap(),
                                                  contentDescription = "Carátula",
                                                  modifier = Modifier
                                                  .size(20.dp)
                                                  .clip(RoundedCornerShape(6.dp))
                )
            }
        } else {
            Box(
                modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
                    )
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                     contentDescription = "Música",
                     tint = Color.White,
                     modifier = Modifier.size(12.dp)
                )
            }
        }

        // Animación de ondas de sonido mejorada
        WaveAnimation(active = showWave)
    }
}

@Composable
fun WaveAnimation(active: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tres barras que simulan ondas de sonido
        // Usamos una lista de índices para aplicar un delay diferente a cada barra.
        listOf(0, 1, 2).forEach { index ->
            // El valor de escala no se usa como altura directa, sino como un factor de delay/offset.
            AnimatedBar(active = active, delayFactor = index)
        }
    }
}

@Composable
fun AnimatedBar(active: Boolean, delayFactor: Int) {
    // Definimos los rangos de altura
    val minHeight = 6.dp
    val maxHeight = 12.dp
    val transition = rememberInfiniteTransition(label = "WaveTransition")

    // Animamos la altura de forma cíclica (sube y baja)
    val animatedHeight by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing, delayMillis = delayFactor * 100),
                                           repeatMode = RepeatMode.Reverse,
        ),
        label = "BarHeightAnimation"
    )

    // Calculamos la altura Dp interpolando entre minHeight y maxHeight
    val heightDp: Dp = if (active) {
        // Interpolamos de 0f a 1f para ir de minHeight a maxHeight y viceversa
        minHeight + (maxHeight - minHeight) * animatedHeight
    } else {
        // Si no está activo, la altura es estática y corta
        minHeight
    }

    Box(
        modifier = Modifier
        .width(3.dp)
        .height(heightDp)
        .background(
            Color.White.copy(
                alpha = if (active) 1f else 0.3f // Opacidad completa si está activo
            ),
            RoundedCornerShape(2.dp)
        )
    )
}

@Composable
fun IdleContent() {
    val size by animateDpAsState(
        targetValue = 8.dp, // Círculo super pequeño
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier
        .size(size)
        .background(Color.Black, CircleShape) // Círculo negro
    )
}


// Función para formatear el tiempo - MEJORADA
private fun formatTime(millis: Long): String {
    if (millis <= 0) return "0:00"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)

        return String.format("%d:%02d", minutes, seconds)
}
