package xyz.fairportstudios.popularin.adapters

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import xyz.fairportstudios.popularin.services.ConvertGenre
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.services.ConvertRuntime
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.TMDbAPI

@BindingAdapter("app:profilePictureURL")
fun loadUserProfile(imageView: ImageView, imageURL: String?) {
    Glide.with(imageView.context).load(imageURL).into(imageView)
}

@BindingAdapter("app:smallTMDbPictureURL")
fun loadSmallTMDbPicture(imageView: ImageView, imageURL: String?) {
    val parsedURL = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${imageURL}"
    Glide.with(imageView.context).load(parsedURL).into(imageView)
}

@BindingAdapter("app:largeTMDbPictureURL")
fun loadLargeTMDbPicture(imageView: ImageView, imageURL: String?) {
    val parsedURL = "${TMDbAPI.BASE_LARGE_IMAGE_URL}${imageURL}"
    Glide.with(imageView.context).load(parsedURL).into(imageView)
}

@BindingAdapter("app:genreBackground")
fun loadGenreBackground(imageView: ImageView, background: Int) {
    imageView.setImageResource(background)
}

@BindingAdapter("app:star")
fun loadStarImage(imageView: ImageView, rating: Double) {
    ConvertRating.getStar(rating)?.let { imageView.setImageResource(it) }
}

@BindingAdapter("app:dateForHumans")
fun loadDate(textView: TextView, date: String?) {
    textView.text = date?.let { ParseDate.getDateForHumans(it) }
}

@BindingAdapter("app:genreForHumans")
fun loadGenre(view: View, id: Int) {
    when (view) {
        is Chip -> view.text = ConvertGenre.getGenreForHumans(id)
        is TextView -> view.text = ConvertGenre.getGenreForHumans(id)
    }
}

@BindingAdapter("app:runtimeForHumans")
fun loadRuntime(textView: TextView, runtime: Int) {
    textView.text = ConvertRuntime.getRuntimeForHumans(runtime)
}

@BindingAdapter("app:rating")
fun loadRating(ratingBar: RatingBar, rating: Double) {
    ratingBar.rating = rating.toFloat()
}