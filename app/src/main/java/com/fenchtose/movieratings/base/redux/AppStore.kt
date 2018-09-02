package com.fenchtose.movieratings.base.redux

import android.content.Context
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.router.navigator
import com.fenchtose.movieratings.features.likespage.LikesPageMiddleware
import com.fenchtose.movieratings.features.likespage.reduceLikesPage
import com.fenchtose.movieratings.features.moviepage.MoviePageMiddleware
import com.fenchtose.movieratings.features.moviepage.reduceMoviePage
import com.fenchtose.movieratings.features.recentlybrowsedpage.RecentlyBrowsedPageMiddleware
import com.fenchtose.movieratings.features.recentlybrowsedpage.recentlyBrowsedPageReducer
import com.fenchtose.movieratings.features.searchpage.SearchMiddleWare
import com.fenchtose.movieratings.features.searchpage.searchPageReducer
import com.fenchtose.movieratings.features.trending.TrendingMoviesMiddleware
import com.fenchtose.movieratings.features.trending.reduceTrendingPage
import com.fenchtose.movieratings.model.db.like.LikeMiddleware
import com.fenchtose.movieratings.model.db.like.reduceLiked
import com.fenchtose.movieratings.model.db.movieCollection.CollectionMiddleware
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RecentlyBrowsedMiddleware

class AppStore(context: Context): SimpleStore<AppState>(
        AppState(),
        listOf(
                AppState::reduceLiked,
                ::searchPageReducer,
                AppState::recentlyBrowsedPageReducer,
                AppState::reduceLikesPage,
                AppState::reduceTrendingPage,
                AppState::reduceMoviePage),
        listOf<Middleware<AppState>>(
                ::logger,
                ::navigator,
                LikeMiddleware.newInstance()::likeMiddleware,
                RecentlyBrowsedMiddleware.newInstance(context)::middleware,
                SearchMiddleWare.newInstance()::searchMiddleware,
                CollectionMiddleware.newInstance()::collectionMiddleware,
                RecentlyBrowsedPageMiddleware.newInstance()::middleware,
                LikesPageMiddleware.newInstance(context)::middleware,
                TrendingMoviesMiddleware.newInstance()::middleware,
                MoviePageMiddleware.newInstance()::middleware
        )
)