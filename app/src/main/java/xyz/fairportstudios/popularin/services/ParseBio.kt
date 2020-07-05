package xyz.fairportstudios.popularin.services

import xyz.fairportstudios.popularin.models.CreditDetail
import java.util.*

object ParseBio {
    fun getBioForHumans(creditDetail: CreditDetail): String {
        val name: String = creditDetail.name
        val department: String = creditDetail.knownForDepartment
        val dob: String = creditDetail.birthday
        var pob: String = creditDetail.placeOfBirth
        var dateForHumans: String = ParseDate.getDateForHumans(dob)

        if (dateForHumans == "Tanpa Tahun") {
            dateForHumans = "yang belum diketahui"
        }
        if (pob.length == 4) {
            pob = "tempat yang belum diketahui"
        }

        return "$name adalah seorang yang dikenal dalam bidang ${department.toLowerCase(Locale.ROOT)} yang lahir pada tanggal $dateForHumans di $pob."
    }
}