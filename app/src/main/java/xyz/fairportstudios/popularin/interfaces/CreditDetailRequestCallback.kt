package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film

interface CreditDetailRequestCallback {
    fun onSuccess(creditDetail: CreditDetail, filmAsCastList: ArrayList<Film>, filmAsCrewList: ArrayList<Film>)
    fun onError(message: String)
}