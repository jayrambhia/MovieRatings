package com.fenchtose.movieratings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.access_info.AccessInfoFragment
import com.fenchtose.movieratings.features.search_page.SearchPageFragment
import com.fenchtose.movieratings.features.settings.SettingsFragment
import com.fenchtose.movieratings.util.AccessibilityUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject


class MainActivity : AppCompatActivity() {

    private var container: FrameLayout? = null
    private var activateButton: TextView? = null
    private var toolbar: Toolbar? = null
    private var titlebar: ActionBar? = null

    private var router: Router? = null

    private var accessibilityPublisher: PublishSubject<Boolean>? = null
    private var accessibilityPagePublisher: PublishSubject<Boolean>? = null
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.fragment_container) as FrameLayout
        activateButton = findViewById(R.id.activate_button) as TextView

        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        titlebar = supportActionBar

        setupObservables()

        router = Router(this)

        router?.callback = object: Router.RouterCallback {
            override fun movedTo(path: RouterPath<out BaseFragment>) {
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

        activateButton?.text = getString(R.string.activate_app_cta, getString(R.string.app_name_short))
        activateButton?.setOnClickListener {
            showAccessibilityInfo()
        }

        showSearchPage()
        accessibilityPagePublisher?.onNext(false)
    }

    override fun onResume() {
        super.onResume()
        accessibilityPublisher?.onNext(AccessibilityUtils.isAccessibilityEnabled(this,
                BuildConfig.APPLICATION_ID + "/." + NetflixReaderService::class.java.simpleName) && AccessibilityUtils.isDrawPermissionEnabled(this))
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_settings -> {
                showSettingsPage()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showSearchPage() {
        router?.go(SearchPageFragment.SearchPath())
    }

    private fun showAccessibilityInfo() {
        router?.go(AccessInfoFragment.AccessibilityPath())
    }

    private fun showSettingsPage() {
        router?.go(SettingsFragment.SettingsPath())
    }

    private fun setupObservables() {
        accessibilityPublisher = PublishSubject.create()
        accessibilityPagePublisher = PublishSubject.create()

        disposable =
                Observable.combineLatest(accessibilityPublisher, accessibilityPagePublisher,
                BiFunction<Boolean, Boolean, Int> {
                    hasAccessibility, isShowingAccessibilityInfo ->
                    if (hasAccessibility && isShowingAccessibilityInfo) {
                        1
                    } else if (!hasAccessibility && !isShowingAccessibilityInfo) {
                        2
                    } else {
                        3
                    }

                })
                .subscribeBy(
                        onNext = {
                            when(it) {
                                1 -> onBackPressed()
                                2 -> activateButton?.visibility = View.VISIBLE
                                3 -> activateButton?.visibility = View.GONE
                            }
                        }
                )
    }

}
