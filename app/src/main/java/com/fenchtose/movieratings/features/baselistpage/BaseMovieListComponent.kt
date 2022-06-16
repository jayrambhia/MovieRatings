package com.fenchtose.movieratings.features.baselistpage

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.features.searchpage.MovieLazyList
import com.fenchtose.movieratings.model.entity.Movie

@Composable
fun BaseMovieListComponent(
    progress: Progress,
    movies: List<Movie>,
    dispatch: Dispatch?,
    @StringRes errorRes: Int,
    @StringRes emptyContentRes: Int,
    itemFooter: @Composable (Movie, Dispatch?) -> Unit = { _, _ -> },
    likeMovie: (Movie) -> Unit,
    openMovie: (Movie) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        when (progress) {
            is Progress.Default -> {
            }
            is Progress.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
            is Progress.Error -> {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = stringResource(id = errorRes),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            is Progress.Success -> {
                if (movies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    ) {
                        Text(
                            text = stringResource(id = emptyContentRes),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    MovieLazyList(
                        movies = movies,
                        dispatch = dispatch,
                        itemFooter = itemFooter,
                        onMovieLiked = likeMovie,
                        openMovie = openMovie
                    )
                }
            }
        }
    }
}