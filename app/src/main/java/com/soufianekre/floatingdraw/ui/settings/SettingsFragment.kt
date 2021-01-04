package com.soufianekre.floatingdraw.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.soufianekre.floatingdraw.R

class SettingsFragment : PreferenceFragmentCompat() {
    private val SETTINGS_KEY: String? = "settings_key"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_layout,SETTINGS_KEY)
    }
}