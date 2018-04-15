package com.fenchtose.movieratings.features.season.episode

import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.model.Episode
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EpisodePresenter(private val provider: MovieProvider,
                       private val entry: Episode): Presenter<EpisodePage>() {
    private var episode: Movie? = null

    override fun attachView(view: EpisodePage) {
        super.attachView(view)
        loadMovie(entry)
    }

    private fun loadMovie(entry: Episode) {
        episode?.let {
            showEpisode(it)
            return
        }

        getEpisode(entry)
    }

    private fun getEpisode(entry: Episode) {
        val d = provider
                .getEpisode(entry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showEpisode(it)
                },{
                    it.printStackTrace()
                    updateState(EpisodePage.State.Error())
                })

        subscribe(d)
        updateState(EpisodePage.State.Loading())
    }

    private fun showEpisode(episode: Movie) {
        this.episode = episode
        updateState(EpisodePage.State.Success(episode))
    }

    private fun updateState(state: EpisodePage.State) {
        getView()?.updateState(state)
    }
}