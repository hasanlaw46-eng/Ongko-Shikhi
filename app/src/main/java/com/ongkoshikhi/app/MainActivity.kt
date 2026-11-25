
package com.ongkoshikhi.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    private lateinit var webView: WebView

    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                this@MainActivity.fileUploadCallback?.onReceiveValue(null)
                this@MainActivity.fileUploadCallback = null

                if (filePathCallback == null) {
                    return false
                }

                this@MainActivity.fileUploadCallback = filePathCallback

                val intent = try {
                    fileChooserParams?.createIntent()
                        ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                } catch (e: Exception) {
                    this@MainActivity.fileUploadCallback?.onReceiveValue(null)
                    this@MainActivity.fileUploadCallback = null
                    return false
                }

                return try {
                    startActivityForResult(
                        intent,
                        FILE_CHOOSER_REQUEST_CODE
                    )
                    true
                } catch (e: ActivityNotFoundException) {
                    this@MainActivity.fileUploadCallback?.onReceiveValue(null)
                    this@MainActivity.fileUploadCallback = null
                    false
                }
            }
        }

        setContentView(webView)

        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {

            val callback = fileUploadCallback ?: return

            val results = if (resultCode == RESULT_OK) {
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    data
                )
            } else {
                null
            }

            callback.onReceiveValue(results)
            fileUploadCallback = null
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        fileUploadCallback?.onReceiveValue(null)
        fileUploadCallback = null
        webView.destroy()
        super.onDestroy()
    }
}
