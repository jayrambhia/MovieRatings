package com.fenchtose.movieratings.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.fenchtose.movieratings.MainActivity
import com.fenchtose.movieratings.R

class ImportActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.import_screen_title)

        val action = intent?.action
        val type = intent?.type

        var uri: Uri? = null
        if (action == Intent.ACTION_SEND && type == "text/plain") {
            uri = intent?.getParcelableExtra(Intent.EXTRA_STREAM)
        } else if (action == Intent.ACTION_VIEW && type == "text/plain") {
            uri = intent?.data
        }

        val fragment = ImportDataFragment()
        val bundle = Bundle()
        if (uri != null) {
            bundle.putParcelable(ImportDataFragment.EXTRA_URI, uri)
        }

        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()

        fragment.callback = object : ImportDataFragment.Callback {
            override fun openFlutter() {
                startActivity(Intent(this@ImportActivity, MainActivity::class.java))
                supportFinishAfterTransition()
            }

            override fun goBack() {
                supportFinishAfterTransition()
            }
        }
    }

}