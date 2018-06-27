package com.fenchtose.movieratings.features.settings.bubble

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R

class BubbleColorAdapter(context: Context,
                         private val colors: List<Int>,
                         @ColorInt preselectedColor: Int,
                         private val callback: ColorSelectorCallback): RecyclerView.Adapter<BubbleColorAdapter.BubbleColorViewHolder>() {

    var selected: Int = colors.indexOf(preselectedColor)
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BubbleColorViewHolder {
        return BubbleColorViewHolder(inflater.inflate(R.layout.bubble_color_item_layout, parent, false), callback)
    }

    override fun onBindViewHolder(holder: BubbleColorViewHolder, position: Int) {
        holder.bind(colors[position], selected == position)
    }

    override fun getItemCount() = colors.size

    override fun getItemId(position: Int): Long {
        return colors[position].toLong()
    }

    class BubbleColorViewHolder(itemView: View, callback: ColorSelectorCallback?): RecyclerView.ViewHolder(itemView) {

        @ColorInt
        var color: Int? = null

        init {
            itemView.setOnClickListener {
                callback?.let {
                    color?.let {
                        callback.onColorSelected(it, adapterPosition)
                    }
                }
            }
        }

        fun bind(@ColorInt color: Int, selected: Boolean) {
            this.color = color
            val bubble = BubbleDrawable(color)
            bubble.selected = selected
            itemView.background = bubble
        }
    }

    interface ColorSelectorCallback {
        fun onColorSelected(@ColorInt color: Int, position: Int)
    }
}