package com.fenchtose.movieratings.features.movie_page

import com.fenchtose.movieratings.base.BaseFragment

class MoviePageFragment: BaseFragment() {


    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return 0
    }
}