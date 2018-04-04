package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.di.DependencyProvider
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.model.api.provider.DbRecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore

class RecentlyBrowsedPageFragment: BaseMovieListPageFragment<BaseMovieListPage, RecentlyBrowsedPagePresenter>() {
    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.recently_browsed_page_title

    override fun getEmptyContent() = R.string.recently_browsed_page_empty_content

    override fun getErrorContent() = R.string.recently_browsed_page_error_content

    override fun createPresenter(): RecentlyBrowsedPagePresenter? {
        DependencyProvider.di()?.let {
            it.database?.run {
                return RecentlyBrowsedPagePresenter(DbRecentlyBrowsedMovieProvider(movieDao()),
                        DbLikeStore(favDao()), it.router)
            }

        }

        return null
    }

    class RecentlyBrowsedPath: RouterPath<RecentlyBrowsedPageFragment>() {
        override fun createFragmentInstance(): RecentlyBrowsedPageFragment {
            return RecentlyBrowsedPageFragment()
        }
    }
}