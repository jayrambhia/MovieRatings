package com.fenchtose.movieratings.features.tts

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie
import java.util.Locale

class Speaker(context: Context): TextToSpeech.OnInitListener {

    private val context = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ready = false
    private var currentMovie: Movie? = null

    override fun onInit(status: Int) {
        ready = if (status == TextToSpeech.SUCCESS) {
            val av = tts?.setLanguage(Locale.US)
            av == TextToSpeech.LANG_AVAILABLE || av == TextToSpeech.LANG_COUNTRY_AVAILABLE || av == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
        } else {
            false
        }

        if (ready) {
            val movie = currentMovie
            if (movie != null) {
                currentMovie = null
                talk(movie)
            }
        }
    }

    fun talk(content: String) {
        val tts = init()
        if (ready) {
            if (BuildConfig.DEBUG) {
                Log.d("Speaker", "speak: $content")
            }
            if (Build.VERSION.SDK_INT >= 21) {
                tts.speak(content, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                @Suppress("DEPRECATION")
                tts.speak(content, TextToSpeech.QUEUE_FLUSH, null)
            }
        }
    }

    fun talk(movie: Movie) {
        movie.takeIf { it.ratings.size > 0 && movie.imdbId != currentMovie?.imdbId }?.run {
            talk(context.getString(R.string.rating_tts, title, ratings[0].value.split("/")[0]))
            currentMovie = this
        }
    }

    private fun init(): TextToSpeech {
        if (tts == null) {
            tts = TextToSpeech(context, this)
            ready = false
        }
        return tts!!
    }

    fun shutdown() {
        ready = false
        tts?.shutdown()
        tts = null

        if (BuildConfig.DEBUG) {
            Log.d("Speaker", "shut down")
        }
    }

}