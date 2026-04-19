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
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

/**
 * Main dashboard for the app. Provides instructions and a manual link tester.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        val inputEdit = findViewById<TextInputEditText>(R.id.test_input_edit)
        val btnTest = findViewById<Button>(R.id.btn_test)
        val switchAutoOpen = findViewById<MaterialSwitch>(R.id.switch_auto_open)

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
                CoroutineScope(Dispatchers.Main).launch {
                    btnTest.isEnabled = false
                    btnTest.text = "Cleaning..."
                    
                    val cleanUrl = withContext(Dispatchers.IO) {
                        LinkCleaner.clean(url)
                    }
                    
                    lastTestedUrl = cleanUrl
                    tvTestResult.text = cleanUrl
                    layoutTestResult.visibility = View.VISIBLE
                    
                    btnTest.isEnabled = true
                    btnTest.text = "Clean Link"
                }
            } else {
                Toast.makeText(this, "Please paste a link first", Toast.LENGTH_SHORT).show()
            }
        }

        btnOpenTest.setOnClickListener {
            lastTestedUrl?.let { openUrl(it) }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }
}
