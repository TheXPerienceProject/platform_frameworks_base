/*
 * Copyright (C) 2011-2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/** Generates the compact refresh-rate glyph used in the status bar. */
public final class RefreshRateIconGenerator {
    private static final float ICON_HEIGHT_DP = 17f;
    private static final float DIGIT_TEXT_SIZE_DP = 10.5f;
    private static final float SUFFIX_TEXT_SIZE_DP = 6.5f;
    private static final float HORIZONTAL_PADDING_DP = 1.5f;
    private static final float TEXT_GAP_DP = 0.7f;
    private static final float SUFFIX_RAISE_DP = 1.6f;

    private RefreshRateIconGenerator() {}

    public static Bitmap generateRefreshRateIcon(Context context, int refreshRate) {
        final float density = context.getResources().getDisplayMetrics().density;
        final int height = Math.max(1, Math.round(ICON_HEIGHT_DP * density));
        final float horizontalPadding = HORIZONTAL_PADDING_DP * density;
        final float gap = TEXT_GAP_DP * density;

        final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        valuePaint.setColor(Color.WHITE);
        valuePaint.setTextSize(DIGIT_TEXT_SIZE_DP * density);
        valuePaint.setTypeface(Typeface.create(getSystemFontFamily(context), Typeface.BOLD));

        final Paint suffixPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        suffixPaint.setColor(Color.WHITE);
        suffixPaint.setTextSize(SUFFIX_TEXT_SIZE_DP * density);
        suffixPaint.setTypeface(Typeface.create(getSystemFontFamily(context), Typeface.BOLD));

        final String value = Integer.toString(refreshRate);
        final String suffix = "Hz";
        final float valueWidth = valuePaint.measureText(value);
        final float suffixWidth = suffixPaint.measureText(suffix);
        final int width = Math.max(1, Math.round(
                horizontalPadding * 2f + valueWidth + gap + suffixWidth));

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        final Paint.FontMetrics valueMetrics = valuePaint.getFontMetrics();
        final float baseline = height / 2f
                - (valueMetrics.ascent + valueMetrics.descent) / 2f;

        float x = horizontalPadding;
        canvas.drawText(value, x, baseline, valuePaint);
        x += valueWidth + gap;
        canvas.drawText(suffix, x, baseline - SUFFIX_RAISE_DP * density, suffixPaint);

        return bitmap;
    }

    private static String getSystemFontFamily(Context context) {
        final int resId = context.getResources().getIdentifier(
                "config_bodyFontFamily", "string", "android");
        if (resId != 0) {
            final String family = context.getString(resId);
            if (family != null && !family.isEmpty()) {
                return family;
            }
        }
        return "sans-serif";
    }
}
