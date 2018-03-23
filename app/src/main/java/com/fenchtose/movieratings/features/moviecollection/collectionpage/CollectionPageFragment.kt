package com.fenchtose.movieratings.features.moviecollection.collectionpage

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore

class CollectionPageFragment: BaseMovieListPageFragment<CollectionPage, CollectionPagePresenter>() {

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.movie_collection_page_title

    override fun createPresenter(): CollectionPagePresenter {
        return CollectionPagePresenter(DbLikeStore(MovieRatingsApplication.database.favDao()),
                DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                path?.takeIf { it is CollectionPagePath }?.let { (it as CollectionPagePath).collection })
    }

    override fun onCreated() {
        path?.takeIf { it is CollectionPagePath }
                ?.let { (it as CollectionPagePath).collection }
                ?.let { MovieRatingsApplication.router?.updateTitle(it.name) }
    }

    class CollectionPagePath(val collection: MovieCollection) : RouterPath<CollectionPageFragment>() {

        override fun createFragmentInstance(): CollectionPageFragment {
            return CollectionPageFragment()
        }

    }
}