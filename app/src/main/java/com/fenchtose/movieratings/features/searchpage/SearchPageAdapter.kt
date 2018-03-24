package com.fenchtose.movieratings.features.searchpage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class SearchPageAdapter(context: Context,
                        private val imageLoader: ImageLoader,
                        private val callback: AdapterCallback?,
                        private val extraLayoutCreator: (() -> SearchItemViewHolder.ExtraLayoutHelper)? = null,
                        private val supportsLoadMore: Boolean = false): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_LOADER = -2
    private val TYPE_MOVIE = 1

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var data: ArrayList<Movie> = ArrayList()
    private var showLoadingMore: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_MOVIE -> SearchItemViewHolder(inflater.inflate(R.layout.search_movie_item_layout, parent, false), callback, extraLayoutCreator)
            TYPE_LOADER -> LoaderMoreViewHolder(inflater.inflate(R.layout.load_more_item_view_layout, parent, false), callback)
            else -> throw RuntimeException("Invalid view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            TYPE_LOADER -> (holder as LoaderMoreViewHolder).bind(showLoadingMore)
            TYPE_MOVIE -> (holder as SearchItemViewHolder).bindMovie(data[position], imageLoader)
        }
    }

    override fun getItemCount(): Int {
        return if (data.size > 0 && supportsLoadMore) data.size + 1 else 0
    }

    override fun getItemId(position: Int): Long {
        return if (position < data.size) data[position].imdbId.hashCode().toLong() else "loading".hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position != 0 && position == itemCount - 1) TYPE_LOADER else TYPE_MOVIE
    }

    fun showLoadingMore(status: Boolean) {
        showLoadingMore = status
        if (itemCount != 0) {
            notifyItemChanged(itemCount - 1)
        }
    }

    interface LoadMoreCallback {
        fun onLoadMore(){ }
    }

    interface AdapterCallback: LoadMoreCallback {
        fun onLiked(movie: Movie)
        fun onClicked(movie: Movie, sharedElement: Pair<View, String>?)
    }

    class Builder(private val context: Context, private val imageLoader: ImageLoader) {
        private var callback: AdapterCallback? = null
        private var extraLayoutCreator: (() -> SearchItemViewHolder.ExtraLayoutHelper)? = null
        private var loadMore: Boolean = false

        fun withCallback(callback: AdapterCallback): Builder {
            this.callback = callback
            return this
        }

        fun withExtraLayoutCreator(extraLayoutCreator: (() -> SearchItemViewHolder.ExtraLayoutHelper)): Builder {
            this.extraLayoutCreator = extraLayoutCreator
            return this
        }

        fun withLoadMore(): Builder {
            loadMore = true
            return this
        }

        fun build() = SearchPageAdapter(context, imageLoader, callback, extraLayoutCreator, loadMore)

    }
}