package com.fenchtose.movieratings.features.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath

class DonatePageFragment: BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.donate_page_title
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.donate_page_layout, container, false)
    class DonatePath: RouterPath<DonatePageFragment>() {
        override fun createFragmentInstance(): DonatePageFragment {
            return DonatePageFragment()
        }
    }
}