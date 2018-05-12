package com.fenchtose.movieratings.features.settings.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.fenchtose.movieratings.R

class SettingsPageComponent: LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.settings_page_component_layout, this, true)
        orientation = VERTICAL
        setBackgroundResource(R.drawable.ripple_white_bg)
        attrs?.let {
            val arr = context.obtainStyledAttributes(it, R.styleable.SettingsPageComponent)
            arr?.let {
                findViewById<TextView>(R.id.settings_title).text = arr.getText(R.styleable.SettingsPageComponent_settings__title)
                findViewById<TextView>(R.id.settings_subtitle).text = arr.getText(R.styleable.SettingsPageComponent_settings__subtitle)
                it.recycle()
            }
        }
    }
}