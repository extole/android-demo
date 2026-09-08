package com.extole.androidsdk

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.extole.android.sdk.Extole
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.FutureTask
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val IDENTIFY_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJhY2tlbmQifQ.eyJleHAiOjE3ODg5NjUzNDgsImVtYWlsIjoiZXhpc3Qtc3RyZW5ndGhAbGF1cmV4dG9sZS5tYWlsb3NhdXIubmV0Iiwic2NvcGUiOiJWRVJJRklFRF9DT05TVU1FUiJ9.DbQS252NX_odFPE8yAwLVJXtzDOqWNBc4LnasBDdIBk";
    }

    @Inject
    lateinit var extoleFuture: FutureTask<Extole>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch {
            val extole = withContext(Dispatchers.IO) { extoleFuture.get() }

            extole.identifyJwt(IDENTIFY_JWT)

            val zone = runCatching { extole.fetchZone("mobile_cta").first }
                .onFailure { extole.getLogger().error("Unable to fetch mobile_cta zone: $it") }
                .getOrNull()

            runOnUiThread {
                zone?.let {
                    findViewById<Button>(R.id.menu_item).setText(
                        it.get("title").toString(), TextView.BufferType.NORMAL
                    )
                }

                findViewById<Button>(R.id.menu_item).setOnClickListener {
                    GlobalScope.launch {
                        zone?.tap()
                    }
                }

                findViewById<Button>(R.id.tap).setOnClickListener {
                    GlobalScope.launch {
                        zone?.tap()
                    }
                }

                findViewById<Button>(R.id.deeplink).setOnClickListener {
                    GlobalScope.launch {
                        extole.sendEvent("deeplink", mapOf("extole_key" to "extole_value"))
                    }
                }
            }
        }
    }
}
