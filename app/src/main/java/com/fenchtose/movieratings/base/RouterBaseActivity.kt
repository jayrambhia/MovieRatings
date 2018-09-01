package com.fenchtose.movieratings.base

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Unsubscribe
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.info.AppInfoFragment
import com.fenchtose.movieratings.features.likespage.LikesPageFragment
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPageFragment
import com.fenchtose.movieratings.features.recentlybrowsedpage.RecentlyBrowsedPageFragment
import com.fenchtose.movieratings.features.searchpage.SearchPageFragment
import com.fenchtose.movieratings.features.settings.SettingsFragment
import com.fenchtose.movieratings.features.trending.TrendingPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences

abstract class RouterBaseActivity: AppCompatActivity() {

    private var router: Router? = null
    private var visibleMenuItems: IntArray? = null

    private var dispatch: Dispatch? = null
    private var unsubscribe: Unsubscribe? = null

    protected fun initializeRouter(toolbar: Toolbar? = null,
                                   onMovedTo: (RouterPath<out BaseFragment>) -> Unit = {},
                                   onRemoved: (RouterPath<out BaseFragment>) -> Unit = {},
                                   onInit: (Router) -> Unit = {}) {
        toolbar?.let {
            setSupportActionBar(it)
        }

        router = Router(this,
                {
                    visibleMenuItems = it.showMenuIcons()
                    invalidateOptionsMenu()
                    onMovedTo(it)
                }, { onRemoved(it) }
        )

        onInit(router!!)
    }

    override fun onBackPressed() {
        if (router?.onBackRequested() == false) {
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        unsubscribe = MovieRatingsApplication.store.subscribe { _, dispatch -> this.dispatch = dispatch }
    }

    override fun onPause() {
        super.onPause()
        unsubscribe?.invoke()
    }

    override fun onDestroy() {
        super.onDestroy()
        router = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        visibleMenuItems?.let {
            val pathIcons = it
            menu?.let {
                (0 until it.size())
                        .map { i -> it.getItem(i) }
                        .forEach { it.isVisible = it.itemId in pathIcons }
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_search -> showSearchPage()
            R.id.action_settings -> showSettingsPage()
            R.id.action_fav -> showFavoritesPage()
            R.id.action_info -> showInfoPage(false)
            R.id.action_history -> showRecentlyBrowsedPage()
            R.id.action_collection -> showMovieCollectionsPage()
            R.id.action_trending -> showTrendingPage()
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    private fun showSearchPage() {
        router?.let {
            dispatch?.invoke(Navigation(it, SearchPageFragment.SearchPath.Default(SettingsPreferences(this))))
        }
    }

    private fun showInfoPage(showSearchOption: Boolean) {
        GaEvents.OPEN_INFO_PAGE.track()
        router?.go(AppInfoFragment.AppInfoPath(showSearchOption))
    }

    private fun showSettingsPage() {
        GaEvents.OPEN_SETTINGS.withCategory("menu").track()
        router?.go(SettingsFragment.SettingsPath())
    }

    private fun showFavoritesPage() {
        GaEvents.OPEN_LIKED_PAGE.track()
        router?.go(LikesPageFragment.LikesPath())
    }

    private fun showRecentlyBrowsedPage() {
        GaEvents.OPEN_RECENTLY_BROWSED_PAGE.track()
        router?.go(RecentlyBrowsedPageFragment.RecentlyBrowsedPath())
    }

    private fun showMovieCollectionsPage() {
        router?.let {
            dispatch?.invoke(Navigation(it, CollectionListPageFragment.CollectionListPagePath(false)))
        }
        GaEvents.OPEN_COLLECTIONS_PAGE.track()
    }

    private fun showTrendingPage() {
        router?.go(TrendingPath())
    }

    protected fun getRouter(): Router? = router

}