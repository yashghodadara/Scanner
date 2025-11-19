package com.example.scanner.presentetion

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MenuInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanner.R
import com.example.scanner.data.BrowserItem
import com.example.scanner.databinding.ActivityWebSearchBinding
import com.example.scanner.util.BaseActivity
import com.example.scanner.util.Constants
import com.example.scanner.util.PreferenceManager

class WebSearchActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ActivityWebSearchBinding
    private var selectedSearchEngine = ""
    private var searchText : String? = ""
    private lateinit var pref: PreferenceManager
    private lateinit var adapter: WebBrowserAdapter
    val browserList = mutableListOf(
        BrowserItem(
            "Google",
            R.drawable.ic_icon_google
        ),
        BrowserItem(
            "Bing",
            R.drawable.ic_icon_bing
        ),
        BrowserItem(
            "Yahoo",
            R.drawable.ic_icon_yahoo
        ),
        BrowserItem(
            "DuckDuckGo",
            R.drawable.ic_icon_duck
        ),
        BrowserItem(
            "Ecosia",
            R.drawable.ic_icon_ecosia
        ),
        BrowserItem(
            "Yandex",
            R.drawable.ic_icon_yandex
        ),
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWebSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUIAAndSetStatusBarColor()
        pref = PreferenceManager(this)
        selectedSearchEngine = pref.getString(Constants.SELECTED_SEARCH_ENGINE)
        searchText = intent.getStringExtra(Constants.SEARCH_TEXT)
        if (selectedSearchEngine.isEmpty()) selectedSearchEngine = "Google"

        setupBrowserList()
        setupWebView()
        loadSelectedBrowserPage(selectedSearchEngine, searchText)
        binding.btnReload.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.btnFaq.setOnClickListener(this)
        binding.btnMore.setOnClickListener(this)
    }

    private fun setupBrowserList() {
        adapter = WebBrowserAdapter(browserList, selectedSearchEngine) { browser ->
            selectedSearchEngine = browser.name
            runOnUiThread {
                loadSelectedBrowserPage(browser.name, searchText)
            }
        }
        binding.rvBrowserList.adapter = adapter
        binding.rvBrowserList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupWebView() {
        val settings = binding.webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportZoom(true)
        settings.loadsImagesAutomatically = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
                binding.webView.visibility = View.GONE
                binding.progressBar.progress = 0
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                    binding.webView.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.webView.visibility = View.GONE
                }
            }
        }
    }

    private fun loadSelectedBrowserPage(browserName: String, query: String?) {
        val encodedQuery = Uri.encode(query?.trim())
        val url = when (browserName) {
            getString(R.string.google) -> "https://www.google.com/search?q=$encodedQuery"
            getString(R.string.bing) -> "https://www.bing.com/search?q=$encodedQuery"
            getString(R.string.yahoo) -> "https://search.yahoo.com/search?p=$encodedQuery"
            getString(R.string.duckduckgo) -> "https://duckduckgo.com/?q=$encodedQuery"
            getString(R.string.ecosia) -> "https://www.ecosia.org/=$encodedQuery"
            getString(R.string.yandex) -> "https://yandex.com/search/?text=$encodedQuery"
            else -> "https://www.google.com/search?q=$encodedQuery"
        }
        binding.webView.stopLoading()
        binding.webView.clearCache(true)
        binding.webView.clearHistory()
        binding.webView.loadUrl(url)
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun showPopupMenu(anchor: View) {
        val popup = PopupMenu(ContextThemeWrapper(this, R.style.CustomPopupMenu), anchor, Gravity.END, 0, R.style.CustomPopupMenu)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_web_options, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_copy -> {
                    val currentUrl = binding.webView.url
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("URL", currentUrl)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.action_share -> {
                    val currentUrl = binding.webView.url
                    if (currentUrl != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, currentUrl)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share via"))
                    } else {
                        Toast.makeText(this, "No URL to share", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                else -> false
            }
        }
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val menuPopupHelper = fieldPopup.get(popup)
            val method = menuPopupHelper.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
            method.invoke(menuPopupHelper, true)
            menuPopupHelper.javaClass.getDeclaredMethod("show", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(menuPopupHelper, 0, 0)
        } catch (e: Exception) {
            popup.show()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnReload -> binding.webView.reload()
            R.id.btnCancel -> finish()
            R.id.btnFaq -> {
                val intent = Intent(this, FAQActivity::class.java)
                startActivity(intent)
            }
            R.id.btnMore ->  showPopupMenu(v)
        }
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUIAAndSetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(
                    WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }

        val bgColor = ContextCompat.getColor(this, R.color.bg_color)
        window.statusBarColor = bgColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}