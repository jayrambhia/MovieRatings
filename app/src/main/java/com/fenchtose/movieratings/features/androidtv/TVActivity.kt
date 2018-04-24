package com.fenchtose.movieratings.features.androidtv

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.fenchtose.movieratings.MovieRatingsApplication

import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.info.AppInfoFragment

class TVActivity : AppCompatActivity() {

    private var router: Router? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)
        router = Router(this)
        router?.go(AppInfoFragment.AppInfoPath(false))
        MovieRatingsApplication.router = router
    }

    override fun onBackPressed() {
        if (router?.onBackRequested() == false) {
            return
        }

        super.onBackPressed()
    }
}
