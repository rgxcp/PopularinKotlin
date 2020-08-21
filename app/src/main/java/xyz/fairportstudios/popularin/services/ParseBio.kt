package xyz.fairportstudios.popularin.services

import xyz.fairportstudios.popularin.models.CreditDetail
import java.util.Locale

object ParseBio {
    fun getBioForHumans(creditDetail: CreditDetail): String {
        var pob = creditDetail.placeOfBirth
        var dfh = ParseDate.getDateForHumans(creditDetail.birthday)
        if (pob.length == 4) {
            pob = "tempat yang belum diketahui"
        }
        if (dfh == "Tanpa Tahun") {
            dfh = "yang belum diketahui"
        }
        return "${creditDetail.name} adalah seorang yang dikenal dalam bidang ${creditDetail.knownForDepartment.toLowerCase(Locale.ROOT)} yang lahir pada tanggal $dfh di $pob."
    }
}