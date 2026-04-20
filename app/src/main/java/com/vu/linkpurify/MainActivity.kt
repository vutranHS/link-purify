package com.vu.linkpurify

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
    
    private var lastClipboardContent: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        val inputEdit = findViewById<TextInputEditText>(R.id.test_input_edit)
        val btnTest = findViewById<Button>(R.id.btn_test)
        val btnPasteClean = findViewById<Button>(R.id.btn_paste_clean)
        val switchAutoOpen = findViewById<MaterialSwitch>(R.id.switch_auto_open)
        val switchAutoClipboard = findViewById<MaterialSwitch>(R.id.switch_auto_clipboard)
        
        // Language Switcher
        val btnChangeLanguage = findViewById<View>(R.id.btn_change_language)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tv_current_language)
        
        // Update current language text
        val currentLang = prefs.getString("Locale.Helper.Selected.Language", "vi")
        tvCurrentLanguage.text = if (currentLang == "vi") getString(R.string.lang_vi) else getString(R.string.lang_en)

        btnChangeLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Set initial state
        switchAutoOpen.isChecked = prefs.getBoolean("auto_open", false)
        switchAutoClipboard.isChecked = prefs.getBoolean("auto_clipboard", true)
        
        switchAutoOpen.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_open", isChecked).apply()
        }

        switchAutoClipboard.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_clipboard", isChecked).apply()
        }

        switchAutoClipboard.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_clipboard", isChecked).apply()
        }

        val containerResults = findViewById<LinearLayout>(R.id.container_results)

        btnTest.setOnClickListener {
            val text = inputEdit.text?.toString() ?: ""
            val urls = LinkPurifyEngine.extractAllUrls(text)
            if (urls.isNotEmpty()) {
                processUrls(urls, btnTest, containerResults)
            } else {
                Toast.makeText(this, "No links found", Toast.LENGTH_SHORT).show()
            }
        }

        btnPasteClean.setOnClickListener {
            checkAndPasteClipboard(true)
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (prefs.getBoolean("auto_clipboard", true)) {
            checkAndPasteClipboard(false)
        }
    }

    private fun checkAndPasteClipboard(manual: Boolean) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val pasteData = item?.text?.toString() ?: ""
            
            // Check if it's the same as last time to avoid spamming
            if (!manual && pasteData == lastClipboardContent) return
            lastClipboardContent = pasteData
            
            val urls = LinkPurifyEngine.extractAllUrls(pasteData)
            
            if (urls.isNotEmpty()) {
                val inputEdit = findViewById<TextInputEditText>(R.id.test_input_edit)
                inputEdit.setText(urls[0]) // Put the first one in the box as usual
                
                if (manual) {
                    val btnTest = findViewById<Button>(R.id.btn_test)
                    val containerResults = findViewById<LinearLayout>(R.id.container_results)
                    processUrls(urls, btnTest, containerResults)
                } else {
                    Toast.makeText(this, getString(R.string.msg_clipboard_detected), Toast.LENGTH_SHORT).show()
                }
            } else if (manual) {
                Toast.makeText(this, "No link found in clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processUrls(urls: List<String>, btn: Button, container: LinearLayout) {
        lifecycleScope.launch {
            btn.isEnabled = false
            val originalText = btn.text
            btn.text = getString(R.string.btn_cleaning)
            
            container.removeAllViews()
            // Remove the "Title" TextView which is index 0 or 1. Actually we should keep the title.
            // Better way: remove all except the first 2 children (Divider and Title)
            while (container.childCount > 2) {
                container.removeViewAt(container.childCount - 1)
            }
            container.visibility = View.VISIBLE

            urls.forEach { url ->
                val cleanUrl = withContext(Dispatchers.IO) {
                    LinkPurifyEngine.clean(url)
                }
                addResultItem(url, cleanUrl, container)
            }
            
            btn.isEnabled = true
            btn.text = originalText
        }
    }

    private fun addResultItem(originalUrl: String, cleanUrl: String, container: LinearLayout) {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.item_clean_result, container, false)
        
        val tvOriginal = itemView.findViewById<TextView>(R.id.tv_original_url)
        val tvUrl = itemView.findViewById<TextView>(R.id.tv_result_url)
        val btnCopy = itemView.findViewById<View>(R.id.btn_copy_item)
        val btnOpen = itemView.findViewById<View>(R.id.btn_open_item)
        
        tvOriginal.text = "From: $originalUrl"
        tvUrl.text = cleanUrl
        
        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Cleaned Link", cleanUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
        }
        
        btnOpen.setOnClickListener {
            openUrl(cleanUrl)
        }
        
        container.addView(itemView)
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
