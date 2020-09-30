package xyz.fairportstudios.popularin.models

import xyz.fairportstudios.popularin.enums.PointType

data class Point(
    val id: Int,
    val typeID: Int,
    val isPositive: Boolean,
    val type: Enum<PointType>,
    val total: String,
    val description: String,
    val timestamp: String
)