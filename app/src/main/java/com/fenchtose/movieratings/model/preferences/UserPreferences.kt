package com.fenchtose.movieratings.model.preferences

import com.fenchtose.movieratings.model.Sort

interface UserPreferences {

    companion object {
        val NETFLIX = "netflix"
        val PRIMEVIDEO = "primevideo"
        val SAVE_HISTORY = "save_history"
        val USE_TTS = "use_tts"
        val TTS_AVAILABLE = "tts_available"
        val SHOW_ACTIVATE_FLUTTER = "show_activate_flutter"
    }

    /**
     * returns if particular app is enabled or not. Default is true.
     */
    fun isAppEnabled(app: String): Boolean
    fun setAppEnabled(app: String, status: Boolean)

    /**
     * returns if particular setting is enabled or not. Default is false.
     */
    fun isSettingEnabled(app: String): Boolean
    fun setSettingEnabled(app: String, status: Boolean)

    fun getToastDuration():Int
    fun setToastDuration(durationInMS: Int)

    fun setLatestLikeSort(type: Sort)
    fun getLatestLikeSort(): Sort

    fun setLatestCollectionSort(collectionId: Long?, type: Sort)
    fun getLatestCollectionSort(collectionId: Long?): Sort
}