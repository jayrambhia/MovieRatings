package com.fenchtose.movieratings.features.android_tv

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.access_info.AccessInfoFragment
import com.fenchtose.movieratings.features.info.AppInfoFragment

class TVActivity : AppCompatActivity() {

    private var router: Router? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)
        router = Router(this)
        router?.go(AppInfoFragment.AppInfoPath())
    }
}
