package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Point

interface UserPointRequestCallback {
    fun onSuccess(totalPage: Int, pointList: ArrayList<Point>)
    fun onNotFound()
    fun onError(message: String)
}