package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.Snackbar
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsFragment: BaseFragment() {

    private var root: ViewGroup? = null
    private var updatePublisher: PublishSubject<Boolean>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater.inflate(R.layout.settings_page_layout, container, false) as ViewGroup
        return root!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view?.findViewById(R.id.netflix_toggle) as SwitchCompat).setOnCheckedChangeListener {
            view, isChecked ->  updatePreference(R.id.netflix_toggle, isChecked)
        }

        val publisher = PublishSubject.create<Boolean>()
        subscribe(publisher
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showUpdatePreferenceSnackbar() })

        updatePublisher = publisher
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatePublisher?.onComplete()
    }

    private fun updatePreference(@IdRes res: Int, checked: Boolean) {
        updatePublisher?.onNext(true)
    }

    private fun showUpdatePreferenceSnackbar() {
        root?.let {
            Snackbar.make(root!!, R.string.settings_preference_update_content, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.settings_header
    }

    class SettingsPath: RouterPath<SettingsFragment>() {

        override fun createFragmentInstance(): SettingsFragment {
            return SettingsFragment()
        }

    }

}