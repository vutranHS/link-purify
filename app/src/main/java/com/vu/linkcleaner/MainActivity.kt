package com.vu.linkcleaner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

/**
 * Main dashboard for the app. Provides instructions and a manual link tester.
 */
class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        val inputEdit = findViewById<TextInputEditText>(R.id.test_input_edit)
        val btnTest = findViewById<Button>(R.id.btn_test)
        val switchAutoOpen = findViewById<MaterialSwitch>(R.id.switch_auto_open)
        
        // Language Switcher
        val btnChangeLanguage = findViewById<View>(R.id.btn_change_language)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tv_current_language)
        
        // Update current language text
        val currentLang = prefs.getString("Locale.Helper.Selected.Language", "vi")
        tvCurrentLanguage.text = if (currentLang == "vi") getString(R.string.lang_vi) else getString(R.string.lang_en)

        btnChangeLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Set initial state (default to false as per user request)
        switchAutoOpen.isChecked = prefs.getBoolean("auto_open", false)
        
        switchAutoOpen.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_open", isChecked).apply()
        }

        val layoutTestResult = findViewById<View>(R.id.layout_test_result)
        val tvTestResult = findViewById<TextView>(R.id.tv_test_result)
        val btnOpenTest = findViewById<Button>(R.id.btn_open_test)
        var lastTestedUrl: String? = null

        btnTest.setOnClickListener {
            val url = inputEdit.text?.toString()
            if (!url.isNullOrBlank()) {
                lifecycleScope.launch {
                    btnTest.isEnabled = false
                    btnTest.text = getString(R.string.btn_cleaning)
                    
                    val cleanUrl = withContext(Dispatchers.IO) {
                        LinkCleaner.clean(url)
                    }
                    
                    lastTestedUrl = cleanUrl
                    tvTestResult.text = cleanUrl
                    layoutTestResult.visibility = View.VISIBLE
                    
                    btnTest.isEnabled = true
                    btnTest.text = getString(R.string.btn_clean)
                }
            } else {
                Toast.makeText(this, getString(R.string.loading), Toast.LENGTH_SHORT).show()
            }
        }

        btnOpenTest.setOnClickListener {
            lastTestedUrl?.let { openUrl(it) }
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(getString(R.string.lang_vi), getString(R.string.lang_en))
        val codes = arrayOf("vi", "en")
        
        val currentLang = getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("Locale.Helper.Selected.Language", "vi")
        val checkedItem = if (currentLang == "vi") 0 else 1

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.setting_language))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                LocaleHelper.setLocale(this, codes[which])
                dialog.dismiss()
                recreate() // Restart activity to apply language
            }
            .setNegativeButton(getString(R.string.btn_ignore), null)
            .show()
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.msg_error_open), Toast.LENGTH_SHORT).show()
        }
    }
}
