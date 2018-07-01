package com.fenchtose.movieratings.base.router

import android.os.Build
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.features.moviepage.DetailTransition
import java.util.Stack

class Router(activity: AppCompatActivity) {

    private val history: Stack<RouterPath<out BaseFragment>> = Stack()
    private val manager = activity.supportFragmentManager
    private val titlebar: ActionBar? = activity.supportActionBar
    var callback: RouterCallback? = null

    private val TAG = "Router"

    companion object {
        val ROUTE_TO_SCREEN = "route_to_screen"
    }

    fun buildRoute(path: RouterPath<out BaseFragment>): Router {
        history.push(path)
        return this
    }

    fun start() {
        if (history.isNotEmpty()) {
            move(history.peek())
        }
    }

    fun go(path: RouterPath<out BaseFragment>) {
        if (history.size >= 1) {
            val top = history.peek()
            if (top.javaClass == path.javaClass) {
                return
            }

            top.saveState()
        }

        move(path)
        history.push(path)
    }

    fun onBackRequested(): Boolean {
        if (history.empty()) {
            Log.e(TAG, "history is empty. We can't go back")
            return true
        }

        val canTopGoBack = canTopGoBack()
        if (canTopGoBack) {
            if (history.size == 1) {
                return  true
            }

            goBack()
        }

        return false
    }

    private fun goBack(): Boolean {

        if (history.size > 1) {
            moveBack()
            return true
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
        return history.peek().fragment
    }

    private fun move(path: RouterPath<out BaseFragment>) {
        val fragment = path.createOrGetFragment()
        val transaction = manager.beginTransaction().replace(R.id.fragment_container, fragment)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            path.getSharedTransitionElement()?.let {
            transaction.addSharedElement(it.first, it.second)
                fragment.sharedElementEnterTransition = DetailTransition()
                fragment.sharedElementReturnTransition = DetailTransition()
            }
        }

        transaction.commit()
        titlebar?.let {
            it.setTitle(fragment.getScreenTitle())
            it.setDisplayShowHomeEnabled(path.showBackButton())
            it.setDisplayHomeAsUpEnabled(path.showBackButton())
        }

        callback?.movedTo(path)
    }

    fun updateTitle(title: CharSequence) {
        titlebar?.title = title
    }

    private fun moveBack() {
        val path = history.pop()
        path.clearState()
        callback?.removed(path)
        if (!history.empty()) {
            val top = history.peek()
            top?.let {
                move(top)
            }
        }
    }

    interface RouterCallback {
        fun movedTo(path: RouterPath<out BaseFragment>)
        fun removed(path: RouterPath<out BaseFragment>)
    }
}