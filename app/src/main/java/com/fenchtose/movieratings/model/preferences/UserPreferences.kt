package com.fenchtose.movieratings.model.preferences

import com.fenchtose.movieratings.model.Sort

interface UserPreferences {

    companion object {
        val NETFLIX = "netflix"
    }

    fun isAppEnabled(app: String): Boolean
    fun setAppEnabled(app: String, status: Boolean)

    fun getToastDuration():Int
    fun setToastDuration(durationInMS: Int)

    fun setLatestLikeSort(type: Sort)
    fun getLatestLikeSort(): Sort

    fun setLatestCollectionSort(collectionId: Long?, type: Sort)
    fun getLatestCollectionSort(collectionId: Long?): Sort
}