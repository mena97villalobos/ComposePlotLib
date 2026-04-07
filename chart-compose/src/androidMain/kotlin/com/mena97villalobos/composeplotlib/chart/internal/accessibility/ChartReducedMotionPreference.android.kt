package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Maps Android "Remove animations" / zeroed animation scales to reduced motion for charts.
 * Reads [Settings.Global] each composition so changes are picked up on recomposition.
 */
@Composable
actual fun chartReducedMotionPreferred(): Boolean {
    val context = LocalContext.current

    fun scale(key: String): Float = Settings.Global.getFloat(context.contentResolver, key, 1f)
    return scale(Settings.Global.TRANSITION_ANIMATION_SCALE) == 0f ||
        scale(Settings.Global.WINDOW_ANIMATION_SCALE) == 0f ||
        scale(Settings.Global.ANIMATOR_DURATION_SCALE) == 0f
}
