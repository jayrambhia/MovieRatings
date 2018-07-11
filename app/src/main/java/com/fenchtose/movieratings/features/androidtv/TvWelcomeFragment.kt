package com.fenchtose.movieratings.features.androidtv

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.settings.AppSectionFragment

class TvWelcomeFragment: BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TvCardAdapter

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.tv_welcome_screen_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.tv_welcome_screen_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = TvCardAdapter(requireContext())
        recyclerView.adapter = adapter
        adapter.cards.addAll(arrayOf(
                TvCard(
                        R.string.tv_welcome_card_enable_flutter,
                        R.drawable.ic_accessibility_yellow_24dp,
                        {
                            MovieRatingsApplication.router?.go(TvAccessInfoFragment.TvAccessInfoPath())
                        }),
                TvCard(R.string.tv_welcome_card_configure_apps,
                        R.drawable.ic_settings_yellow_24dp,
                        {
                            MovieRatingsApplication.router?.go(AppSectionFragment.SettingsAppSectionPath())
                        })
        ))
        adapter.notifyDataSetChanged()
    }

    class TvWelcomePath: RouterPath<TvWelcomeFragment>() {
        override fun createFragmentInstance(): TvWelcomeFragment {
            return TvWelcomeFragment()
        }
    }

}