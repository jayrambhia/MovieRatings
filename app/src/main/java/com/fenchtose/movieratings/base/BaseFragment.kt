package com.fenchtose.movieratings.base

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.View
import com.fenchtose.movieratings.widgets.ThemedSnackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment(), FragmentNavigation {

    private var disposables: CompositeDisposable? = null
    var path: RouterPath<out BaseFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
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

    fun <T: View> Fragment.bind(@IdRes idRes: Int): Lazy<T> {
        return lazy {
            view!!.findViewById<T>(idRes)
        }
    }
}