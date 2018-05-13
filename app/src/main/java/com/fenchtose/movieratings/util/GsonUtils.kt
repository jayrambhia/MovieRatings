package com.fenchtose.movieratings.util

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
inline fun <reified T> Gson.fromJson(jsonElement: JsonElement) = this.fromJson<T>(jsonElement, object: TypeToken<T>() {}.type)