/*
 * Copyright (C) 2011-2025 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

public class RefreshRateIconGenerator {
    
    private static final int ICON_SIZE_DP = 24;
    private static final int TEXT_SIZE_DP = 10;
    private static final int CIRCLE_STROKE_DP = 2;
    
    public static Bitmap generateRefreshRateIcon(Context context, int refreshRate) {
        final float density = context.getResources().getDisplayMetrics().density;
        final int iconSize = (int) (ICON_SIZE_DP * density);
        final int textSize = (int) (TEXT_SIZE_DP * density);
        final int circleStroke = (int) (CIRCLE_STROKE_DP * density);
        
        Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Fondo transparente
        canvas.drawColor(Color.TRANSPARENT);
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Determinar el color basado en el modo oscuro/claro
        int textColor = isDarkMode(context) ? Color.WHITE : Color.BLACK;
        int circleColor = isDarkMode(context) ? Color.WHITE : Color.BLACK;
        
        // Dibujar círculo exterior
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(circleStroke);
        paint.setColor(circleColor);
        
        float circlePadding = circleStroke / 2f;
        RectF circleRect = new RectF(circlePadding, circlePadding, 
                                   iconSize - circlePadding, iconSize - circlePadding);
        canvas.drawOval(circleRect, paint);
        
        // Dibujar texto
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(textColor);
        
        String text = String.valueOf(refreshRate);
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);
        
        float x = iconSize / 2f;
        float y = (iconSize - textBounds.height()) / 2f - textBounds.top;
        
        canvas.drawText(text, x, y, paint);
        
        return bitmap;
    }
    
    /**
     * Detecta si el dispositivo está en modo oscuro
     */
    private static boolean isDarkMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & 
                            Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}