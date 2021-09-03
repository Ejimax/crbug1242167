package com.example.crbug1242167

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.format.Formatter
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.crbug1242167.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        val sharedPrefs = getSharedPreferences("main", Context.MODE_PRIVATE)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webview.webViewClient = WebViewClientImpl()
        binding.webview.webChromeClient = WebChromeClientImpl()
        @SuppressLint("SetJavaScriptEnabled")
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.settings.databaseEnabled = true
        binding.urlEdit.setText(sharedPrefs.getString(PREFS_KEY_URL, null))
        binding.urlEdit.doOnTextChanged { text, _, _, _ ->
            sharedPrefs.edit {
                putString(PREFS_KEY_URL, text?.toString())
            }
        }
        binding.originEdit.setText(sharedPrefs.getString(PREFS_KEY_ORIGIN, null))
        binding.originEdit.doOnTextChanged { text, _, _, _ ->
            sharedPrefs.edit {
                putString(PREFS_KEY_ORIGIN, text?.toString())
            }
        }
        val webStorage = WebStorage.getInstance()
        val cookieManager = CookieManager.getInstance()
        binding.loadButton.setOnClickListener {
            val url = binding.urlEdit.text.toString()
            binding.webview.loadUrl(url)
            Timber.d("loadUrl($url)")
        }
        binding.deleteOriginButton.setOnClickListener {
            val origin = binding.originEdit.text.toString()
            webStorage.deleteOrigin(origin)
            Timber.d("webStorage.deleteOrigin($origin)")
        }
        binding.getUsageButton.setOnClickListener {
            val origin = binding.originEdit.text.toString()
            webStorage.getUsageForOrigin(origin) { usage ->
                val fileSize = Formatter.formatFileSize(this, usage)
                Timber.d("Usage for $origin: $usage ($fileSize)")
            }
            Timber.d("webStorage.getUsageForOrigin($origin)")
        }
        binding.deleteAllDataButton.setOnClickListener {
            webStorage.deleteAllData()
            Timber.d("webStorage.deleteAllData()")
        }
        binding.deleteCookiesButton.setOnClickListener {
            cookieManager.removeAllCookies {
                Timber.d("removeAllCookies finished")
            }
            Timber.d("cookieManager.removeAllCookies()")
        }
        binding.getOriginsButton.setOnClickListener {
            webStorage.getOrigins {
                @Suppress("UNCHECKED_CAST")
                val origins = it as Map<String, WebStorage.Origin>
                for ((_, value) in origins) {
                    val fileSize = Formatter.formatFileSize(this, value.usage)
                    Timber.d("Usage for ${value.origin}: ${value.usage} ($fileSize)")
                }
            }
        }
        binding.deleteCacheButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                Timber.d("start cacheDir.deleteRecursively()")
                cacheDir.deleteRecursively()
                Timber.d("finish cacheDir.deleteRecursively()")
            }
        }
        binding.restartButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    sharedPrefs.edit().commit()
                }
                delay(200)
                finishAffinity()
                val intent = KillProcessActivity.createIntent(this@MainActivity)
                startActivity(intent)
            }
        }
    }

    private inner class WebViewClientImpl : WebViewClient()

    private inner class WebChromeClientImpl : WebChromeClient()

    companion object {

        private const val PREFS_KEY_URL = "url"

        private const val PREFS_KEY_ORIGIN = "origin"
    }
}
