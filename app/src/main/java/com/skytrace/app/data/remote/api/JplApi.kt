package com.skytrace.app.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * NASA JPL Small-Body Database and Horizons API.
 * Provides asteroid/comet lookup and ephemeris data.
 */
interface JplApi {

    /**
     * Search the JPL Small-Body Database.
     * Base URL: https://ssd-api.jpl.nasa.gov
     */
    @GET("/sbdb.api")
    suspend fun lookupSmallBody(
        @Query("sstr") searchString: String,
        @Query("full-prec") fullPrecision: Boolean = false
    ): Response<String>

    /**
     * Close approach data from JPL.
     */
    @GET("/cad.api")
    suspend fun getCloseApproaches(
        @Query("date-min") dateMin: String? = null,
        @Query("date-max") dateMax: String? = null,
        @Query("dist-max") distMax: String = "0.2",
        @Query("sort") sort: String = "date",
        @Query("limit") limit: Int = 20
    ): Response<String>

    /**
     * JPL Horizons ephemeris lookup.
     * Returns positional data for a body at given time/location.
     */
    @GET("/horizons.api")
    suspend fun getEphemeris(
        @Query("format") format: String = "json",
        @Query("COMMAND") command: String,
        @Query("OBJ_DATA") objData: String = "NO",
        @Query("MAKE_EPHEM") makeEphem: String = "YES",
        @Query("EPHEM_TYPE") ephemType: String = "OBSERVER",
        @Query("CENTER") center: String, // 'coord@399' for Earth surface
        @Query("COORD_TYPE") coordType: String = "GEODETIC",
        @Query("SITE_COORD") siteCoord: String, // 'lon,lat,alt'
        @Query("START_TIME") startTime: String,
        @Query("STOP_TIME") stopTime: String,
        @Query("STEP_SIZE") stepSize: String = "1h",
        @Query("QUANTITIES") quantities: String = "1,2,4,9" // RA/Dec, Alt/Az, Mag, phase angle
    ): Response<String>

    companion object {
        const val BASE_URL = "https://ssd-api.jpl.nasa.gov"
    }
}
