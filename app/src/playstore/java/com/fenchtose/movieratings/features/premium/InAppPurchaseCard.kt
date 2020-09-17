package com.fenchtose.movieratings.features.premium

import android.content.Context
import androidx.cardview.widget.CardView
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
    private val bragCta: Button

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.inapp_purchase_item_layout, this, true)
        val density = context.resources.displayMetrics.density
        radius = density*4
        val padding = (density*8).toInt()
        setContentPadding(padding, padding, padding, padding)
        title = findViewById(R.id.title_view)
        subtitle = findViewById(R.id.subtitle_view)
        cta = findViewById(R.id.purchase_cta)
        bragCta = findViewById(R.id.brag_cta)
        thanksView = findViewById(R.id.bought_view)
    }

    fun update(content: PurchaseCardContent, onPurchase: (SkuDetails) -> Unit, onBrag: (String) -> Unit) {
        title.text = content.title
        subtitle.text = content.description
        cta.text = context.getString(R.string.donate_card_cta, content.price)
        cta.setOnClickListener { onPurchase(content.skuDetails) }
        cta.show(content.purchased == 0)
        thanksView.show(content.purchased > 0)
        bragCta.show(content.purchased > 0)
        bragCta.setOnClickListener { onBrag(content.sku) }
    }

}