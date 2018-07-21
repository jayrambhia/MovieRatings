package com.fenchtose.movieratings.base

import com.fenchtose.movieratings.features.searchpage.CollectionSearchPageState
import com.fenchtose.movieratings.features.searchpage.SearchPageState

data class AppState(
        val searchPage: SearchPageState = SearchPageState(),
        val collectionSearchPage: CollectionSearchPageState = CollectionSearchPageState()
)