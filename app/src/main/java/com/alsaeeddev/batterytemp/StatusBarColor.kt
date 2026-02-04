package com.alsaeeddev.batterytemp

import android.app.Activity
import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb


@Composable
fun StatusBarColor(
    color: Color,
    darkIcons: Boolean
) {
    val view = LocalView.current


    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // API 26+ uses Compose Color.toArgb()
                window.statusBarColor = color.toArgb()
            } else {
                // API 24-25 fallback: manually convert Compose Color to ARGB int
                window.statusBarColor =
                    (color.alpha * 255).toInt() shl 24 or
                            (color.red * 255).toInt() shl 16 or
                            (color.green * 255).toInt() shl 8 or
                            (color.blue * 255).toInt()
            }


            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = darkIcons
        }
    }
}
