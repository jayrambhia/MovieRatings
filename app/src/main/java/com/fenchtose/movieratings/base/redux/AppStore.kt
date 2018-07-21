package com.fenchtose.movieratings.base.redux

import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.router.navigator
import com.fenchtose.movieratings.features.searchpage.SearchMiddleWare
import com.fenchtose.movieratings.features.searchpage.searchPageReducer
import com.fenchtose.movieratings.model.db.like.LikeMiddleware

class AppStore: SimpleStore<AppState>(
        AppState(),
        listOf(::searchPageReducer),
        listOf<Middleware<AppState>>(
                ::logger,
                ::navigator,
                SearchMiddleWare.newInstance()::searchMiddleware,
                LikeMiddleware.newInstance()::likeMiddleware
        )
)