package com.fenchtose.movieratings.widgets.pagesection

interface PageSection<in DATA> {
    fun setContent(content: DATA)
}