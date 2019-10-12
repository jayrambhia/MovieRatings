package com.fenchtose.movieratings.features.moviepage

import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.scale
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.entity.Episode

class EpisodesViewHolder(itemView: View, callback: EpisodesAdapter.Callback): RecyclerView.ViewHolder(itemView) {

    private var episode: Episode? = null

    private val number: TextView = itemView.findViewById(R.id.episode_number)
    private val title: TextView = itemView.findViewById(R.id.episode_title)
    private val rating: TextView = itemView.findViewById(R.id.episode_rating)
    private val release: TextView = itemView.findViewById(R.id.episode_release)

    init {
        itemView.setOnClickListener {
            episode?.let {
                callback.onSelected(it)
            }
        }
    }

    fun bind(episode: Episode) {
        this.episode = episode
        number.text = episode.episode.toString()
        title.text = episode.title
        rating.text = SpannableStringBuilder(rating.context.getText(R.string.movie_page_episodes_rated))
                .bold {
                    scale(1.1f) {
                        append("  ${episode.imdbRating}")
                    }
                }

        release.text = SpannableStringBuilder(release.context.getString(R.string.movie_page_episodes_released_on))
                .bold {
                    scale(1.1f) {
                        append("  ${episode.released}")
                    }
                }
    }
}