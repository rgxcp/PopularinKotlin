package xyz.fairportstudios.popularin.services

import android.content.Context

object ConvertPixel {
    fun getDensity(context: Context, px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }
}