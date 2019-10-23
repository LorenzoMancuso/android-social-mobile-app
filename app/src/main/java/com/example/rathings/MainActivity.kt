package com.example.rathings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.squareup.picasso.Picasso
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Picasso.get().setIndicatorsEnabled(true)

        val config = resources.configuration
        val language = PreferenceManager.getDefaultSharedPreferences(this).getString("app_language_id", Locale.getDefault().toLanguageTag())
        val locale = Locale(language)
        Log.d("[LANGUAGE]", language)
        config.setLocale(locale)
        Locale.setDefault(locale)
        this.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Set to FALSE restarting activity to re-build change language helpers
        val editSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editSharedPreferences.putBoolean("restart", false)
        editSharedPreferences.apply()

        //go to login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

    }
}
