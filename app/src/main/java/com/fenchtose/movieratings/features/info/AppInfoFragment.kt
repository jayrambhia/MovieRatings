package com.fenchtose.movieratings.features.info

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.util.IntentUtils

class AppInfoFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.info_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById(R.id.rate_view).setOnClickListener {
            IntentUtils.openPlaystore(context)
        }

        view.findViewById(R.id.share_view).setOnClickListener {
            IntentUtils.openShareIntent(context, "Have you met Flutter? It shows movie ratings on your screen when you're browsing Netflix. Get the app. " + IntentUtils.PLAYSTORE_URL)
        }

        view.findViewById(R.id.credit_view).setOnClickListener {
            showCreditsDialog()
        }
    }

    private fun showCreditsDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.credit_dialog_title)
                .setMessage(R.string.credit_dialog_content)
                .show()
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.search_page_title
    }

    class AppInfoPath: RouterPath<AppInfoFragment>() {
        override fun createFragmentInstance(): AppInfoFragment {
            return AppInfoFragment()
        }
    }
}