package com.fenchtose.movieratings.features.androidtv

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.image.ImageLoader

class TvAccessInfoFragment: BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.tv_welcome_screen_title

    private var focuslistener: View.OnFocusChangeListener? = null
    private lateinit var contentView: TextView
    private lateinit var imageView: ImageView
    private lateinit var imageLoader: ImageLoader

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.tv_access_info_screen_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentView = view.findViewById(R.id.content_view)
        imageView = view.findViewById(R.id.imageview)

        imageLoader = GlideLoader(Glide.with(this))

        focuslistener = View.OnFocusChangeListener {
            textview, hasFocus ->
            updateContent(when(textview.id) {
                R.id.step0 -> Pair(R.string.tv_access_info_step0, R.drawable.image_step0)
                R.id.step1 -> Pair(R.string.tv_access_info_step1, R.drawable.image_step1)
                R.id.step2 -> Pair(R.string.tv_access_info_step2, R.drawable.image_step2)
                R.id.step3 -> Pair(R.string.tv_access_info_step3, R.drawable.image_step3)
                else -> Pair(R.string.tv_access_info_step1, R.drawable.image_step1)
            })

            (textview as TextView).setTextColor(ContextCompat.getColor(requireContext(), if (hasFocus) R.color.textColorDark else R.color.textColorLight))
        }

        view.findViewById<TextView>(R.id.step0).onFocusChangeListener = focuslistener
        view.findViewById<TextView>(R.id.step1).onFocusChangeListener = focuslistener
        view.findViewById<TextView>(R.id.step2).onFocusChangeListener = focuslistener
        view.findViewById<TextView>(R.id.step3).onFocusChangeListener = focuslistener

        view.findViewById<TextView>(R.id.step0).requestFocus()
    }

    private fun updateContent(content: Pair<Int, Int>) {
        contentView.setText(content.first)
        imageLoader.loadDrawable(content.second, imageView)
    }

    class TvAccessInfoPath: RouterPath<TvAccessInfoFragment>() {
        override fun createFragmentInstance(): TvAccessInfoFragment {
            return TvAccessInfoFragment()
        }

    }
}