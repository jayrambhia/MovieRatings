package com.fenchtose.movieratings.base.router

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionSet
import android.util.Log
import android.view.Gravity
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterBaseActivity
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.features.moviepage.DetailTransition
import com.fenchtose.movieratings.features.moviepage.MoviePath
import com.fenchtose.movieratings.features.premium.DonatePageFragment

class Router(activity: RouterBaseActivity,
             private val roots: Map<String, RouterRoot>,
             private val onMovedTo: (path: RouterPath<out BaseFragment>, isRoot: Boolean) -> Unit,
             private val onRemoved: (path: RouterPath<out BaseFragment>) -> Unit) {

    private val manager = activity.supportFragmentManager
    private val titlebar: ActionBar? = activity.supportActionBar
    private var currentRoot: String = ""

    private val keyPathMap: HashMap<String, Pair<String, ((Bundle) -> RouterPath<out BaseFragment>)>> = HashMap()

    var dispatch: Dispatch? = null

    private val TAG = "Router"

    companion object {
        const val ROUTE_TO_SCREEN = "route_to_screen"
        const val HISTORY = "history"

        const val ROOT_SEARCH = "search"
        const val ROOT_PERSONAL = "personal"
        const val ROOT_INFO = "info"
        const val ROOT_COLLECTIONS = "collections"
    }

    init {
        keyPathMap[DonatePageFragment.DonatePath.KEY] = Pair(ROOT_SEARCH, DonatePageFragment.DonatePath.createPath())
        keyPathMap[MoviePath.KEY] = Pair(ROOT_SEARCH, MoviePath.createPath())
    }

    fun canHandleKey(key: String): Boolean {
        return keyPathMap.containsKey(key)
    }

    private fun currentRoot(): RouterRoot? = roots[currentRoot]

    fun buildRoute(root: String, path: RouterPath<out BaseFragment>, recreateRoot: Boolean = false): Router {
        roots[root]?.build(dispatch, path, recreateRoot)
        return this
    }

    fun buildRoute(extras: Bundle): Router {
        keyPathMap[extras.getString(ROUTE_TO_SCREEN, "")]?.let {
            buildRoute(it.first, it.second(extras))
        }
        return this
    }

    fun start(root: String) {
        currentRoot = root
        currentRoot()?.top()?.let {
            // We may not have dispatch attached at this moment.
            MovieRatingsApplication.store.dispatchEarly(it.initAction())
            move(it, currentRoot()?.size() == 1)
        }
    }

    fun switchRoot(root: String, startAtBase: Boolean) {
        if (currentRoot == root) {
            if (!startAtBase) {
                return
            }

            currentRoot()?.clearTillBase(dispatch)
        }

        start(root)
    }

    fun go(path: RouterPath<out BaseFragment>) {
        currentRoot()?.let {
            val top = it.top()
            if (top.javaClass == path.javaClass) {
                return
            }

            it.build(dispatch, path)
            move(path)
        }

    }

    fun onBackRequested(): Boolean {
        currentRoot()?.let {
            val canTopGoBack = canTopGoBack()
            if (canTopGoBack) {
                if (it.size() == 1) {
                    return true
                }

                goBack()
            }

            return false
        }

        Log.e(TAG, "current root not found.")
        return true
    }

    private fun goBack(): Boolean {
        currentRoot()?.let {
            if (it.size() > 1) {
                moveBack()
                return true
            }

        }
        return false
    }

    private fun canTopGoBack(): Boolean {
        val fragment: BaseFragment? = getTopView()

        fragment?.let {
            return fragment.canGoBack()
        }

        return true

    }

    private fun getTopView() : BaseFragment? {
        return currentRoot()?.top()?.fragment
    }

    private fun move(path: RouterPath<out BaseFragment>, animate: Boolean = false) {
        path.attachRouter(this)
        val fragment = path.createOrGetFragment()
        val transaction = manager.beginTransaction().replace(R.id.fragment_container, fragment)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            path.getSharedTransitionElement()?.let {
            transaction.addSharedElement(it.first, it.second)
                fragment.sharedElementEnterTransition = DetailTransition()
                fragment.sharedElementReturnTransition = DetailTransition()
            }
        }

        if (animate) {
            fragment.enterTransition = TransitionSet().apply {
                addTransition(Fade(Fade.IN))
                addTransition(Slide(Gravity.BOTTOM))
                ordering = TransitionSet.ORDERING_TOGETHER
            }
        } else {
            fragment.enterTransition = null
        }

        transaction.commit()
        titlebar?.let {
            it.setTitle(fragment.getScreenTitle())
            val root = currentRoot()
            val backEnabled = if (root != null) {
                root.size() > 1 && path.showBackButton()
            } else {
                path.showBackButton()
            }

            it.setDisplayShowHomeEnabled(backEnabled)
            it.setDisplayHomeAsUpEnabled(backEnabled)
            it.elevation = it.themedContext.resources.getDimension(path.toolbarElevation())
        }

        onMovedTo.invoke(path, currentRoot()?.size() == 1)
    }

    fun updateTitle(title: CharSequence) {
        titlebar?.title = title
    }

    private fun moveBack() {
        currentRoot()?.let {
            val path = it.pop()
            path?.let {
                path.detach()
                onRemoved.invoke(path)
            }

            val top = it.top()
            move(top)
            path?.let {
                dispatch?.invoke(path.clearAction())
            }

        }

    }

    class History {

        val history = ArrayList<Pair<String, Bundle>>()

        constructor()
        constructor(extras: Bundle) {
            if (!extras.containsKey(KEY_PATHS)) {
                return
            }

            val pathKeys = extras.getStringArrayList(KEY_PATHS) ?: return
            pathKeys.forEach { key ->
                if (extras.containsKey(key)) {
                    extras.getBundle(key)?.let {
                        history.add(Pair(key, it))
                    }
                }
            }
        }

        companion object {
            private const val KEY_PATHS = "paths"
        }

        fun addPath(pathKey: String, extras: Bundle): History {
            history.add(Pair(pathKey, extras))
            return this
        }

        fun toBundle(): Bundle {
            val extras = Bundle()
            extras.putStringArrayList(KEY_PATHS, ArrayList(history.map { it.first }))
            history.forEach {
                extras.putString(ROUTE_TO_SCREEN, it.first)
                extras.putBundle(it.first, it.second)
            }

            return extras
        }

        fun isEmpty(): Boolean = history.isEmpty()
    }
}