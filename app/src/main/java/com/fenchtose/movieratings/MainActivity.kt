package com.fenchtose.movieratings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.accessinfo.AccessInfoFragment
import com.fenchtose.movieratings.features.info.AppInfoFragment
import com.fenchtose.movieratings.features.likespage.LikesPageFragment
import com.fenchtose.movieratings.features.searchpage.SearchPageFragment
import com.fenchtose.movieratings.features.settings.SettingsFragment
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.IntentUtils
import com.fenchtose.movieratings.util.PackageUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject


class MainActivity : AppCompatActivity() {

    private var container: FrameLayout? = null
    private var activateButton: ViewGroup? = null
    private var toolbar: Toolbar? = null
    private var titlebar: ActionBar? = null

    private var router: Router? = null

    private var accessibilityPublisher: PublishSubject<Boolean>? = null
    private var accessibilityPagePublisher: PublishSubject<Boolean>? = null
    private var disposable: Disposable? = null

    private var analytics: AnalyticsDispatcher? = null

    private var visibleMenuItems: IntArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.fragment_container)
        activateButton = findViewById(R.id.activate_button)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        titlebar = supportActionBar

        setupObservables()

        router = Router(this)
        MovieRatingsApplication.router = router

        router?.callback = object: Router.RouterCallback {
            override fun movedTo(path: RouterPath<out BaseFragment>) {
                updateMenuItems(path)
                if (path is AccessInfoFragment.AccessibilityPath) {
                    accessibilityPagePublisher?.onNext(true)
                }
            }

            override fun removed(path: RouterPath<out BaseFragment>) {
                if (path is AccessInfoFragment.AccessibilityPath) {
                    accessibilityPagePublisher?.onNext(false)
                }
            }
        }

        activateButton?.setOnClickListener {
            showAccessibilityInfo()
        }

//        showInfoPage()
        showSearchPage()
        accessibilityPagePublisher?.onNext(false)

        analytics = MovieRatingsApplication.analyticsDispatcher
    }

    override fun onResume() {
        super.onResume()
        accessibilityPublisher?.onNext(AccessibilityUtils.hasAllPermissions(this))
    }

    override fun onBackPressed() {
        if (router?.onBackRequested() == false) {
            return
        }

        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityPublisher?.onComplete()
        accessibilityPagePublisher?.onComplete()
        disposable?.dispose()
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
            R.id.action_settings -> showSettingsPage()
            R.id.action_fav -> showFavoritesPage()
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    private fun updateMenuItems(path: RouterPath<out BaseFragment>) {
        this.visibleMenuItems = path.showMenuIcons()
        invalidateOptionsMenu()
    }

    private fun showSearchPage() {
        router?.go(SearchPageFragment.SearchPath())
    }

    private fun showInfoPage() {
        router?.go(AppInfoFragment.AppInfoPath())
    }

    private fun showAccessibilityInfo() {
        analytics?.sendEvent(Event("activate_button_clicked"))
        router?.go(AccessInfoFragment.AccessibilityPath())
    }

    private fun showSettingsPage() {
        router?.go(SettingsFragment.SettingsPath())
    }

    private fun showFavoritesPage() {
        router?.go(LikesPageFragment.LikesPath())
    }

    private fun onAccessibilityActivated() {
        router?.onBackRequested()
        // Show a dialog?
        val builder = AlertDialog.Builder(this)
                .setTitle(R.string.accessibility_enabled_dialog_title)
                .setMessage(R.string.accessibility_enabled_dialog_content)
                .setNeutralButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }

        if (PackageUtils.hasInstalled(this, PackageUtils.NETFLIX)) {
            builder.setPositiveButton(R.string.accessibility_enabled_open_netflix) { dialog, _ ->
                dialog.dismiss()
                IntentUtils.launch3rdParty(this, PackageUtils.NETFLIX)
            }
        }
    }

    private fun setupObservables() {
        accessibilityPublisher = PublishSubject.create()
        accessibilityPagePublisher = PublishSubject.create()

        disposable =
                Observable.combineLatest(accessibilityPublisher, accessibilityPagePublisher,
                BiFunction<Boolean, Boolean, Int> {
                    hasAccessibility, isShowingAccessibilityInfo ->
                    when {
                        hasAccessibility && isShowingAccessibilityInfo -> 1
                        !hasAccessibility && !isShowingAccessibilityInfo -> 2
                        else -> 3
                    }

                })
                .subscribeBy(
                        onNext = {
                            when(it) {
                                1 -> onAccessibilityActivated()
                                2 -> activateButton?.visibility = View.VISIBLE
                                3 -> activateButton?.visibility = View.GONE
                            }
                        }
                )
    }

}
