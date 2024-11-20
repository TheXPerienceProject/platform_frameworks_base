/*
 * Copyright (C) 2024 the risingOS Android Project
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
package com.android.systemui.qs;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

public class TileUtils {
   
   public static boolean isCompactQSMediaPlayerEnforced(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(), 
            "qs_compact_media_player_mode",0, UserHandle.USER_CURRENT) != 0;
   }
}
