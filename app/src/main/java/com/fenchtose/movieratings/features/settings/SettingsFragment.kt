package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.settings.bubble.RatingBubbleSectionFragment
import com.fenchtose.movieratings.widgets.SimpleAdapter
import com.fenchtose.movieratings.widgets.SimpleAdapterViewBinder
import com.fenchtose.movieratings.util.checkBatteryOptimized
import com.fenchtose.movieratings.util.show

class SettingsFragment : BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_header
    override fun screenName() = GaScreens.SETTINGS

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_page_redesign_layout, container, false)
    }

    override fun onViewCreated(continar: View, savedInstanceState: Bundle?) {

        val recyclerView: RecyclerView = continar.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = SimpleAdapter(
            listOf(
                SimpleAdapterViewBinder(
                    SettingsSection::class,
                    R.layout.settings_page_section_item
                ) { view, items, position ->
                    bindItem(
                        view,
                        items[position] as SettingsSection
                    )
                }
            )
        )

        recyclerView.adapter = adapter

        val items = mutableListOf(
            SettingsSection(
                R.string.settings_app_section_title,
                R.string.settings_app_section_subtitle,
                AppSectionFragment.SettingsAppSectionPath()
            ),
            SettingsSection(
                R.string.settings_data_and_privacy_section_title,
                R.string.settings_data_and_privacy_section_subtitle,
                DataSectionFragment.DataSettingsPath()
            ),
            SettingsSection(
                R.string.settings_tts_section_title,
                R.string.settings_tts_section_subtitle,
                TTSSectionFragment.TTSSettingsPath()
            ),
            SettingsSection(
                R.string.settings_misc_section_title,
                R.string.settings_misc_section_subtitle,
                MiscSectionFragment.MiscSettingsPath()
            ),
            SettingsSection(
                R.string.settings_rating_section_title,
                R.string.settings_rating_section_subtitle,
                RatingBubbleSectionFragment.RatingSectionPath()
            )
        )

        if (checkBatteryOptimized(requireContext())) {
            items.add(
                SettingsSection(
                    R.string.settings_battery_optimization_section_title,
                    R.string.settings_battery_optimization_section_subtitle,
                    BatteryOptimizationPath()
                )
            )
        }

        adapter.setItems(items)
    }

    private fun bindItem(view: View, item: SettingsSection) {
        view.findViewById<TextView>(R.id.settings_title).setText(item.title)
        view.findViewById<TextView>(R.id.settings_subtitle).setText(item.subtitle)
        view.setOnClickListener {
            path?.getRouter()?.go(item.goTo)
        }
    }

    class SettingsPath : RouterPath<SettingsFragment>() {
        override fun createFragmentInstance() = SettingsFragment()
        override fun category() = GaCategory.SETTINGS
    }
}

data class SettingsSection(
    val title: Int,
    val subtitle: Int,
    val goTo: RouterPath<out BaseFragment>
)