package com.soufianekre.floatingdraw.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.soufianekre.floatingdraw.R

class SettingsFragment : PreferenceFragmentCompat(),Preference.OnPreferenceChangeListener {
    private val SETTINGS_KEY: String = "settings_layout_key"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_layout,SETTINGS_KEY)
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return false
    }
}