package com.fenchtose.movieratings.features.trending

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageState

class TrendingFragment: BaseMovieListPageFragment() {

    override fun getErrorContent() = R.string.trending_page_error_content
    override fun getEmptyContent() = R.string.trending_page_empty_content
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.trending_screen_title
    override fun screenName() = AppScreens.TRENDING
    override fun loadingAction() = LoadTrendingPage

    override fun reduceState(appState: AppState): BaseMovieListPageState {
        return BaseMovieListPageState(appState.trendingPage.movies, appState.trendingPage.progress)
    }

    override fun render(appState: AppState, dispatch: Dispatch) {

    }

    @Composable
    override fun Header(appState: AppState, dispatch: Dispatch) {
        val selectedIndex = when(appState.trendingPage.currentTab) {
            TrendingTab.DAY -> 0
            TrendingTab.WEEK -> 1
        }

        TabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier
                .requiredHeight(48.dp)
                .fillMaxWidth(),
            backgroundColor = colorResource(id = R.color.colorPrimary),
            tabs = {
                TrendingTab(selected = selectedIndex == 0, type = TrendingTab.DAY, dispatch = dispatch)
                TrendingTab(selected = selectedIndex == 1, type = TrendingTab.WEEK, dispatch = dispatch)
            }
        )
    }

    @Composable
    fun TrendingTab(selected: Boolean, type: TrendingTab, dispatch: Dispatch) {
        Tab(selected = selected, onClick = { dispatch(SwitchTab(type)) }) {
            Text(text = stringResource(id = type.title))
        }
    }
}

class TrendingPath: RouterPath<TrendingFragment>() {
    override fun createFragmentInstance() = TrendingFragment()
    override fun category() = GaCategory.TRENDING
    override fun toolbarElevation() = R.dimen.toolbar_no_elevation
    override fun clearAction() = ClearTrendingPage
}