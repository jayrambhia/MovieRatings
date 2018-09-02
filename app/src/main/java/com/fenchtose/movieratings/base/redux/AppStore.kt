package com.fenchtose.movieratings.base.redux

import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.router.navigator
import com.fenchtose.movieratings.features.recentlybrowsedpage.RecentlyBrowsedMiddleware
import com.fenchtose.movieratings.features.recentlybrowsedpage.recentlyBrowsedPageReducer
import com.fenchtose.movieratings.features.searchpage.SearchMiddleWare
import com.fenchtose.movieratings.features.searchpage.searchPageReducer
import com.fenchtose.movieratings.model.db.like.LikeMiddleware
import com.fenchtose.movieratings.model.db.movieCollection.CollectionMiddleware

class AppStore: SimpleStore<AppState>(
        AppState(),
        listOf(::searchPageReducer, AppState::recentlyBrowsedPageReducer),
        listOf<Middleware<AppState>>(
                ::logger,
                ::navigator,
                SearchMiddleWare.newInstance()::searchMiddleware,
                LikeMiddleware.newInstance()::likeMiddleware,
                CollectionMiddleware.newInstance()::collectionMiddleware,
                RecentlyBrowsedMiddleware.newInstance()::middleware
        )
)