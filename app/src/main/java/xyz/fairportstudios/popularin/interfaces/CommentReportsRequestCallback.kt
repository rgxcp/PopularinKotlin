package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Report

interface CommentReportsRequestCallback {
    fun onSuccess(totalPage: Int, reports: List<Report>)
    fun onNotFound()
    fun onError(message: String)
}