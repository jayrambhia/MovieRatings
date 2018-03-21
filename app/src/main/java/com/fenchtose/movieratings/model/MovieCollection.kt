package com.fenchtose.movieratings.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "COLLECTIONS")
class MovieCollection {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "COLLECTION_ID")
    var id: Long = 0

    @ColumnInfo(name = "COLLECTION_NAME")
    var name: String = ""

    @ColumnInfo(name = "CREATED_AT")
    var createdAt: Long = 0

    @ColumnInfo(name = "UPDATED_AT")
    var updatedAt: Long = 0

    @ColumnInfo(name = "IS_DELETED")
    var deleted: Int = 0

    companion object {
        fun create(name: String): MovieCollection {
            val collection = MovieCollection()
            collection.name = name
            collection.createdAt = System.currentTimeMillis()
            collection.updatedAt = collection.createdAt
            return collection
        }
    }
}