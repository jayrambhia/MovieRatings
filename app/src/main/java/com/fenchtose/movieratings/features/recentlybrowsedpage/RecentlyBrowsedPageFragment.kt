package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageState

class RecentlyBrowsedPageFragment: BaseMovieListPageFragment() {

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.recently_browsed_page_title

    override fun screenName() = AppScreens.RECENTLY_BROWSED

    override fun getEmptyContent() = R.string.recently_browsed_page_empty_content

    override fun getErrorContent() = R.string.recently_browsed_page_error_content

    override fun reduceState(appState: AppState): BaseMovieListPageState {
        return BaseMovieListPageState(appState.recentlyBrowsedPage.movies, appState.recentlyBrowsedPage.progress)
    }

    override fun loadingAction() = LoadRecentlyBrowsedMovies

    class RecentlyBrowsedPath: RouterPath<RecentlyBrowsedPageFragment>() {
        override fun createFragmentInstance() = RecentlyBrowsedPageFragment()
        override fun category() = GaCategory.RECENTLY_BROWSED
        override fun clearAction() = ClearRecentlyBrowsedState
    }
}