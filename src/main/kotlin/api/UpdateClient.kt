package api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object UpdateClient {
    private val httpClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun getLatestReleaseVersion(): GithubRelease.Asset? {
        val release: GithubRelease = httpClient.get("https://api.github.com/repos/videogreg93/voice/releases/latest").body()
        return release.assets.firstOrNull()
    }
}