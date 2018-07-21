package com.fenchtose.movieratings.base

import com.fenchtose.movieratings.features.searchpage.SearchPageState

data class AppState(
        val searchPage: SearchPageState = SearchPageState()
)