package xyz.fairportstudios.popularin.services

import xyz.fairportstudios.popularin.R

object ConvertRating {
    fun getStar(rating: Double): Int? {
        val stars = HashMap<Double, Int>()
        stars[0.5] = R.drawable.ic_star_05
        stars[1.0] = R.drawable.ic_star_10
        stars[1.5] = R.drawable.ic_star_15
        stars[2.0] = R.drawable.ic_star_20
        stars[2.5] = R.drawable.ic_star_25
        stars[3.0] = R.drawable.ic_star_30
        stars[3.5] = R.drawable.ic_star_35
        stars[4.0] = R.drawable.ic_star_40
        stars[4.5] = R.drawable.ic_star_45
        stars[5.0] = R.drawable.ic_star_50
        return stars[rating]
    }
}