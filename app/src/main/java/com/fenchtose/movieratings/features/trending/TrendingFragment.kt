package com.fenchtose.movieratings.features.trending

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragmentRedux
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageState
import com.fenchtose.movieratings.widgets.IndicatorTabLayout

class TrendingFragment: BaseMovieListPageFragmentRedux() {

    private var tabLayout: IndicatorTabLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tabs)
        tabLayout?.let {
            it.addTab(createTab(R.string.trending_tab_today))
            it.addTab(createTab(R.string.trending_tab_week))

            it.addListener {
                val tab = when(it) {
                    0 -> TrendingTab.DAY
                    1 -> TrendingTab.WEEK
                    else -> TrendingTab.DAY
                }

                GaEvents.SELECT_TRENDING_TAB.withLabelArg(tab.key).track()
                dispatch?.invoke(SwitchTab(tab))
            }
        }
    }

    private fun createTab(@StringRes resId: Int): IndicatorTabLayout.Tab {
        val view = LayoutInflater.from(context).inflate(R.layout.tab_item_layout, null).apply { (this as TextView).setText(resId) }
        return IndicatorTabLayout.Tab(view)
    }

    override fun getErrorContent() = R.string.trending_page_error_content
    override fun getEmptyContent() = R.string.trending_page_empty_content
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.trending_screen_title
    override fun screenName() = GaScreens.TRENDING
    override fun getLayout() = R.layout.trending_movies_page_layout
    override fun loadingAction() = LoadTrendingPage

    override fun reduceState(appState: AppState): BaseMovieListPageState {
        return BaseMovieListPageState(appState.trendingPage.movies, appState.trendingPage.progress)
    }

    override fun render(appState: AppState, dispatch: Dispatch) {
        tabLayout?.selectTab(when(appState.trendingPage.currentTab) {
            TrendingTab.DAY -> 0
            TrendingTab.WEEK -> 1
        })
    }

}

class TrendingPath: RouterPath<TrendingFragment>() {
    override fun createFragmentInstance() = TrendingFragment()
    override fun category() = GaCategory.TRENDING
    override fun toolbarElevation() = R.dimen.toolbar_no_elevation
    override fun clearAction() = ClearTrendingPage
}