package com.extole.androidsdk

import android.content.Context
import com.extole.android.sdk.Extole
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Configuration {
    val threadPool = Executors.newFixedThreadPool(1)

    @Singleton
    @Provides
    fun extole(@ApplicationContext context: Context): FutureTask<Extole> {
        val extoleFuture = FutureTask<Extole> {
            runBlocking {
                Extole.init(
                    programDomain = "refer.booksy.com",
                    context = context,
                    appName = "extole-mobile-test",
                    data = mapOf("version" to "1.0"),
                    listenToEvents = true
                )
            }
        }
        threadPool.submit(extoleFuture)
        return extoleFuture
    }
}
