package dev.yveskalume.newsapp.data.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dev.yveskalume.newsappp.shared.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val ktorModule = module {
    singleOf(::provideKtorEngine)
    singleOf(::provideKtoClient)
}

private fun provideKtorEngine(context: Context): HttpClientEngine {
    return OkHttp.create {
        addInterceptor(
            interceptor = ChuckerInterceptor.Builder(context)
                .build()
        )
    }
}

private fun provideKtoClient(engine: HttpClientEngine): HttpClient {

    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        defaultRequest {
            url("https://newsapi.org/v2/")
            val params = url.parameters
            params.append("apiKey", BuildConfig.NEWS_API_KEY)
            params.append("language", "en")
        }
    }
}