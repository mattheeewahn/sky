package com.skytrace.app.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * CelesTrak TLE data API.
 * Provides Two-Line Element sets for satellite tracking.
 * Base URL: https://celestrak.org
 */
interface CelesTrakApi {

    @GET("/NORAD/elements/gp.php")
    suspend fun getTLEByGroup(
        @Query("GROUP") group: String,
        @Query("FORMAT") format: String = "tle"
    ): Response<String>

    @GET("/NORAD/elements/gp.php")
    suspend fun getTLEByName(
        @Query("NAME") name: String,
        @Query("FORMAT") format: String = "tle"
    ): Response<String>

    @GET("/NORAD/elements/gp.php")
    suspend fun getTLEByNoradId(
        @Query("CATNR") noradId: Int,
        @Query("FORMAT") format: String = "tle"
    ): Response<String>

    companion object {
        const val BASE_URL = "https://celestrak.org"

        // Common satellite groups
        const val GROUP_STATIONS = "stations"
        const val GROUP_VISUAL = "visual"
        const val GROUP_ACTIVE = "active"
        const val GROUP_STARLINK = "starlink"
        const val GROUP_GPS = "gps-ops"
        const val GROUP_WEATHER = "weather"
        const val GROUP_SCIENCE = "science"
        const val GROUP_DEBRIS_COSMOS = "cosmos-2251-debris"
        const val GROUP_DEBRIS_IRIDIUM = "iridium-33-debris"
    }
}
