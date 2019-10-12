package com.fenchtose.movieratings.util

fun String?.emptyAsNull(): String? = if (this == null || length == 0) null else this