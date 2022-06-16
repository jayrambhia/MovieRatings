package com.fenchtose.movieratings.features.searchpage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.model.entity.Movie

// TODO: Compose - Support Pagination!
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieLazyList(movies: List<Movie>, dispatch: Dispatch?, itemFooter: @Composable (Movie, Dispatch?) -> Unit = { _, _ -> }, onMovieLiked: (Movie) -> Unit, openMovie: (Movie) -> Unit) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
        items(
            items = movies,
            itemContent = {
                MovieItemView(movie = it, itemFooter,  dispatch, onMovieLiked, openMovie)
            }
        )
    }
}