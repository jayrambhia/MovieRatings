package com.fenchtose.movieratings.features.settings

import android.os.Bundle

import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterBaseActivity
import com.fenchtose.movieratings.base.router.Router

class SettingsActivity : RouterBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_layout)
        initializeRouter(toolbar = findViewById(R.id.toolbar), onInit = ::buildPathAndStart)
    }

    private fun buildPathAndStart(router: Router) {
        router.go(SettingsFragment.SettingsPath())
    }
}
