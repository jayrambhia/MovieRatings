package com.fenchtose.movieratings.widgets

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.RuntimeException
import kotlin.reflect.KClass

class SimpleAdapter(private val viewBinders: List<SimpleAdapterViewBinder<*>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Any> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewBinder = viewBinders[viewType]
        return SimpleViewHolder(viewBinder, parent)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        var typeIndex = -1
        viewBinders.forEachIndexed { index, type ->
            if (type.kClass == items[position]::class) {
                typeIndex = index
                return@forEachIndexed
            }
        }

        if (typeIndex == -1) {
            throw RuntimeException("ViewType not provided for ${items[position]::class}")
        }

        return typeIndex
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SimpleViewHolder) {
            holder.bind(items, position)
        }
    }

    fun setItems(items: List<Any>) {
        this.items = items
        notifyDataSetChanged()
    }

    private inner class SimpleViewHolder(private val binder: SimpleAdapterViewBinder<*>, parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                binder.layoutId,
                parent,
                false
            )
        ) {

        fun bind(items: List<Any>, position: Int) {
            binder.onBind(itemView, items, position)
        }
    }
}

class SimpleAdapterViewBinder<T : Any>(
    val kClass: KClass<T>,
    val layoutId: Int,
    val onBind: (view: View, items: List<Any>, position: Int) -> Unit
)