package com.skytrace.app.services.sky

import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType

/**
 * NGC catalog - built-in subset of the brightest/most-observed NGC objects.
 * Full catalog (7840 objects) is synced from remote on first launch.
 * This embedded set covers objects commonly targeted by amateur astronomers.
 */
object NgcCatalog {

    data class NgcEntry(
        val number: Int,
        val name: String?,
        val type: String,
        val constellation: String,
        val ra: Double,
        val dec: Double,
        val magnitude: Double,
        val sizeArcmin: Double? = null
    )

    val objects: List<NgcEntry> = listOf(
        // Showpiece galaxies
        NgcEntry(224, "Andromeda Galaxy", "Spiral Galaxy", "Andromeda", 0.712, 41.269, 3.4, 178.0), // M31
        NgcEntry(598, "Triangulum Galaxy", "Spiral Galaxy", "Triangulum", 1.564, 30.660, 5.7, 73.0), // M33
        NgcEntry(4594, "Sombrero Galaxy", "Spiral Galaxy", "Virgo", 12.667, -11.623, 8.0, 9.0), // M104
        NgcEntry(5194, "Whirlpool Galaxy", "Spiral Galaxy", "Canes Venatici", 13.498, 47.195, 8.4, 11.0), // M51
        NgcEntry(4258, null, "Spiral Galaxy", "Canes Venatici", 12.317, 47.300, 8.4, 19.0), // M106
        NgcEntry(253, "Sculptor Galaxy", "Spiral Galaxy", "Sculptor", 0.793, -25.288, 7.2, 27.0),
        NgcEntry(891, "Silver Sliver Galaxy", "Spiral Galaxy", "Andromeda", 2.376, 42.349, 9.9, 14.0),
        NgcEntry(2403, null, "Spiral Galaxy", "Camelopardalis", 7.615, 65.603, 8.5, 22.0),
        NgcEntry(4565, "Needle Galaxy", "Spiral Galaxy", "Coma Berenices", 12.604, 25.988, 9.6, 16.0),
        NgcEntry(4631, "Whale Galaxy", "Spiral Galaxy", "Canes Venatici", 12.703, 32.541, 9.2, 15.0),
        NgcEntry(7331, null, "Spiral Galaxy", "Pegasus", 22.618, 34.416, 9.5, 11.0),
        NgcEntry(1300, null, "Barred Spiral Galaxy", "Eridanus", 3.330, -19.411, 10.4, 6.0),
        NgcEntry(2841, null, "Spiral Galaxy", "Ursa Major", 9.368, 50.976, 9.2, 8.0),
        NgcEntry(3115, "Spindle Galaxy", "Lenticular Galaxy", "Sextans", 10.087, -7.719, 8.9, 8.0),
        NgcEntry(3184, null, "Spiral Galaxy", "Ursa Major", 10.303, 41.424, 9.8, 7.0),
        NgcEntry(3521, null, "Spiral Galaxy", "Leo", 11.096, -0.036, 8.9, 11.0),
        NgcEntry(3628, null, "Spiral Galaxy", "Leo", 11.338, 13.587, 9.5, 15.0),
        NgcEntry(4244, "Silver Needle Galaxy", "Spiral Galaxy", "Canes Venatici", 12.293, 37.807, 10.2, 16.0),
        NgcEntry(4449, null, "Irregular Galaxy", "Canes Venatici", 12.472, 44.094, 9.6, 6.0),
        NgcEntry(4656, "Hockey Stick Galaxy", "Irregular Galaxy", "Canes Venatici", 12.729, 32.169, 10.5, 15.0),
        NgcEntry(5907, "Splinter Galaxy", "Spiral Galaxy", "Draco", 15.265, 56.329, 10.3, 13.0),
        NgcEntry(6946, "Fireworks Galaxy", "Spiral Galaxy", "Cepheus", 20.578, 60.154, 8.8, 11.0),
        // Bright nebulae
        NgcEntry(7000, "North America Nebula", "Emission Nebula", "Cygnus", 20.988, 44.333, 4.0, 120.0),
        NgcEntry(7293, "Helix Nebula", "Planetary Nebula", "Aquarius", 22.494, -20.837, 7.6, 13.0),
        NgcEntry(6543, "Cat's Eye Nebula", "Planetary Nebula", "Draco", 17.977, 66.633, 8.1, 0.3),
        NgcEntry(6960, "Western Veil Nebula", "Supernova Remnant", "Cygnus", 20.756, 30.711, 7.0, 70.0),
        NgcEntry(6992, "Eastern Veil Nebula", "Supernova Remnant", "Cygnus", 20.935, 31.717, 7.0, 60.0),
        NgcEntry(2237, "Rosette Nebula", "Emission Nebula", "Monoceros", 6.536, 4.950, 9.0, 80.0),
        NgcEntry(2024, "Flame Nebula", "Emission Nebula", "Orion", 5.685, -1.850, 10.0, 30.0),
        NgcEntry(2392, "Eskimo Nebula", "Planetary Nebula", "Gemini", 7.487, 20.912, 9.2, 0.7),
        NgcEntry(3242, "Ghost of Jupiter", "Planetary Nebula", "Hydra", 10.413, -18.638, 7.8, 0.3),
        NgcEntry(6826, "Blinking Planetary", "Planetary Nebula", "Cygnus", 19.746, 50.526, 8.8, 0.4),
        NgcEntry(7027, null, "Planetary Nebula", "Cygnus", 21.119, 42.233, 8.5, 0.3),
        NgcEntry(7662, "Blue Snowball", "Planetary Nebula", "Andromeda", 23.434, 42.533, 8.3, 0.5),
        NgcEntry(1499, "California Nebula", "Emission Nebula", "Perseus", 4.051, 36.633, 5.0, 145.0),
        NgcEntry(2264, "Cone Nebula", "Emission Nebula", "Monoceros", 6.680, 9.883, 3.9, 20.0),
        NgcEntry(6888, "Crescent Nebula", "Emission Nebula", "Cygnus", 20.202, 38.350, 7.4, 18.0),
        NgcEntry(7635, "Bubble Nebula", "Emission Nebula", "Cassiopeia", 23.339, 61.200, 10.0, 15.0),
        // Bright clusters
        NgcEntry(869, "Double Cluster h", "Open Cluster", "Perseus", 2.328, 57.133, 4.3, 30.0),
        NgcEntry(884, "Double Cluster χ", "Open Cluster", "Perseus", 2.373, 57.150, 4.4, 30.0),
        NgcEntry(457, "Owl Cluster", "Open Cluster", "Cassiopeia", 1.328, 58.283, 6.4, 13.0),
        NgcEntry(663, null, "Open Cluster", "Cassiopeia", 1.773, 61.233, 7.1, 16.0),
        NgcEntry(752, null, "Open Cluster", "Andromeda", 1.960, 37.683, 5.7, 50.0),
        NgcEntry(1502, null, "Open Cluster", "Camelopardalis", 4.131, 62.333, 5.7, 8.0),
        NgcEntry(2169, "37 Cluster", "Open Cluster", "Orion", 6.142, 13.967, 5.9, 7.0),
        NgcEntry(6231, null, "Open Cluster", "Scorpius", 16.901, -41.817, 2.6, 15.0),
        NgcEntry(6530, null, "Open Cluster", "Sagittarius", 18.074, -24.350, 4.6, 14.0),
        NgcEntry(6819, null, "Open Cluster", "Cygnus", 19.684, 40.183, 7.3, 5.0),
        NgcEntry(7789, "Caroline's Rose", "Open Cluster", "Cassiopeia", 23.960, 56.717, 6.7, 16.0),
        // Globular clusters
        NgcEntry(104, "47 Tucanae", "Globular Cluster", "Tucana", 0.401, -72.081, 4.1, 31.0),
        NgcEntry(5139, "Omega Centauri", "Globular Cluster", "Centaurus", 13.447, -47.479, 3.7, 36.0),
        NgcEntry(6397, null, "Globular Cluster", "Ara", 17.676, -53.674, 5.7, 32.0),
        NgcEntry(362, null, "Globular Cluster", "Tucana", 1.054, -70.849, 6.4, 14.0),
        NgcEntry(1851, null, "Globular Cluster", "Columba", 5.236, -40.047, 7.1, 11.0),
        NgcEntry(2808, null, "Globular Cluster", "Carina", 9.199, -64.864, 6.2, 14.0),
        NgcEntry(5904, null, "Globular Cluster", "Serpens", 15.309, 2.083, 5.6, 23.0), // M5
        NgcEntry(6093, null, "Globular Cluster", "Scorpius", 16.283, -22.975, 7.3, 10.0), // M80
        NgcEntry(6121, null, "Globular Cluster", "Scorpius", 16.393, -26.526, 5.6, 36.0), // M4
        NgcEntry(6218, null, "Globular Cluster", "Ophiuchus", 16.788, -1.950, 6.7, 16.0), // M12
        NgcEntry(6254, null, "Globular Cluster", "Ophiuchus", 16.952, -4.100, 6.6, 20.0), // M10
        NgcEntry(6341, null, "Globular Cluster", "Hercules", 17.285, 43.137, 6.4, 14.0), // M92
        NgcEntry(6656, null, "Globular Cluster", "Sagittarius", 18.607, -23.905, 5.1, 32.0), // M22
        NgcEntry(6752, null, "Globular Cluster", "Pavo", 19.181, -59.985, 5.4, 29.0),
        NgcEntry(6838, null, "Globular Cluster", "Sagitta", 19.896, 18.783, 8.2, 7.0), // M71
        NgcEntry(7078, null, "Globular Cluster", "Pegasus", 21.500, 12.167, 6.2, 18.0), // M15
        NgcEntry(7099, null, "Globular Cluster", "Capricornus", 21.673, -23.180, 7.2, 12.0), // M30
        // Additional popular targets
        NgcEntry(281, "Pacman Nebula", "Emission Nebula", "Cassiopeia", 0.877, 56.617, 7.4, 35.0),
        NgcEntry(1333, null, "Reflection Nebula", "Perseus", 3.489, 31.350, 5.6, 6.0),
        NgcEntry(1535, "Cleopatra's Eye", "Planetary Nebula", "Eridanus", 4.236, -12.744, 9.6, 0.3),
        NgcEntry(1977, "Running Man Nebula", "Reflection Nebula", "Orion", 5.588, -4.833, 7.0, 20.0),
        NgcEntry(2359, "Thor's Helmet", "Emission Nebula", "Canis Major", 7.312, -13.200, 11.5, 8.0),
        NgcEntry(2440, null, "Planetary Nebula", "Puppis", 7.698, -18.211, 9.4, 0.2),
        NgcEntry(2683, null, "Spiral Galaxy", "Lynx", 8.880, 33.422, 9.7, 9.0),
        NgcEntry(2903, null, "Spiral Galaxy", "Leo", 9.540, 21.501, 8.9, 13.0),
        NgcEntry(3132, "Eight-Burst Nebula", "Planetary Nebula", "Vela", 10.119, -40.437, 9.2, 0.8),
        NgcEntry(3372, "Carina Nebula", "Emission Nebula", "Carina", 10.752, -59.867, 1.0, 120.0),
        NgcEntry(4038, "Antennae Galaxies", "Interacting Galaxies", "Corvus", 12.032, -18.867, 10.3, 5.0),
        NgcEntry(4755, "Jewel Box Cluster", "Open Cluster", "Crux", 12.894, -60.333, 4.2, 10.0),
        NgcEntry(5128, "Centaurus A", "Elliptical Galaxy", "Centaurus", 13.424, -43.018, 6.8, 26.0),
        NgcEntry(6302, "Bug Nebula", "Planetary Nebula", "Scorpius", 17.232, -37.104, 7.1, 0.8),
        NgcEntry(6334, "Cat's Paw Nebula", "Emission Nebula", "Scorpius", 17.344, -35.783, 5.5, 40.0),
        NgcEntry(6357, "Lobster Nebula", "Emission Nebula", "Scorpius", 17.414, -34.350, 5.5, 40.0),
        NgcEntry(6514, "Trifid Nebula", "Emission Nebula", "Sagittarius", 18.037, -23.033, 6.3, 28.0), // M20
        NgcEntry(6523, "Lagoon Nebula", "Emission Nebula", "Sagittarius", 18.063, -24.383, 6.0, 90.0), // M8
        NgcEntry(6611, "Eagle Nebula", "Emission Nebula", "Serpens", 18.314, -13.783, 6.0, 7.0), // M16
        NgcEntry(6720, "Ring Nebula", "Planetary Nebula", "Lyra", 18.893, 33.029, 8.8, 1.4), // M57
        NgcEntry(6853, "Dumbbell Nebula", "Planetary Nebula", "Vulpecula", 19.993, 22.717, 7.5, 8.0), // M27
        NgcEntry(7023, "Iris Nebula", "Reflection Nebula", "Cepheus", 21.017, 68.167, 6.8, 18.0),
        NgcEntry(7380, "Wizard Nebula", "Emission Nebula", "Cepheus", 22.785, 58.100, 7.2, 25.0),
        NgcEntry(7538, null, "Emission Nebula", "Cepheus", 23.220, 61.467, 6.0, 9.0),
    )

    fun toCelestialObjects(): List<CelestialObject> = objects.map { ngc ->
        CelestialObject(
            id = "ngc_${ngc.number}",
            name = ngc.name?.let { "NGC ${ngc.number} - $it" } ?: "NGC ${ngc.number}",
            catalogId = "NGC ${ngc.number}",
            type = ObjectType.NGC,
            rightAscension = ngc.ra,
            declination = ngc.dec,
            magnitude = ngc.magnitude,
            constellation = ngc.constellation,
            description = ngc.type
        )
    }
}
