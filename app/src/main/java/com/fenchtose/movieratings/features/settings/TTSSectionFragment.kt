package com.fenchtose.movieratings.features.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.PackageUtils

class TTSSectionFragment: BaseFragment() {

    private val CHECK_TTS = 12
    private var preferences: UserPreferences? = null
    private var updatePublisher: PreferenceUpdater? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_tts_section_page_header
    override fun screenName() = GaScreens.SETTINGS_TTS_SECTION

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_tts_section_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val preferences = SettingsPreferences(requireContext())
        this.preferences = preferences

        updatePublisher = PreferenceUpdater(view as ViewGroup)

        val ttsToggle = view.findViewById<SwitchCompat>(R.id.tts_toggle)
        ttsToggle.isChecked = preferences.isSettingEnabled(UserPreferences.TTS_AVAILABLE) && preferences.isSettingEnabled(UserPreferences.USE_TTS)
        ttsToggle.setOnCheckedChangeListener {
            toggle, isChecked ->
            run {
                if (!isChecked) {
                    preferences.setEnabled(UserPreferences.USE_TTS, false)
                    updatePublisher?.show(UserPreferences.USE_TTS)
                    return@setOnCheckedChangeListener
                }

                if (preferences.isSettingEnabled(UserPreferences.TTS_AVAILABLE)) {
                    preferences.setEnabled(UserPreferences.USE_TTS, true)
                    updatePublisher?.show(UserPreferences.USE_TTS)
                    return@setOnCheckedChangeListener
                }

                toggle.isChecked = false
                checkForTTS()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatePublisher?.release()
    }

    private fun checkForTTS() {
        val intent = Intent().setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        if (PackageUtils.isIntentCallabale(requireContext(), intent)) {
            startActivityForResult(intent, CHECK_TTS)
        } else {
            showSnackbar(R.string.settings_install_tts_unsupported)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_TTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                preferences?.setEnabled(UserPreferences.TTS_AVAILABLE, true)
            } else {
                showTtsInstallDialog()
            }
        }
    }

    private fun showTtsInstallDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.settings_install_tts_dialog_title)
                .setMessage(R.string.settings_install_tts_dialog_content)
                .setPositiveButton(R.string.settings_install_tts_dialog_cta) { _, _ -> installTTS() }
                .setNeutralButton(R.string.settings_install_tts_dialog_later) {d, _ -> d.dismiss()}
                .show()
    }

    private fun installTTS() {
        val intent = Intent().setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        if (PackageUtils.isIntentCallabale(requireContext(), intent)) {
            startActivity(intent)
        } else {
            showSnackbar(R.string.settings_install_tts_unsupported)
        }
    }

    class TTSSettingsPath: RouterPath<TTSSectionFragment>() {
        override fun createFragmentInstance(): TTSSectionFragment {
            return TTSSectionFragment()
        }
    }

}