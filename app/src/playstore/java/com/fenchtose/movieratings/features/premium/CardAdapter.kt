package com.fenchtose.movieratings.features.premium

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.android.billingclient.api.SkuDetails

class CardAdapter(private val context: Context,
                  private val onPurchase: ((SkuDetails) -> Unit)): PagerAdapter() {

    private val items: MutableList<SkuDetails> = arrayListOf()
    private val pSkus: MutableList<String> = arrayListOf()

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`
    override fun getCount() = items.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return InAppPurchaseCard(context).apply {
            sku = items[position]
            bought = pSkus.contains(items[position].sku)
            onPurchase = this@CardAdapter.onPurchase
            container.addView(this)
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        (`object` as InAppPurchaseCard).onPurchase = null
    }

    fun update(skus: List<SkuDetails>, purchases: List<String>) {
        items.clear()
        items.addAll(skus)

        pSkus.clear()
        pSkus.addAll(purchases)
        notifyDataSetChanged()
    }

    override fun getPageWidth(position: Int) = 1f
}