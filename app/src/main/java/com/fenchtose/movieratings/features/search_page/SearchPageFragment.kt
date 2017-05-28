package com.fenchtose.movieratings.features.search_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath

class SearchPageFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.search_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): String {
        return "Movies & Ratings"
    }

    class SearchPath : RouterPath<SearchPageFragment>() {
        override fun createFragmentInstance(): SearchPageFragment {
            return SearchPageFragment()
        }
    }
}