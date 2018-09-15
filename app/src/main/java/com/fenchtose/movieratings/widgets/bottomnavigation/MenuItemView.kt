package com.fenchtose.movieratings.widgets.bottomnavigation

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.fenchtose.movieratings.R

class MenuItemView(context: Context, val item: MenuItem): FrameLayout(context) {
    val image: ImageView
    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_bar_menu_item_view_layout, this, true)
        image = findViewById(R.id.item_image)
        image.setImageResource(item.iconRes)
        setBackgroundResource(R.drawable.ripple_onyx_bg_2)
        active(false)
    }

    fun active(status: Boolean) {
        image.setColorFilter(ContextCompat.getColor(context, if (status) R.color.bottom_bar_item_color_active else R.color.bottom_bar_item_color_inactive), PorterDuff.Mode.SRC_IN)
    }
}