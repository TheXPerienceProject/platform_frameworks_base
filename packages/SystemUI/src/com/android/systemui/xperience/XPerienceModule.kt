/*
 * Copyright (C) 2023 The LineageOS Project
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

package com.android.systemui.xperience

import com.android.systemui.qs.QsEventLogger
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.shared.model.TileCategory
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.tiles.CompassTile
import com.android.systemui.qs.tiles.CPUInfoTile
import com.android.systemui.qs.tiles.OnTheGoTile
import com.android.systemui.qs.tiles.PowerShareTile
import com.android.systemui.qs.tiles.NfcTile
import com.android.systemui.qs.tiles.WeatherTile
import com.android.systemui.qs.tiles.base.shared.model.QSTileConfig;
import com.android.systemui.qs.tiles.base.shared.model.QSTilePolicy;
import com.android.systemui.qs.tiles.base.shared.model.QSTileUIConfig;
import com.android.systemui.res.R

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
interface XPerienceModule {

    /** Inject OnTheGoTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(OnTheGoTile.TILE_SPEC)
    fun bindOnTheGoTile(onTheGoTile: OnTheGoTile): QSTileImpl<*>

    /** Inject CPUInfoTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(CPUInfoTile.TILE_SPEC)
    fun CPUInfoTile(cpuInfoTile: CPUInfoTile): QSTileImpl<*>

    /** Inject CompassTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(CompassTile.TILE_SPEC)
    fun bindCompassTile(compassTile: CompassTile): QSTileImpl<*>

    /** Inject PowerShareTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(PowerShareTile.TILE_SPEC)
    fun bindPowerShareTile(powerShareTile: PowerShareTile): QSTileImpl<*>

    /** Inject WeatherTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(WeatherTile.TILE_SPEC)
    fun bindWeatherTile(weatherTile: WeatherTile): QSTileImpl<*>

    companion object {
        @Provides
        @IntoMap
        @StringKey(NfcTile.TILE_SPEC)
        fun provideNfcConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(NfcTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_nfc,
                    labelRes = R.string.quick_settings_nfc_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.CONNECTIVITY
            )
        }

       @Provides
        @IntoMap
        @StringKey(CPUInfoTile.TILE_SPEC)
        fun provideCPUInfoConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(CPUInfoTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_cpu_info,
                    labelRes = R.string.quick_settings_cpuinfo_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.UTILITIES
            )
        }

       @Provides
        @IntoMap
        @StringKey(CompassTile.TILE_SPEC)
        fun provideCompassConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(CompassTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_compass,
                    labelRes = R.string.quick_settings_compass_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.UTILITIES
            )
        }

       @Provides
        @IntoMap
        @StringKey(PowerShareTile.TILE_SPEC)
        fun providePowerShareConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(PowerShareTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_powershare,
                    labelRes = R.string.quick_settings_powershare_enabled_label  
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.UTILITIES
            )
        }

    }
}
