package com.fenchtose.movieratings.features.baselistpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fenchtose.movieratings.model.entity.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieListViewModel(private val strategy: MovieListStrategy) : ViewModel() {
    private val _uiState = MutableStateFlow(BaseMovieListPageState())
    val uiState: StateFlow<BaseMovieListPageState> = _uiState

    init {
        viewModelScope.launch {
            reload()
        }
    }

    private suspend fun reload() {
        _uiState.value = BaseMovieListPageState.loading()
        val movies = strategy.loadMovies()
        _uiState.value = BaseMovieListPageState.success(movies)
    }
}

class MovieListViewModelFactory(private val strategy: MovieListStrategy) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MovieListViewModel(strategy) as T
    }
}

interface MovieListStrategy {
    suspend fun loadMovies(): List<Movie>
}
