package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.model.api.provider.DbRecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore

class RecentlyBrowsedPageFragment: BaseMovieListPageFragment<BaseMovieListPage, RecentlyBrowsedPagePresenter>() {
    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.recently_browsed_page_title
    }

    override fun createPresenter(): RecentlyBrowsedPagePresenter {
        return RecentlyBrowsedPagePresenter(DbRecentlyBrowsedMovieProvider(MovieRatingsApplication.database.movieDao()),
                DbLikeStore(MovieRatingsApplication.database.favDao()))
    }

    class RecentlyBrowsedPath: RouterPath<RecentlyBrowsedPageFragment>() {
        override fun createFragmentInstance(): RecentlyBrowsedPageFragment {
            return RecentlyBrowsedPageFragment()
        }
    }
}