package api

import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(val assets: List<Asset>) {
    @Serializable
    data class Asset(
        val name: String,
        val browser_download_url: String,
    ) {
        val version: String?
            get() = name.split("voice-").getOrNull(1)?.replace(".msi", "")
    }
}
