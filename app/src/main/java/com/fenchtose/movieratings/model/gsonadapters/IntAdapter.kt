package com.fenchtose.movieratings.model.gsonadapters

import com.fenchtose.movieratings.BuildConfig
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class IntAdapter: TypeAdapter<Int>() {
    override fun write(out: JsonWriter, value: Int?) {
        if (value == null) {
            out.nullValue()
            return
        }

        out.value(value)
    }

    override fun read(reader: JsonReader): Int {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return -1
        }

        val stringVal = reader.nextString()
        if (stringVal.isEmpty() || stringVal.toLowerCase().equals("n/a")) {
            return -1
        }

        return try {
            stringVal.toInt()
        } catch (e: NumberFormatException) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            -1
        }

    }

}