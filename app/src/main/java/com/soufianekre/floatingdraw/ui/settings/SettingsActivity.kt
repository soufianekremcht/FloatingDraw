package com.soufianekre.floatingdraw.ui.settings

import android.os.Bundle
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity :BaseActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadFragment(SettingsFragment())
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container,fragment)
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}