package com.fenchtose.movieratings.features.androidtv

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.premium.DonatePageFragment
import com.fenchtose.movieratings.features.settings.AppSectionFragment
import com.fenchtose.movieratings.util.AccessibilityUtils

class TvWelcomeFragment: BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TvCardAdapter

    private val accessibilityCard = TvCard(
            R.string.tv_welcome_card_enable_flutter,
            R.drawable.ic_accessibility_yellow_24dp,
            {
                MovieRatingsApplication.router?.go(TvAccessInfoFragment.TvAccessInfoPath())
            }
    )

    private val inAppPurchaseCard = TvCard(
            R.string.tv_welcome_card_in_app_purchase,
            R.drawable.ic_monetization_on_yellow_24dp,
            {
                MovieRatingsApplication.router?.go(DonatePageFragment.DonatePath())
            }
    )

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
                TvCard(R.string.tv_welcome_card_configure_apps,
                        R.drawable.ic_settings_yellow_24dp,
                        {
                            MovieRatingsApplication.router?.go(AppSectionFragment.SettingsAppSectionPath())
                        })
        ))

        adapter.notifyDataSetChanged()
        view.findViewById<TextView>(R.id.version_view).text = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        if (AccessibilityUtils.hasAllPermissions(requireContext())) {
            if (adapter.cards.contains(accessibilityCard)) {
                adapter.cards.remove(accessibilityCard)
                adapter.notifyDataSetChanged()
            }
        } else {
            if (!adapter.cards.contains(accessibilityCard)) {
                adapter.cards.add(0, accessibilityCard)
                adapter.notifyDataSetChanged()
            }
        }

        if (!adapter.cards.contains(inAppPurchaseCard)) {
            adapter.cards.add(inAppPurchaseCard)
            adapter.notifyDataSetChanged()
        }
    }

    class TvWelcomePath: RouterPath<TvWelcomeFragment>() {
        override fun createFragmentInstance(): TvWelcomeFragment {
            return TvWelcomeFragment()
        }
    }

}