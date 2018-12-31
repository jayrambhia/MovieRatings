package com.fenchtose.movieratings.features.updates

import android.content.Context
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.analytics.events.GaEvent
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.base.redux.reduceChild
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.settings.AppSectionFragment
import com.fenchtose.movieratings.features.settings.BatteryOptimizationPath
import com.fenchtose.movieratings.features.settings.MiscSectionFragment
import com.fenchtose.movieratings.features.updates.BannerStore.Companion.BANNER_BATTERY_OPTIMIZATION
import com.fenchtose.movieratings.features.updates.BannerStore.Companion.BANNER_MAL
import com.fenchtose.movieratings.features.updates.BannerStore.Companion.BANNER_REDBOX
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks
import com.fenchtose.movieratings.util.remove

data class UpdateItem(
    val id: String,
    val maxVersion: Int,
    val icon: Int = 0,
    val description: CharSequence,
    val positiveCtaText: String,
    val filter: (context: Context) -> Boolean = { true }
)

data class UpdateBannersState(
    val banners: List<UpdateItem> = listOf()
)

data class Dismiss(val banner: UpdateItem): Action
data class Loaded(val banners: List<UpdateItem>): Action
data class PositiveCta(val banner: UpdateItem, val router: Router): Action

data class Load(val version: Int): Action

fun AppState.reduceUpdateBanners(action: Action): AppState {
    return reduceChild(updatesBanners, action, {reduce(it)}, {copy(updatesBanners = it)})
}

private fun UpdateBannersState.reduce(action: Action): UpdateBannersState {
    return when(action) {
        is Dismiss -> copy(banners = banners.dismiss(action.banner))
        is Loaded -> copy(banners = action.banners)
        is PositiveCta -> copy(banners = banners.dismiss(action.banner))
        else -> this
    }
}

private fun List<UpdateItem>.dismiss(item: UpdateItem): List<UpdateItem> {
    var index = -1
    forEachIndexed {
        i, banner ->
        if (banner.id == item.id) {
            index = i
            return@forEachIndexed
        }
    }

    if (index != -1) {
        return remove(index)
    }

    return this
}

class UpdatesBannerMiddleware(private val store: BannerStore,
                              private val rxHooks: RxHooks) {
    fun middleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        when (action) {
            is Load -> load(action.version, dispatch)
            is Dismiss -> {
                GaEvents.UPDATE_BANNER_DISMISS.withLabelArg(action.banner.id).track()
                store.dismiss(action.banner)
                return next(state, Load(BuildConfig.VERSION_CODE), dispatch)
            }
            is PositiveCta -> {
                GaEvents.UPDATE_BANNER_CTA.withLabelArg(action.banner.id).track()
                onPositive(action.banner, action.router, dispatch)
                store.dismiss(action.banner)
                return next(state, Load(BuildConfig.VERSION_CODE), dispatch)
            }
        }

        return next(state, action, dispatch)
    }

    private fun onPositive(banner: UpdateItem, router: Router, dispatch: Dispatch) {
        when(banner.id) {
            BANNER_MAL -> dispatch(Navigation(router, MiscSectionFragment.MiscSettingsPath()))
            BANNER_REDBOX -> dispatch(Navigation(router, AppSectionFragment.SettingsAppSectionPath()))
            BANNER_BATTERY_OPTIMIZATION -> dispatch(Navigation(router, BatteryOptimizationPath()))
        }
    }

    private fun load(version: Int, dispatch: Dispatch) {
        store.load(version)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe {
                    dispatch(Loaded(it))
                }
    }

    companion object {
        fun newInstance(context: Context): UpdatesBannerMiddleware {
            return UpdatesBannerMiddleware(
                    PreferencesBannerStore(context),
                    AppRxHooks()
            )
        }
    }
}