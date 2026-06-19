package com.skytrace.app.services.sky

/**
 * Keplerian orbital elements for solar system planets.
 * Values are for J2000.0 epoch with rates per Julian century.
 * Source: NASA JPL planetary elements table (simplified).
 */
data class PlanetElements(
    val name: String,
    val l0: Double, // mean longitude at epoch (deg)
    val l1: Double, // rate (deg/century)
    val a0: Double, // semi-major axis (AU)
    val a1: Double, // rate (AU/century)
    val e0: Double, // eccentricity
    val e1: Double, // rate
    val i0: Double, // inclination (deg)
    val i1: Double, // rate
    val omega0: Double, // longitude of ascending node (deg)
    val omega1: Double, // rate
    val pi0: Double, // longitude of perihelion (deg)
    val pi1: Double, // rate
    val magnitude: Double = 0.0 // typical visual magnitude
) {
    companion object {
        val MERCURY = PlanetElements(
            name = "Mercury",
            l0 = 252.25032, l1 = 149472.67411,
            a0 = 0.38710, a1 = 0.0,
            e0 = 0.20563, e1 = 0.000002527,
            i0 = 7.005, i1 = -0.00059,
            omega0 = 48.331, omega1 = -0.12534,
            pi0 = 77.456, pi1 = 0.16047,
            magnitude = -0.4
        )

        val VENUS = PlanetElements(
            name = "Venus",
            l0 = 181.97973, l1 = 58517.81539,
            a0 = 0.72333, a1 = 0.0,
            e0 = 0.00677, e1 = -0.000004938,
            i0 = 3.395, i1 = -0.00078,
            omega0 = 76.680, omega1 = -0.27769,
            pi0 = 131.564, pi1 = 0.00480,
            magnitude = -4.4
        )

        val EARTH = PlanetElements(
            name = "Earth",
            l0 = 100.46646, l1 = 35999.37245,
            a0 = 1.00000, a1 = 0.0,
            e0 = 0.01671, e1 = -0.00004204,
            i0 = 0.000, i1 = -0.01294,
            omega0 = 174.873, omega1 = -0.24120,
            pi0 = 102.937, pi1 = 0.32327
        )

        val MARS = PlanetElements(
            name = "Mars",
            l0 = 355.45332, l1 = 19140.30268,
            a0 = 1.52368, a1 = 0.0,
            e0 = 0.09340, e1 = 0.000009048,
            i0 = 1.850, i1 = -0.00681,
            omega0 = 49.559, omega1 = -0.29257,
            pi0 = 336.060, pi1 = 0.44441,
            magnitude = -2.0
        )

        val JUPITER = PlanetElements(
            name = "Jupiter",
            l0 = 34.40438, l1 = 3034.74613,
            a0 = 5.20260, a1 = 0.0,
            e0 = 0.04849, e1 = 0.000016335,
            i0 = 1.303, i1 = -0.00195,
            omega0 = 100.464, omega1 = 0.17641,
            pi0 = 14.331, pi1 = 0.21548,
            magnitude = -2.7
        )

        val SATURN = PlanetElements(
            name = "Saturn",
            l0 = 49.94432, l1 = 1222.49362,
            a0 = 9.55491, a1 = 0.0,
            e0 = 0.05551, e1 = -0.000034664,
            i0 = 2.489, i1 = 0.00251,
            omega0 = 113.666, omega1 = -0.25068,
            pi0 = 93.057, pi1 = 0.54196,
            magnitude = 0.5
        )

        val URANUS = PlanetElements(
            name = "Uranus",
            l0 = 313.23218, l1 = 428.48202,
            a0 = 19.21845, a1 = 0.0,
            e0 = 0.04630, e1 = -0.000027293,
            i0 = 0.773, i1 = -0.00163,
            omega0 = 74.006, omega1 = 0.05087,
            pi0 = 173.005, pi1 = 0.09266,
            magnitude = 5.7
        )

        val NEPTUNE = PlanetElements(
            name = "Neptune",
            l0 = 304.88003, l1 = 218.45642,
            a0 = 30.11039, a1 = 0.0,
            e0 = 0.00899, e1 = 0.000006245,
            i0 = 1.770, i1 = 0.00030,
            omega0 = 131.784, omega1 = -0.00610,
            pi0 = 48.124, pi1 = 0.02916,
            magnitude = 7.8
        )

        val ALL_PLANETS = listOf(MERCURY, VENUS, MARS, JUPITER, SATURN, URANUS, NEPTUNE)
    }
}
