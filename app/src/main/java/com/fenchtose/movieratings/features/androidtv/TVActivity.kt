package com.fenchtose.movieratings.features.androidtv

import android.os.Bundle

import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterBaseActivity
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.info.AppInfoFragment

class TVActivity : RouterBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)
        initializeRouter(onInit = ::buildPathAndStart)
    }

    private fun buildPathAndStart(router: Router) {
        router.go(AppInfoFragment.AppInfoPath(false))
    }
}
