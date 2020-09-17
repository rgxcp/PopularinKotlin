package xyz.fairportstudios.popularin.services

import android.content.Context
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Genre

object LoadGenre {
    fun getAllGenre(context: Context, genreList: ArrayList<Genre>) {
        genreList.add(Genre(28, R.drawable.img_action, context.getString(R.string.genre_action)))
        genreList.add(Genre(16, R.drawable.img_animation, context.getString(R.string.genre_animation)))
        genreList.add(Genre(99, R.drawable.img_documentary, context.getString(R.string.genre_documentary)))
        genreList.add(Genre(18, R.drawable.img_drama, context.getString(R.string.genre_drama)))
        genreList.add(Genre(14, R.drawable.img_fantasy, context.getString(R.string.genre_fantasy)))
        genreList.add(Genre(878, R.drawable.img_fiction, context.getString(R.string.genre_fiction)))
        genreList.add(Genre(27, R.drawable.img_horror, context.getString(R.string.genre_horror)))
        genreList.add(Genre(80, R.drawable.img_crime, context.getString(R.string.genre_crime)))
        genreList.add(Genre(10751, R.drawable.img_family, context.getString(R.string.genre_family)))
        genreList.add(Genre(35, R.drawable.img_comedy, context.getString(R.string.genre_comedy)))
        genreList.add(Genre(9648, R.drawable.img_mystery, context.getString(R.string.genre_mystery)))
        genreList.add(Genre(10752, R.drawable.img_war, context.getString(R.string.genre_war)))
        genreList.add(Genre(12, R.drawable.img_adventure, context.getString(R.string.genre_adventure)))
        genreList.add(Genre(10749, R.drawable.img_romance, context.getString(R.string.genre_romance)))
        genreList.add(Genre(36, R.drawable.img_history, context.getString(R.string.genre_history)))
        genreList.add(Genre(53, R.drawable.img_thriller, context.getString(R.string.genre_thriller)))
    }
}