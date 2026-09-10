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
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val IDENTIFY_JWT_A = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJhY2tlbmQifQ.eyJleHAiOjE3ODkxMjU4NzksImVtYWlsIjoiY2VydGFpbi1oZXJAbGF1cmV4dG9sZS5tYWlsb3NhdXIubmV0Iiwic2NvcGUiOiJWRVJJRklFRF9DT05TVU1FUiJ9.OG8tfOw4Vl_dGfU7jC_lOmx0VXCeYPkgOl3pRML5aHk"
        private const val IDENTIFY_JWT_B = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJhY2tlbmQifQ.eyJleHAiOjE3ODkxMjU4OTIsImVtYWlsIjoiZmluZ2VyLWNvbXBhc3NAbGF1cmV4dG9sZS5tYWlsb3NhdXIubmV0Iiwic2NvcGUiOiJWRVJJRklFRF9DT05TVU1FUiJ9.H8lnyckNOmTtU0CEv4jfx4pRKvej8_U02BzYzoSu3cc"
        private const val PROGRAM_URL = "https://refer.booksy.com/"
        private const val TAG = "ExtoleDemo"
    }

    @Inject
    lateinit var extoleFuture: FutureTask<Extole>

    @Volatile
    private var zone: Zone? = null

    private var jwtIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch {
            val extole = withContext(Dispatchers.IO) { extoleFuture.get() }

            logIdentity(extole, "after init")
            switchIdentity(extole)

            runOnUiThread {
                findViewById<Button>(R.id.menu_item).setOnClickListener {
                    logIdentity(extole, "menu_item tap")
                    GlobalScope.launch { zone?.tap() }
                }

                findViewById<Button>(R.id.tap).setOnClickListener {
                    GlobalScope.launch {
                        switchIdentity(extole)
                        logIdentity(extole, "tap after switch")
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

    private suspend fun switchIdentity(extole: Extole) {
        val jwts = listOf(IDENTIFY_JWT_A, IDENTIFY_JWT_B)
        val jwt = jwts[jwtIndex % jwts.size]
        jwtIndex++
        if (jwt.isBlank()) {
            Log.w(TAG, "JWT #$jwtIndex is not set, keeping the current identity")
            return
        }
        extole.identifyJwt(jwt)
        logIdentity(extole, "after identifyJwt #$jwtIndex")
    }

    private fun logIdentity(extole: Extole, stage: String) {
        Log.i(TAG, "$stage: access_token=${(extole as ExtoleInternal).getAccessToken()}")
        Log.i(TAG, "$stage: cookies=${CookieManager.getInstance().getCookie(PROGRAM_URL)}")
    }
}
