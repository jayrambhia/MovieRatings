package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.entity.Episode

class EpisodesAdapter(context: Context,
                      private val callback: Callback): RecyclerView.Adapter<EpisodesViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val episodes: ArrayList<Episode> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodesViewHolder {
        return EpisodesViewHolder(inflater.inflate(R.layout.episode_list_item_layout, parent, false), callback)
    }

    override fun onBindViewHolder(holder: EpisodesViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    override fun getItemId(position: Int): Long {
        return episodes[position].imdbId.hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return episodes.size
    }

    fun updateEpisodes(episodes: List<Episode>) {
        this.episodes.clear()
        this.episodes.addAll(episodes)
    }

    interface Callback {
        fun onSelected(episode: Episode)
    }
}