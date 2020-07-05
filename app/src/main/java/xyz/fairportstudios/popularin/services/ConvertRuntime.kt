package xyz.fairportstudios.popularin.services

object ConvertRuntime {
    fun getRuntimeForHumans(runtime: Int): String {
        val hour: Int = runtime / 60
        val minute: Int = runtime % 60

        return "$hour jam $minute menit"
    }
}