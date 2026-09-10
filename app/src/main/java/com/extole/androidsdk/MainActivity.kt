package com.extole.androidsdk

import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.extole.android.sdk.Extole
import com.extole.android.sdk.Zone
import com.extole.android.sdk.impl.ExtoleInternal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private data class Identity(val label: String, val jwt: String)

    companion object {
        private const val IDENTIFY_JWT_LAUR = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJhY2tlbmQifQ.eyJleHAiOjE3ODkxMjU4NzksImVtYWlsIjoiY2VydGFpbi1oZXJAbGF1cmV4dG9sZS5tYWlsb3NhdXIubmV0Iiwic2NvcGUiOiJWRVJJRklFRF9DT05TVU1FUiJ9.OG8tfOw4Vl_dGfU7jC_lOmx0VXCeYPkgOl3pRML5aHk"
        private const val IDENTIFY_JWT_DAN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJhY2tlbmQifQ.eyJleHAiOjE3ODkxMjU4OTIsImVtYWlsIjoiZmluZ2VyLWNvbXBhc3NAbGF1cmV4dG9sZS5tYWlsb3NhdXIubmV0Iiwic2NvcGUiOiJWRVJJRklFRF9DT05TVU1FUiJ9.H8lnyckNOmTtU0CEv4jfx4pRKvej8_U02BzYzoSu3cc"
        private const val PROGRAM_URL = "https://refer.booksy.com/"
        private const val TAG = "ExtoleDemo"
    }

    private val identities = listOf(
        Identity("Laur", IDENTIFY_JWT_LAUR),
        Identity("Dan", IDENTIFY_JWT_DAN)
    )

    private val switchCount = AtomicInteger()

    @Inject
    lateinit var extoleFuture: FutureTask<Extole>

    @Volatile
    private var zone: Zone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch {
            val extole = withContext(Dispatchers.IO) { extoleFuture.get() }

            logIdentity(extole, "after init")
            switchIdentity(extole)

            runOnUiThread {
                findViewById<Button>(R.id.menu_item).setOnClickListener {
                    logIdentity(extole, "tap without switch")
                    GlobalScope.launch { tapZone() }
                }

                findViewById<Button>(R.id.tap).setOnClickListener {
                    GlobalScope.launch {
                        switchIdentity(extole)
                        tapZone()
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

    private suspend fun switchIdentity(extole: Extole) {
        val identity = identities[switchCount.getAndIncrement() % identities.size]
        Log.i(TAG, "switching identity to ${identity.label}")
        extole.identifyJwt(identity.jwt)
        logIdentity(extole, "identified as ${identity.label}")
        zone = runCatching { extole.fetchZone("mobile_cta").first }
            .onFailure { extole.getLogger().error("Unable to fetch mobile_cta zone: $it") }
            .getOrNull()
        Log.i(TAG, "zone for ${identity.label}: title=${zone?.get("title")}")
        runOnUiThread {
            zone?.let {
                findViewById<Button>(R.id.menu_item).setText(
                    it.get("title").toString(), TextView.BufferType.NORMAL
                )
            }
        }
    }

    private suspend fun tapZone() {
        val tapped = zone
        if (tapped == null) {
            Log.w(TAG, "no zone to tap, mobile_cta was not fetched")
            return
        }
        Log.i(TAG, "tapping zone")
        tapped.tap()
    }

    private fun logIdentity(extole: Extole, stage: String) {
        Log.i(TAG, "$stage: access_token=${(extole as ExtoleInternal).getAccessToken()}")
        Log.i(TAG, "$stage: cookies=${CookieManager.getInstance().getCookie(PROGRAM_URL)}")
    }
}
