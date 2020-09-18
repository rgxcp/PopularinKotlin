package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.FilmMetadata

interface FilmMetadataRequestCallback {
    fun onSuccess(filmMetadata: FilmMetadata)
    fun onNotFound()
    fun onError(message: String)
}