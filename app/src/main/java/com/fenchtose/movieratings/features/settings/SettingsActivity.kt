package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import com.fenchtose.movieratings.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_layout)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment()).commit()
        (findViewById<Toolbar>(R.id.toolbar)).setTitle(R.string.settings_header)
    }
}
