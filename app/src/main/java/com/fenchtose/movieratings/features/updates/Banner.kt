package com.fenchtose.movieratings.features.updates

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.util.show

class Banner(
    private val container: View,
    private val message: CharSequence,
    private val icon: Drawable?,
    private val positiveText: String,
    private val onPositive: (Banner) -> Unit,
    private val dismissText: String,
    private val onDismiss: (Banner) -> Unit
) {

    fun show() {
        container.findViewById<TextView>(R.id.banner_content).text = message
        icon?.let {
            container.findViewById<ImageView>(R.id.banner_icon).apply {
                show(true)
                setImageDrawable(it)
            }
        } ?: kotlin.run { container.findViewById<View>(R.id.banner_icon).show(false) }
        setupCta(container.findViewById(R.id.banner_dismiss_cta), dismissText, onDismiss)
        setupCta(container.findViewById(R.id.banner_positive_cta), positiveText, onPositive)
        container.show(true)
    }

    fun dismiss() {
        container.show(false)
    }

    private fun setupCta(cta: Button, text: String, onClick: (Banner) -> Unit) {
        if (text.isEmpty()) {
            cta.show(false)
            return
        }

        cta.show(true)
        cta.text = text
        cta.setOnClickListener { onClick(this) }
    }

    class Builder(private val context: Context, private val container: View) {
        private var message: CharSequence = ""
        private var drawable: Drawable? = null
        private var dismissText: String = ""
        private var onDismiss: (Banner) -> Unit = {}
        private var ctaText: String = ""
        private var onPositiveCta: (Banner) -> Unit = {}

        fun withMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun withMessage(@StringRes resId: Int): Builder {
            this.message = context.getString(resId)
            return this
        }

        fun withIcon(drawable: Drawable): Builder {
            this.drawable = drawable
            return this
        }

        fun withIcon(@DrawableRes drawableId: Int): Builder {
            this.drawable = ContextCompat.getDrawable(context, drawableId)
            return this
        }

        fun withDismiss(text: String, onDismiss: (Banner) -> Unit): Builder {
            this.dismissText = text
            this.onDismiss = onDismiss
            return this
        }

        fun withDismiss(@StringRes id: Int, onDismiss: (Banner) -> Unit): Builder {
            this.dismissText = context.getString(id)
            this.onDismiss = onDismiss
            return this
        }

        fun withCta(text: String, onCta: (Banner) -> Unit): Builder {
            this.ctaText = text
            this.onPositiveCta = onCta
            return this
        }

        fun withCta(@StringRes id: Int, onCta: (Banner) -> Unit): Builder {
            this.ctaText = context.getString(id)
            this.onPositiveCta = onCta
            return this
        }

        fun withUpdateBanner(item: UpdateItem, router: Router, dispatch: Dispatch): Builder {
            onDismiss = { banner ->
                banner.dismiss()
                dispatch(Dismiss(item))
            }
            onPositiveCta = { banner ->
                banner.dismiss()
                dispatch(PositiveCta(item, router))
            }
            dismissText = context.getString(R.string.update_banner_dismiss_cta)
            ctaText = item.positiveCtaText
            message = item.description
            drawable = if (item.icon != 0) ContextCompat.getDrawable(context, item.icon) else null
            return this
        }

        fun build(): Banner {
            return Banner(
                container,
                message,
                drawable,
                ctaText,
                onPositiveCta,
                dismissText,
                onDismiss
            )
        }

    }
}