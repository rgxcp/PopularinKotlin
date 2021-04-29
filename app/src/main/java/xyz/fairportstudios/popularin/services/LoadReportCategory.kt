package xyz.fairportstudios.popularin.services

import android.content.Context
import xyz.fairportstudios.popularin.R

object LoadReportCategory {
    fun getAllReportCategory(context: Context): Array<String> {
        return arrayOf(
            context.getString(R.string.report_category_1),
            context.getString(R.string.report_category_2),
            context.getString(R.string.report_category_3),
            context.getString(R.string.report_category_4),
            context.getString(R.string.report_category_5)
        )
    }
}