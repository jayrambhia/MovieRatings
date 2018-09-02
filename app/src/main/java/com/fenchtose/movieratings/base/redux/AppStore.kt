package com.fenchtose.movieratings.base.redux

import android.content.Context
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.router.navigator
import com.fenchtose.movieratings.features.likespage.LikesPageMiddleware
import com.fenchtose.movieratings.features.likespage.reduceLikesPage
import com.fenchtose.movieratings.features.recentlybrowsedpage.RecentlyBrowsedMiddleware
import com.fenchtose.movieratings.features.recentlybrowsedpage.recentlyBrowsedPageReducer
import com.fenchtose.movieratings.features.searchpage.SearchMiddleWare
import com.fenchtose.movieratings.features.searchpage.searchPageReducer
import com.fenchtose.movieratings.features.trending.TrendingMoviesMiddleware
import com.fenchtose.movieratings.features.trending.reduceTrendingPage
import com.fenchtose.movieratings.model.db.like.LikeMiddleware
import com.fenchtose.movieratings.model.db.movieCollection.CollectionMiddleware

class AppStore(context: Context): SimpleStore<AppState>(
        AppState(),
        listOf(
                ::searchPageReducer,
                AppState::recentlyBrowsedPageReducer,
                AppState::reduceLikesPage,
                AppState::reduceTrendingPage),
        listOf<Middleware<AppState>>(
                ::logger,
                ::navigator,
                SearchMiddleWare.newInstance()::searchMiddleware,
                LikeMiddleware.newInstance()::likeMiddleware,
                CollectionMiddleware.newInstance()::collectionMiddleware,
                RecentlyBrowsedMiddleware.newInstance()::middleware,
                LikesPageMiddleware.newInstance(context)::middleware,
                TrendingMoviesMiddleware.newInstance()::middleware
        )
)