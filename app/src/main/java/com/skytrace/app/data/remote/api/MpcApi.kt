package com.skytrace.app.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Minor Planet Center API.
 * Provides asteroid and comet orbital data.
 * Base URL: https://www.minorplanetcenter.net
 */
interface MpcApi {

    /**
     * Query MPCORB database for asteroid orbital elements.
     * Returns asteroid data in JSON format.
     */
    @GET("/web_service/search_orbits_json")
    suspend fun searchOrbits(
        @Query("name") name: String? = null,
        @Query("designation") designation: String? = null,
        @Query("number") number: Int? = null,
        @Query("limit") limit: Int = 20
    ): Response<String>

    /**
     * Get NEO Confirmation Page objects.
     * These are recently reported objects needing confirmation.
     */
    @GET("/iau/NEO/toconfirm_tabular.html")
    suspend fun getNeoConfirmationPage(): Response<String>

    /**
     * Get observable NEOs.
     */
    @GET("/iau/lists/Observable.html")
    suspend fun getObservableNeos(): Response<String>

    /**
     * Fetch MPCORB extended data (large file, use for sync).
     */
    @GET
    suspend fun fetchMpcorbData(@Url url: String): Response<String>

    companion object {
        const val BASE_URL = "https://www.minorplanetcenter.net"
        const val MPCORB_EXTENDED_URL = "https://www.minorplanetcenter.net/iau/MPCORB/MPCORB.DAT"
        const val NEO_BRIGHT_URL = "https://www.minorplanetcenter.net/iau/lists/LastBrightNEOs.html"
    }
}
