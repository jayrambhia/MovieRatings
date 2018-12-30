package com.fenchtose.movieratings.features.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.util.IntentUtils

class BatteryOptimizationFragment : BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.battery_optimization_info_screen_title
    override fun screenName() = GaScreens.BATTERY_OPTIMIZATION_INFO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        return inflater.inflate(R.layout.battery_optimization_info_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.battery_optimization_cta).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                IntentUtils.openBatteryOptimizationWhitelist(requireContext())
            }
        }
    }
}

class BatteryOptimizationPath : RouterPath<BatteryOptimizationFragment>() {
    override fun createFragmentInstance(): BatteryOptimizationFragment {
        return BatteryOptimizationFragment()
    }
}