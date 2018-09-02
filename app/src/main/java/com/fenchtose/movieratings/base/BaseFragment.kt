package com.fenchtose.movieratings.base

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.View
import com.fenchtose.movieratings.analytics.events.ScreenView
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Unsubscribe
import com.fenchtose.movieratings.widgets.ThemedSnackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment(), FragmentNavigation {

    private var disposables: CompositeDisposable? = null
    var path: RouterPath<out BaseFragment>? = null

    private var unsubscribe: Unsubscribe? = null
    protected var dispatch: Dispatch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
    }

    override fun onStart() {
        super.onStart()
        ScreenView(screenName()).track()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribe?.invoke()
        dispatch = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables?.dispose()
    }

    fun subscribe(d: Disposable) {
        disposables?.add(d)
    }

    fun showSnackbar(@StringRes resId: Int) {
        if (isAdded) {
            ThemedSnackbar.make(view!!, resId, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun showSnackbar(content: CharSequence) {
        if (isAdded) {
            ThemedSnackbar.make(view!!, content, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun showSnackbarWithAction(content: String, @StringRes actionResId: Int, listener: View.OnClickListener) {
        view?.let {
            ThemedSnackbar.makeWithAction(it,
                    content,
                    Snackbar.LENGTH_LONG,
                    actionResId,
                    listener
            ).show()
        }
    }

    protected fun render(_render: (AppState, Dispatch) -> Unit) {
        unsubscribe = MovieRatingsApplication.store.subscribe { state, dispatch ->
            this.dispatch = dispatch
            _render(state, dispatch)
        }
    }

    open fun saveState(): PresenterState? = null
}