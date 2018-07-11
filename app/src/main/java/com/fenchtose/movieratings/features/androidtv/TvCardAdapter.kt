package com.fenchtose.movieratings.features.androidtv

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R

class TvCardAdapter(context: Context): RecyclerView.Adapter<TvCardViewHolder>() {

    val cards = ArrayList<TvCard>()
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvCardViewHolder {
        return TvCardViewHolder(inflater.inflate(R.layout.tv_welcome_screen_card_layout, parent, false))
    }

    override fun getItemCount() = cards.size

    override fun onBindViewHolder(holder: TvCardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun onViewRecycled(holder: TvCardViewHolder) {
        super.onViewRecycled(holder)
        holder.detach()
    }

    override fun onViewAttachedToWindow(holder: TvCardViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.attach()
    }
}

class TvCardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val icon: ImageView = itemView.findViewById(R.id.icon_view)
    private val title: TextView = itemView.findViewById(R.id.title_view)

    private var card: TvCard? = null

    init {
        itemView.setOnClickListener {
            card?.onClick?.invoke()
        }
    }

    private val focusListener = View.OnFocusChangeListener {
        v, hasFocus ->
        (v as CardView).cardElevation = if (hasFocus) 12f else 3f
        v.scaleX = if (hasFocus) 1.05f else 1f
        v.scaleY = if (hasFocus) 1.05f else 1f
        ImageViewCompat.setImageTintList(icon, ContextCompat.getColorStateList(icon.context, if (hasFocus) R.color.textColorDark else R.color.textColorLight))
        title.setTextColor(ContextCompat.getColor(title.context, if (hasFocus) R.color.textColorDark else R.color.textColorLight))
    }

    fun bind(card: TvCard) {
        this.card = card
        icon.setImageResource(card.icon)
        title.setText(card.title)
    }

    fun attach() {
        itemView.onFocusChangeListener = focusListener
    }

    fun detach() {
        itemView.onFocusChangeListener = null
    }

}

data class TvCard(@StringRes val title: Int, @DrawableRes val icon: Int, val onClick: (() -> Unit))