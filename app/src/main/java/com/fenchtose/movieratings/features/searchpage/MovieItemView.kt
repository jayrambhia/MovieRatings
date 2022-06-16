package com.fenchtose.movieratings.features.searchpage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.model.entity.Movie
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun MovieItemView(movie: Movie, footer: @Composable (Movie, Dispatch?) -> Unit, dispatch: Dispatch?, onMovieLiked: (Movie) -> Unit, openMovie: (Movie) -> Unit) {
    Card(
        backgroundColor = colorResource(id = R.color.colorAccent),
        modifier = Modifier.clickable {
            openMovie(movie)
        }
    ) {
        Column() {
            // TODO: Min height + expand if image is bigger.
            Box(modifier = Modifier.requiredHeight(180.dp)) {
                GlideImage(
                    imageModel = movie.poster,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.background(color = colorResource(id = R.color.colorAccent)),
                    previewPlaceholder = R.drawable.parks_rec
                )
            }
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.body1,
                        color = colorResource(id = R.color.textColorLight)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp, 32.dp)
                        .padding(4.dp)
                        .clickable { onMovieLiked(movie) }
                ) {
                    Icon(
                        painter = painterResource(id = if (movie.liked) R.drawable.ic_favorite_accent_24dp else R.drawable.ic_favorite_border_onyx_24dp),
                        contentDescription = "",
                        tint = colorResource(id = R.color.colorPrimary),
                    )
                }
            }
            footer(movie, dispatch)
        }
    }
}

@Composable
fun MovieItemPreview() {
    val movie = Movie(
        imdbId = "",
        title = "Batman Begins",
        year = "2008",
        type = "Movie",
        poster = "https://images-na.ssl-images-amazon.com/images/M/MV5BMjA5MjUxNDgwNF5BMl5BanBnXkFtZTgwMDI5NjMwNDE@._V1_SX300.jpg",
    )
    Box(Modifier.width(240.dp)) {
        MovieItemView(movie = movie, { _, _ -> }, {}, {}) {}
    }
}