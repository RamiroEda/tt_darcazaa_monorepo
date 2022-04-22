package mx.ipn.upiiz.darcazaa.data.repositories

import mx.ipn.upiiz.darcazaa.data.models.History
import mx.ipn.upiiz.darcazaa.data.models.PreferenceKeys
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import retrofit2.http.GET
import retrofit2.http.Path

interface HistoryRepository {
    suspend fun historyByHash(hash: String): List<History>
}

class HistoryRetrofitRepository(
    val preferences: UserPreferences,
    val builder: retrofit2.Retrofit.Builder
) : HistoryRepository {
    private interface Retrofit : HistoryRepository {
        @GET("/history")
        override suspend fun historyByHash(@Path("hash") hash: String): List<History>
    }

    override suspend fun historyByHash(hash: String): List<History> {
        return getRetrofit().create(Retrofit::class.java).historyByHash(hash)
    }

    private fun getRetrofit() = builder.baseUrl(
        "http://${
            preferences.get(
                PreferenceKeys.Url,
                "192.168.1.1"
            )
        }"
    ).build()
}