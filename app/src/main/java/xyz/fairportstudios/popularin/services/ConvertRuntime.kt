package xyz.fairportstudios.popularin.services

object ConvertRuntime {
    fun getRuntimeForHumans(runtime: Int): String {
        val hour = runtime / 60
        val minute = runtime % 60
        return "$hour jam $minute menit"
    }
}