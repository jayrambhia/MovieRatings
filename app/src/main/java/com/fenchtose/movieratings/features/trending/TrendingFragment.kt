package com.fenchtose.movieratings.features.trending

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.util.AppRxHooks

class TrendingFragment: BaseMovieListPageFragment<TrendingFragment, TrendingPresenter>() {
    override fun createPresenter(): TrendingPresenter {
        val likeStore = DbLikeStore.getInstance(MovieRatingsApplication.database.favDao())

        return TrendingPresenter(
                AppRxHooks(),
                MovieRatingsApplication.ratingProviderModule.ratingProvider,
                MovieRatingsApplication.database.movieDao(),
                likeStore,
                setOf(likeStore),
                path?.getRouter()
        )
    }

    override fun getErrorContent() = R.string.trending_page_error_content
    override fun getEmptyContent() = R.string.trending_page_empty_content
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.trending_screen_title

}

class TrendingPath: RouterPath<TrendingFragment>() {
    override fun createFragmentInstance(): TrendingFragment {
        return TrendingFragment()
    }
}