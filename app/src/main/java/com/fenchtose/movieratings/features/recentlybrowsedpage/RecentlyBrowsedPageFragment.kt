package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.model.api.provider.DbRecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.util.AppRxHooks

class RecentlyBrowsedPageFragment: BaseMovieListPageFragment<BaseMovieListPage, RecentlyBrowsedPagePresenter>() {
    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.recently_browsed_page_title

    override fun getEmptyContent() = R.string.recently_browsed_page_empty_content

    override fun getErrorContent() = R.string.recently_browsed_page_error_content

    override fun createPresenter(): RecentlyBrowsedPagePresenter {
        return RecentlyBrowsedPagePresenter(
                AppRxHooks(),
                DbRecentlyBrowsedMovieProvider(MovieRatingsApplication.database.movieDao()),
                DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()))
    }

    class RecentlyBrowsedPath: RouterPath<RecentlyBrowsedPageFragment>() {
        override fun createFragmentInstance(): RecentlyBrowsedPageFragment {
            return RecentlyBrowsedPageFragment()
        }
    }
}