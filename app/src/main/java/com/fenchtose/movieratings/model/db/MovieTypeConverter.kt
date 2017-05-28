package com.fenchtose.movieratings.model.db

import android.arch.persistence.room.TypeConverter
import com.fenchtose.movieratings.model.Rating
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class MovieTypeConverter {
    companion object {

        val gson : Gson = Gson()

        @TypeConverter
        fun fromString(data: String): ArrayList<Rating> {
            val json = gson.fromJson(data, JsonElement::class.java).asJsonArray
            val list = ArrayList<Rating>()
            json.mapTo(list) { Rating(it.asJsonObject.get("Source").asString, it.asJsonObject.get("Value").asString) }
            return list
        }

        @TypeConverter
        fun listToString(ratings: ArrayList<Rating>) : String {
            val json = JsonArray()
            for (rating in ratings) {
                val item = JsonObject()
                item.addProperty("Source", rating.source)
                item.addProperty("Value", rating.value)
                json.add(item)
            }

            return json.toString()
        }
    }
}