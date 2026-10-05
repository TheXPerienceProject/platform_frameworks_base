/*
 * Copyright (C) 2022 The Nameless-AOSP Project
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.os;

import com.oplus.os.WaveformEffect;

/** @hide */
interface ILinearmotorVibratorService {
    void vibrate(in WaveformEffect effect);
    void cancelVibrate(in WaveformEffect effect);
}
