package com.fenchtose.movieratings.features.premium

import android.content.Context
import androidx.cardview.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.util.show

class InAppPurchaseCard : CardView {

    private val title: TextView
    private val subtitle: TextView
    private val cta: Button
    private val thanksView: TextView
    private val bragCta: Button
    private val subBoughtPriceView: TextView

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
        subBoughtPriceView = findViewById(R.id.sub_price_view)
    }

    fun update(content: PurchaseCardContent, onPurchase: (SkuDetails) -> Unit, onBrag: (String) -> Unit) {
        title.text = content.title
        subtitle.text = content.description

        val product = content.skuDetails
        if (product.type == BillingClient.SkuType.SUBS) {
            val basicPeriod = product.subscriptionPeriod.basicPeriodicConversion()
            if (basicPeriod == null) {
                // lol
                show(false)
                return
            }

            val pricing = content.price  + " / $basicPeriod"
            cta.text = context.getString(R.string.donate_card_cta, pricing)
            subBoughtPriceView.text = pricing
            subBoughtPriceView.show(content.purchased > 0)
        } else {
            cta.text = context.getString(R.string.donate_card_cta, content.price)
            subBoughtPriceView.show(false)
        }

        cta.setOnClickListener { onPurchase(content.skuDetails) }
        cta.show(content.purchased == 0)
        thanksView.show(content.purchased > 0)
        bragCta.show(content.purchased > 0)
        bragCta.setOnClickListener { onBrag(content.sku) }
    }

}

fun String.basicPeriodicConversion(): String? {
    return when(this) {
        "P1M" -> "month"
        "P3M" -> "3 months"
        "P6M" -> "6 months"
        "P1Y" -> "year"
        else -> null
    }
}