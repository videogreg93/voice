package api

import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(val assets: List<Asset>) {
    @Serializable
    data class Asset(val name: String)
}
