package com.fenchtose.movieratings.features.premium

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.android.billingclient.api.SkuDetails
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.util.show

class InAppPurchaseCard : CardView {

    private val title: TextView
    private val subtitle: TextView
    private val cta: Button
    private val thanksView: TextView

    var onPurchase: ((SkuDetails) -> Unit)? = null

    var sku: SkuDetails? = null
    set(value) {
        field = value
        value?.let {
            title.text = it.title.replace(Regex("\\(.+\\)"), "").trim()
            subtitle.text = it.description
            cta.text = it.price
            thanksView.text = context.getString(R.string.donate_already_purchased_text, it.price)
        }
    }

    var bought: Boolean = false
    set(value) {
        field = value
//        cta.show(!field)
        thanksView.show(field)
        subtitle.show(!field)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.inapp_purchase_item_layout, this, true)
        val density = context.resources.displayMetrics.density
        radius = density*8
        val padding = (density*24).toInt()
        setContentPadding(padding, padding, padding, padding)
        title = findViewById(R.id.title_view)
        subtitle = findViewById(R.id.subtitle_view)
        cta = findViewById(R.id.purchase_cta)
        cta.setOnClickListener {
            sku?.let {
                onPurchase?.invoke(it)
            }
        }

        thanksView = findViewById(R.id.bought_view)
    }

}