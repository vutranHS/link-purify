package com.vu.linkpurify

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*

/**
 * A transparent activity that intercepts affiliate links, cleans them, and redirects.
 * Shows a review UI if auto-open is disabled.
 */
class CleanActivity : AppCompatActivity() {

    private var cleanedUrl: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show the review UI early (in a loading state) to prevent the "no activity" or "transparent hang" feel
        setContentView(R.layout.activity_clean)
        findViewById<TextView>(R.id.tv_clean_url).text = getString(R.string.msg_cleaning)
        findViewById<View>(android.R.id.content).visibility = View.VISIBLE
        
        // Disable buttons while loading
        findViewById<MaterialButton>(R.id.btn_open).isEnabled = false
        
        val inputUrl = when (intent.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                } else null
            }
            Intent.ACTION_VIEW -> {
                intent.dataString
            }
            else -> null
        }

        if (inputUrl != null) {
            // Check if it's a shopping link. If not, and we are acting as a general browser, forward silently.
            if (!LinkPurifyEngine.isAffiliateLink(inputUrl)) {
                silentForward(inputUrl)
                return
            }
            processUrl(inputUrl)
        } else {
            finish()
        }
    }

    private fun silentForward(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            // Find another app that can handle this, excluding ourselves to avoid loops
            val packageManager = packageManager
            val activities = packageManager.queryIntentActivities(intent, 0)
            val otherActivity = activities.find { it.activityInfo.packageName != packageName }
            
            if (otherActivity != null) {
                intent.setPackage(otherActivity.activityInfo.packageName)
                startActivity(intent)
            } else {
                // If no other handler, just let the standard chooser handle it (might include us, but better than doing nothing)
                startActivity(Intent.createChooser(intent, "Open with..."))
            }
        } catch (e: Exception) {
            // Fallback
        }
        finish()
    }

    private fun processUrl(url: String) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    LinkPurifyEngine.clean(url)
                }
                cleanedUrl = result
                
                val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
                val autoOpen = prefs.getBoolean("auto_open", false)
                
                if (autoOpen) {
                    openUrl(result)
                    finish()
                } else {
                    updateReviewUI()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CleanActivity, getString(R.string.msg_error_process), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateReviewUI() {
        findViewById<TextView>(R.id.tv_clean_url).text = cleanedUrl ?: ""
        findViewById<MaterialButton>(R.id.btn_open).isEnabled = true
        
        findViewById<MaterialButton>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
        
        findViewById<MaterialButton>(R.id.btn_open).setOnClickListener {
            cleanedUrl?.let { openUrl(it) }
            finish()
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            // Important: Don't exclude from recents if we want the user to find the browser/product app later.
            // But keep NEW_TASK for non-activity contexts.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.msg_error_open), Toast.LENGTH_SHORT).show()
        }
    }
}
