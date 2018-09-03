package com.fenchtose.movieratings.base

import com.fenchtose.movieratings.features.likespage.LikesPageState
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPageState
import com.fenchtose.movieratings.features.moviepage.MoviePageState
import com.fenchtose.movieratings.features.recentlybrowsedpage.RecentlyBrowsedState
import com.fenchtose.movieratings.features.searchpage.CollectionSearchPageState
import com.fenchtose.movieratings.features.searchpage.SearchPageState
import com.fenchtose.movieratings.features.trending.TrendingPageState

data class AppState(
        val searchPage: SearchPageState = SearchPageState(),
        val collectionSearchPage: CollectionSearchPageState = CollectionSearchPageState(),
        val recentlyBrowsedPage: RecentlyBrowsedState = RecentlyBrowsedState(),
        val likesPage: LikesPageState = LikesPageState(),
        val trendingPage: TrendingPageState = TrendingPageState(),
        val moviePage: MoviePageState = MoviePageState(),
        val collectionListPage: CollectionListPageState = CollectionListPageState()
)