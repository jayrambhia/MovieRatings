package com.fenchtose.movieratings.features.moviecollection.collectionlist

import com.fenchtose.movieratings.model.db.entity.MovieCollection

data class CollectionListPageState(
    val collections: List<MovieCollection>
)