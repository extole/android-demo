package com.extole.androidsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.extole.android.sdk.Extole
import javax.inject.Inject

class ExtoleWebView : AppCompatActivity() {

    @Inject
    lateinit var extole: Extole
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView = findViewById<WebView>(R.id.webview)
        extole.webView(webView)
    }
}