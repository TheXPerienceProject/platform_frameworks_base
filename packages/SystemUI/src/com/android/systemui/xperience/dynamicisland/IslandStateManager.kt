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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton encargado de mantener y distribuir el estado actual de la Dynamic Island.
 * Esta es la fuente de verdad para la interfaz de usuario (Compose) en el overlay.
 */
object IslandStateManager {

    // El estado inicial: la isla está inactiva y vacía.
    private val _islandData = MutableStateFlow(IslandData())

    // El StateFlow público al que se suscribirá el Composable.
    val islandData: StateFlow<IslandData> = _islandData.asStateFlow()

    /**
     * Actualiza el estado de la isla con una nueva copia de IslandData.
     * Esto notifica automáticamente a todos los Composable que lo están observando.
     */
    fun updateState(data: IslandData) {
        // Usamos .update para asegurar que la actualización se haga de forma segura y atómica.
        _islandData.update { data }
    }

    /**
     * Función conveniente para actualizar solo una parte del estado (por ejemplo, solo el título),
     * manteniendo el resto de los datos (artista, estado, etc.) intactos.
     *
     * @param block Una lambda que toma el IslandData actual y devuelve el IslandData modificado.
     */
    fun updatePartialState(block: (IslandData) -> IslandData) {
        _islandData.update(block)
    }

    /**
     * Reinicia la isla a su estado inactivo por defecto.
     */
    fun resetToIdle() {
        _islandData.update { IslandData(state = IslandState.Idle) }
    }
}
