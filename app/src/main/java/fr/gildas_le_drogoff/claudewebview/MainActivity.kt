package fr.gildas_le_drogoff.claudewebview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    companion object {
        private val allowedDomains = listOf("claude.ai")

        // Uses a MutationObserver to handle dynamically loaded content.
        // Initially used for a specific WebView (e.g., Perplexity), kept for reuse on other sites.
        private val jsCacherBoutonsApp =
            """
            (function() {
                function masquer() {
                    document.querySelectorAll('button, a').forEach(function(el) {
                        var t = (el.innerText || '').toLowerCase();
                        if (t.includes("ouvrir dans l'application") || t.includes('open in app')) {
                            el.style.display = 'none';
                        }
                    });
                }
                masquer();
                new MutationObserver(masquer).observe(document.body, { childList: true, subtree: true });
            })();
            """.trimIndent()
    }

    private val urlToLoad = "https://claude.ai/new/"
    private var chatWebView: WebView? = null
    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    private val filePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val uri =
                if (result.resultCode == RESULT_OK) {
                    result.data?.dataString?.let { arrayOf(it.toUri()) }
                } else {
                    null
                }
            uploadMessage?.onReceiveValue(uri)
            uploadMessage = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(android.R.style.Theme_DeviceDefault_DayNight)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (chatWebView?.canGoBack() == true) {
                        chatWebView?.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )

        chatWebView =
            findViewById<WebView>(R.id.chatWebView).also { webView ->
                registerForContextMenu(webView)
                configurerWebView(webView)
                webView.loadUrl(urlToLoad)
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configurerWebView(webView: WebView) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowContentAccess = false
            allowFileAccess = false
            builtInZoomControls = false
            displayZoomControls = false
            setGeolocationEnabled(false)
            userAgentString = construireUA()
        }

        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                    if (msg.message().contains("NotAllowedError: Write permission denied.")) {
                        afficherToast(R.string.error_copy)
                        return true
                    }
                    return false
                }

                override fun onShowFileChooser(
                    view: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams,
                ): Boolean {
                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = filePathCallback
                    filePickerLauncher.launch(fileChooserParams.createIntent())
                    return true
                }
            }

        webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean {
                    val host = request.url.host ?: return true
                    return if (allowedDomains.none { host.endsWith(it) }) {
                        startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        true
                    } else {
                        false
                    }
                }

                override fun onPageFinished(
                    view: WebView,
                    url: String,
                ) {
                    super.onPageFinished(view, url)
                    view.evaluateJavascript(jsCacherBoutonsApp, null)
                }
            }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?,
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val result = chatWebView?.hitTestResult ?: return
        val url = result.extra ?: return

        if (result.type in
            listOf(
                WebView.HitTestResult.SRC_ANCHOR_TYPE,
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE,
            )
        ) {
            val host = url.toUri().host
            if (host != null && allowedDomains.none { host.endsWith(it) }) {
                afficherToast(R.string.url_copied)
            }
        }
    }

    private fun afficherToast(resId: Int) {
        runOnUiThread { Toast.makeText(this, resId, Toast.LENGTH_LONG).show() }
    }

    private fun construireUA(): String =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) " + "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Mobile Safari/537.36"
}
