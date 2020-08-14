package xyz.fairportstudios.popularin.services

object ParseDate {
    fun getDateForHumans(date: String): String {
        val months = HashMap<String, String>()
        months["01"] = "Januari"
        months["02"] = "Februari"
        months["03"] = "Maret"
        months["04"] = "April"
        months["05"] = "Mei"
        months["06"] = "Juni"
        months["07"] = "Juli"
        months["08"] = "Agustus"
        months["09"] = "September"
        months["10"] = "Oktober"
        months["11"] = "November"
        months["12"] = "Desember"
        return try {
            val day = date.substring(8, 10)
            val month = date.substring(5, 7)
            val year = date.substring(0, 4)
            "$day ${months[month]} $year"
        } catch (exception: StringIndexOutOfBoundsException) {
            "Tanpa Tahun"
        }
    }

    fun getDay(date: String): String {
        return try {
            date.substring(8, 10)
        } catch (exception: StringIndexOutOfBoundsException) {
            return "01"
        }
    }

    fun getMonth(date: String): String {
        return try {
            date.substring(5, 7)
        } catch (exception: StringIndexOutOfBoundsException) {
            return "01"
        }
    }

    fun getYear(date: String): String {
        return try {
            date.substring(0, 4)
        } catch (exception: StringIndexOutOfBoundsException) {
            return "2020"
        }
    }
}