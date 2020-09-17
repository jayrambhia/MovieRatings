package com.fenchtose.movieratings.features.premium

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.android.billingclient.api.SkuDetails

class CardsContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val cards: MutableList<InAppPurchaseCard> = mutableListOf()
    private val verticalMargin = (context.resources.displayMetrics.density * 8).toInt()
    private val horizontalMargin = (context.resources.displayMetrics.density * 16).toInt()

    init {
        orientation = VERTICAL
        if (isInEditMode) {
            update(
                listOf(
                    PurchaseCardContent(
                        skuDetails = SkuDetails(""),
                        sku = "test",
                        title = "Basic",
                        description = "This is a basic card",
                        price = "$ 0.99",
                        purchased = 1
                    )
                ),
                onPurchase = {},
                onBrag = {}
            )
        }
    }

    fun update(content: List<PurchaseCardContent>, onPurchase: (SkuDetails) -> Unit, onBrag: (String) -> Unit) {
        resizeTo(content.size)
        content.forEachIndexed { index, item ->
            if (cards.size > index) {
                cards[index].update(item, onPurchase, onBrag)
            }
        }
    }

    private fun resizeTo(size: Int) {
        while (cards.size > size) {
            val card = cards.removeAt(cards.size - 1)
            removeView(card)
        }

        while (cards.size < size) {
            val card = InAppPurchaseCard(context)
            cards.add(card)
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin)
            }
            addView(card, params)
        }
    }
}

data class PurchaseCardContent(
    val sku: String,
    val title: String,
    val description: String,
    val price: String,
    val purchased: Int,
    val skuDetails: SkuDetails
)