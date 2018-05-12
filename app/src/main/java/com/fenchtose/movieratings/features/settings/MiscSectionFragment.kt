package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.MainActivity
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MiscSectionFragment: BaseFragment() {

    private var updatePublisher: PublishSubject<String>? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_misc_section_page_header
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_misc_section_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = SettingsPreferences(context)

        addAppToggle(preferences, view, R.id.show_activate_toggle, UserPreferences.SHOW_ACTIVATE_FLUTTER)
        addAppToggle(preferences, view, R.id.use_year_toggle, UserPreferences.USE_YEAR)

        val publisher = PublishSubject.create<String>()
        subscribe(publisher
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    when(it) {
                        UserPreferences.SHOW_ACTIVATE_FLUTTER -> {
                            activity?.let {
                                if (it is MainActivity) {
                                    it.triggerAccessibilityCheck()
                                }
                            }
                        }
                    }
                }
                .subscribe {
                    showSnackbar(R.string.settings_preference_update_content)
                })

        updatePublisher = publisher
    }

    private fun addAppToggle(preferences: UserPreferences, root: View, @IdRes buttonId: Int, key: String) {
        val toggle = root.findViewById<SwitchCompat?>(buttonId)
        toggle?.let {
            it.visibility = View.VISIBLE
            it.isChecked = preferences.isAppEnabled(key)
            it.setOnCheckedChangeListener {
                _, isChecked -> updatePreference(preferences, key, isChecked)
            }
        }
    }

    private fun updatePreference(preferences: UserPreferences, app: String, checked: Boolean) {
        preferences.setAppEnabled(app, checked)
        updatePublisher?.onNext(app)
    }

    class MiscSettingsPath: RouterPath<MiscSectionFragment>() {
        override fun createFragmentInstance(): MiscSectionFragment {
            return MiscSectionFragment()
        }
    }
}