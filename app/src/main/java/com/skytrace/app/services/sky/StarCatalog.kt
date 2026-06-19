package com.skytrace.app.services.sky

import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType

/**
 * Built-in bright star catalog (Yale Bright Star Catalog subset).
 * Contains the brightest stars visible to naked eye for sky map.
 * Full catalog data can be synced from remote source.
 */
object StarCatalog {

    data class StarEntry(
        val name: String,
        val bayer: String?,
        val constellation: String,
        val ra: Double, // hours
        val dec: Double, // degrees
        val magnitude: Double
    )

    /** Top ~50 brightest stars for immediate display */
    val brightStars: List<StarEntry> = listOf(
        StarEntry("Sirius", "α CMa", "Canis Major", 6.752, -16.716, -1.46),
        StarEntry("Canopus", "α Car", "Carina", 6.399, -52.696, -0.74),
        StarEntry("Arcturus", "α Boo", "Boötes", 14.261, 19.182, -0.05),
        StarEntry("Vega", "α Lyr", "Lyra", 18.616, 38.784, 0.03),
        StarEntry("Capella", "α Aur", "Auriga", 5.278, 45.998, 0.08),
        StarEntry("Rigel", "β Ori", "Orion", 5.242, -8.202, 0.13),
        StarEntry("Procyon", "α CMi", "Canis Minor", 7.655, 5.225, 0.34),
        StarEntry("Betelgeuse", "α Ori", "Orion", 5.919, 7.407, 0.42),
        StarEntry("Achernar", "α Eri", "Eridanus", 1.629, -57.237, 0.46),
        StarEntry("Hadar", "β Cen", "Centaurus", 14.064, -60.373, 0.61),
        StarEntry("Altair", "α Aql", "Aquila", 19.846, 8.868, 0.77),
        StarEntry("Acrux", "α Cru", "Crux", 12.443, -63.100, 0.77),
        StarEntry("Aldebaran", "α Tau", "Taurus", 4.599, 16.509, 0.85),
        StarEntry("Antares", "α Sco", "Scorpius", 16.490, -26.432, 0.96),
        StarEntry("Spica", "α Vir", "Virgo", 13.420, -11.161, 0.97),
        StarEntry("Pollux", "β Gem", "Gemini", 7.755, 28.026, 1.14),
        StarEntry("Fomalhaut", "α PsA", "Piscis Austrinus", 22.961, -29.622, 1.16),
        StarEntry("Deneb", "α Cyg", "Cygnus", 20.690, 45.280, 1.25),
        StarEntry("Mimosa", "β Cru", "Crux", 12.795, -59.689, 1.25),
        StarEntry("Regulus", "α Leo", "Leo", 10.140, 11.967, 1.35),
        StarEntry("Castor", "α Gem", "Gemini", 7.577, 31.888, 1.58),
        StarEntry("Bellatrix", "γ Ori", "Orion", 5.419, 6.350, 1.64),
        StarEntry("Elnath", "β Tau", "Taurus", 5.438, 28.608, 1.65),
        StarEntry("Alnilam", "ε Ori", "Orion", 5.603, -1.202, 1.69),
        StarEntry("Polaris", "α UMi", "Ursa Minor", 2.530, 89.264, 1.98),
        StarEntry("Dubhe", "α UMa", "Ursa Major", 11.062, 61.751, 1.79),
        StarEntry("Merak", "β UMa", "Ursa Major", 11.031, 56.383, 2.37),
        StarEntry("Alkaid", "η UMa", "Ursa Major", 13.792, 49.314, 1.86),
        StarEntry("Mizar", "ζ UMa", "Ursa Major", 13.399, 54.926, 2.27),
        StarEntry("Albireo", "β Cyg", "Cygnus", 19.512, 27.960, 3.08),
        StarEntry("Denebola", "β Leo", "Leo", 11.818, 14.572, 2.14),
        StarEntry("Alpheratz", "α And", "Andromeda", 0.140, 29.091, 2.06),
        StarEntry("Mirach", "β And", "Andromeda", 1.163, 35.621, 2.05),
        StarEntry("Almach", "γ And", "Andromeda", 2.065, 42.330, 2.17),
        StarEntry("Schedar", "α Cas", "Cassiopeia", 0.675, 56.537, 2.23),
        StarEntry("Caph", "β Cas", "Cassiopeia", 0.153, 59.150, 2.27),
        StarEntry("Algenib", "γ Peg", "Pegasus", 0.220, 15.184, 2.84),
        StarEntry("Markab", "α Peg", "Pegasus", 23.079, 15.205, 2.49),
        StarEntry("Scheat", "β Peg", "Pegasus", 23.063, 28.083, 2.42),
        StarEntry("Diphda", "β Cet", "Cetus", 0.727, -17.987, 2.02),
        StarEntry("Rasalhague", "α Oph", "Ophiuchus", 17.582, 12.560, 2.08),
        StarEntry("Sabik", "η Oph", "Ophiuchus", 17.173, -15.725, 2.43),
        StarEntry("Nunki", "σ Sgr", "Sagittarius", 18.921, -26.297, 2.05),
        StarEntry("Kaus Australis", "ε Sgr", "Sagittarius", 18.403, -34.385, 1.85),
        StarEntry("Shaula", "λ Sco", "Scorpius", 17.560, -37.104, 1.63),
        StarEntry("Eltanin", "γ Dra", "Draco", 17.943, 51.489, 2.23),
    )

    fun toCelestialObjects(): List<CelestialObject> = brightStars.map { star ->
        CelestialObject(
            id = "star_${star.name.lowercase().replace(" ", "_")}",
            name = star.name,
            catalogId = star.bayer,
            type = ObjectType.STAR,
            rightAscension = star.ra,
            declination = star.dec,
            magnitude = star.magnitude,
            constellation = star.constellation
        )
    }
}
