package com.fenchtose.movieratings.model.db.recentlyBrowsed

import com.fenchtose.movieratings.model.RecentlyBrowsed

interface RecentlyBrowsedStore {
    fun update(data: RecentlyBrowsed)
}