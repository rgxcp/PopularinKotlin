package xyz.fairportstudios.popularin.services

object ConvertGenre {
    fun getGenreForHumans(id: Int): String? {
        val genres: HashMap<Int, String> = HashMap()
        genres[0] = "Tanpa Genre"
        genres[12] = "Petualangan"
        genres[14] = "Fantasi"
        genres[16] = "Animasi"
        genres[18] = "Drama"
        genres[27] = "Horor"
        genres[28] = "Aksi"
        genres[35] = "Komedi"
        genres[36] = "Sejarah"
        genres[37] = "Barat"
        genres[53] = "Thriller"
        genres[80] = "Kejahatan"
        genres[99] = "Dokumenter"
        genres[878] = "Fiksi"
        genres[9648] = "Misteri"
        genres[10402] = "Musik"
        genres[10749] = "Romansa"
        genres[10751] = "Keluarga"
        genres[10752] = "Perang"
        genres[10770] = "Serial TV"

        return genres[id]
    }
}