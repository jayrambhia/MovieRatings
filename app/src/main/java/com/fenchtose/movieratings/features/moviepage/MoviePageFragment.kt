package com.fenchtose.movieratings.features.moviepage

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.image.ImageLoader

class MoviePageFragment: BaseFragment() {

    var movie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val imageLoader = GlideLoader(Glide.with(this))
        val imageView: ImageView = view.findViewById(R.id.poster_view)
        val titleView: TextView = view.findViewById(R.id.title_view)
        val genreView: TextView = view.findViewById(R.id.genre_view)
        val ratingView: TextView = view.findViewById(R.id.rating_view)

        val rParams = ratingView.layoutParams as CoordinatorLayout.LayoutParams
        rParams.behavior = RatingBehavior(context)
        ratingView.layoutParams = rParams

        val handler = Handler()

        movie?.let {

            path?.getSharedTransitionElement()?.let {
                ViewCompat.setTransitionName(imageView, it.second)
            }

            imageLoader.loadImage(it.poster, imageView, object : ImageLoader.Callback {
                override fun imageLoaded(image: String, view: ImageView) {
                    handler.postDelayed({
                        startPostponedEnterTransition()
                        val params = imageView.layoutParams as CoordinatorLayout.LayoutParams
                        params.behavior = PosterBehavior()
                        imageView.layoutParams = params

                    } , 60)

                }
            })

            titleView.text = it.title
            genreView.text = it.genre
            ratingView.text = if (it.ratings.size > 0) it.ratings[0].value else "8.1/10"
        }
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.movie_page_title
    }

    class MoviePath(val movie: Movie, sharedView: View?, viewName: String?): RouterPath<MoviePageFragment>() {
        private val sharedElement: Pair<View, String>? = if (sharedView != null && viewName != null) Pair(sharedView, viewName) else null

        override fun createFragmentInstance(): MoviePageFragment {
            val fragment = MoviePageFragment()
            fragment.movie = movie
            return fragment
        }

        override fun getSharedTransitionElement(): Pair<View, String>? {
            return sharedElement
        }
    }
}