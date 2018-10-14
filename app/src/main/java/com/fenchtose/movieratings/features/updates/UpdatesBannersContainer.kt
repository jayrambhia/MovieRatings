package com.fenchtose.movieratings.features.updates

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Unsubscribe
import com.fenchtose.movieratings.util.show

class UpdatesBannersContainer: LinearLayout {

    private val recyclerView: RecyclerView
    private val adapter: BannersAdapter

    private var dispatch: Dispatch? = null
    private var unsubscribe: Unsubscribe? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(R.layout.updates_banners_container_content, this, true)
        orientation = VERTICAL
        recyclerView = findViewById(R.id.recyclerview)
        adapter = BannersAdapter(context, {
            dispatch?.invoke(Dismiss(it))
        })

        adapter.setHasStableIds(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        unsubscribe = MovieRatingsApplication.store.subscribe { appState, dispatch -> render(appState.updatesBanners, dispatch) }
        MovieRatingsApplication.store.dispatchEarly(Load(BuildConfig.VERSION_CODE))
    }

    private fun render(state: UpdateBannersState, dispatch: Dispatch) {
        this.dispatch = dispatch
        adapter.update(state.banners)
        adapter.notifyDataSetChanged()
        show(!state.banners.isEmpty())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unsubscribe?.invoke()
        dispatch = null
    }
}

class BannersAdapter(context: Context, private val onDismiss: (UpdateItem) -> Unit): RecyclerView.Adapter<BannerViewHolder>() {

    private val banners = ArrayList<UpdateItem>()
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(inflater.inflate(R.layout.update_banner_item_view, parent, false), onDismiss)
    }
    override fun getItemCount() = banners.size
    override fun getItemId(position: Int) = banners[position].id.hashCode().toLong()
    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(banners[position])
    }

    fun update(data: List<UpdateItem>) {
        banners.clear()
        banners.addAll(data)
    }
}

class BannerViewHolder(itemView: View, onDismiss: (UpdateItem) -> Unit): RecyclerView.ViewHolder(itemView) {

    private val title = itemView.findViewById<TextView>(R.id.title)
    private val description = itemView.findViewById<TextView>(R.id.content)
    private val cta = itemView.findViewById<TextView>(R.id.cta)

    init {
        itemView.findViewById<View>(R.id.dismiss_cta).setOnClickListener {
            banner?.let(onDismiss)
        }
    }

    private var banner: UpdateItem? = null

    fun bind(banner: UpdateItem) {
        this.banner = banner
        title.text = banner.title
        description.text = banner.description
    }
}