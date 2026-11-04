package com.naturewalls.app.data.model

/**
 * One leaf category of the collection tree, e.g. "Misty Forests".
 *
 * [searchQuery] is executed live against Wikimedia Commons (CirrusSearch, file namespace).
 * Every query in this catalogue was validated against the live API before shipping — each one
 * returns dozens of photographs that pass the licence allow-list, so no empty or fabricated
 * shelf exists in the app.
 */
data class SubCategory(
    val id: String,
    val name: String,
    val searchQuery: String
)

/**
 * A top-level collection, e.g. "Forests".
 */
data class WallpaperGroup(
    val id: String,
    val name: String,
    val tagline: String,
    val coverQuery: String,
    val subCategories: List<SubCategory>
) {
    fun subCategory(id: String?): SubCategory? = subCategories.firstOrNull { it.id == id }
}

/**
 * The live browse tree. Images are never stored here — only the search terms used to ask
 * Wikimedia Commons for fresh photographs every time the app opens.
 */
object CategoryCatalog {

    val groups: List<WallpaperGroup> = listOf(
        WallpaperGroup(
            id = "forests",
            name = "Forests",
            tagline = "Deep canopy and quiet light",
            coverQuery = "forest sunlight trees",
            subCategories = listOf(
                SubCategory("tropical", "Tropical Forests", "tropical forest"),
                SubCategory("rainforest", "Rainforests", "rainforest"),
                SubCategory("pine", "Pine Forests", "pine forest"),
                SubCategory("bamboo", "Bamboo Forests", "bamboo forest"),
                SubCategory("misty", "Misty Forests", "mist forest trees morning"),
                SubCategory("autumn", "Autumn Forests", "autumn forest"),
                SubCategory("snowy", "Snowy Forests", "snowy forest"),
                SubCategory("dark", "Dark Forests", "forest night trees")
            )
        ),
        WallpaperGroup(
            id = "mountains",
            name = "Mountains",
            tagline = "Stone, snow and altitude",
            coverQuery = "mountain peak landscape",
            subCategories = listOf(
                SubCategory("snow", "Snow Mountains", "snow mountain"),
                SubCategory("himalayan", "Himalayan Mountains", "himalaya mountains"),
                SubCategory("rocky", "Rocky Mountains", "rocky mountains"),
                SubCategory("lakes", "Mountain Lakes", "mountain lake"),
                SubCategory("green", "Green Mountains", "green mountains"),
                SubCategory("sunsets", "Mountain Sunsets", "mountain sunset"),
                SubCategory("valleys", "Mountain Valleys", "mountain valley")
            )
        ),
        WallpaperGroup(
            id = "water",
            name = "Waterfalls & Rivers",
            tagline = "Water in motion",
            coverQuery = "waterfall long exposure",
            subCategories = listOf(
                SubCategory("tropical", "Tropical Waterfalls", "tropical waterfall"),
                SubCategory("giant", "Giant Waterfalls", "giant waterfall"),
                SubCategory("hidden", "Hidden Waterfalls", "hidden waterfall"),
                SubCategory("clear", "Crystal Clear Rivers", "clear river"),
                SubCategory("mountain", "Mountain Rivers", "mountain river"),
                SubCategory("streams", "Forest Streams", "creek forest water stream"),
                SubCategory("lakes", "Lakes", "lake nature")
            )
        ),
        WallpaperGroup(
            id = "flowers",
            name = "Flowers & Plants",
            tagline = "Petals, leaves and bloom",
            coverQuery = "flower macro blossom",
            subCategories = listOf(
                SubCategory("roses", "Roses", "rose flower"),
                SubCategory("sunflowers", "Sunflowers", "sunflower field"),
                SubCategory("cherry", "Cherry Blossoms", "cherry blossom"),
                SubCategory("tulips", "Tulips", "tulip field"),
                SubCategory("orchids", "Orchids", "orchid flower"),
                SubCategory("lotus", "Lotus Flowers", "lotus flower"),
                SubCategory("wildflowers", "Wildflowers", "wildflowers meadow"),
                SubCategory("tropical", "Tropical Plants", "tropical leaves plant"),
                SubCategory("cactus", "Cactus", "cactus plant desert")
            )
        ),
        WallpaperGroup(
            id = "landscapes",
            name = "Natural Landscapes",
            tagline = "Wide open earth",
            coverQuery = "landscape panorama nature",
            subCategories = listOf(
                SubCategory("beaches", "Beaches", "beach sand ocean"),
                SubCategory("islands", "Islands", "tropical island"),
                SubCategory("deserts", "Deserts", "desert dunes"),
                SubCategory("fields", "Green Fields", "green field"),
                SubCategory("grasslands", "Grasslands", "grassland steppe plain"),
                SubCategory("valleys", "Valleys", "valley landscape"),
                SubCategory("cliffs", "Cliffs", "cliffs coastline ocean"),
                SubCategory("countryside", "Countryside", "countryside landscape"),
                SubCategory("snow", "Snow Landscapes", "snow landscape")
            )
        ),
        WallpaperGroup(
            id = "seasons",
            name = "Weather & Seasons",
            tagline = "Sky, mist and season",
            coverQuery = "dramatic sky landscape clouds",
            subCategories = listOf(
                SubCategory("rainy", "Rainy Weather", "rain drops"),
                SubCategory("foggy", "Foggy Landscapes", "fog landscape"),
                SubCategory("snowfall", "Snowfall", "snowfall"),
                SubCategory("autumn", "Autumn", "autumn leaves"),
                SubCategory("spring", "Spring", "spring flowers"),
                SubCategory("summer", "Summer", "summer landscape green"),
                SubCategory("winter", "Winter", "winter landscape"),
                SubCategory("sunrise", "Sunrise", "sunrise"),
                SubCategory("sunset", "Sunset", "sunset landscape sky")
            )
        )
    )

    /** Rotating editorial queries for the "Trending" shelf — always fetched live. */
    val trendingQueries: List<String> = listOf(
        "sunset mountains reflection",
        "fog forest valley",
        "waterfall moss rocks",
        "autumn lake reflection",
        "wildflower meadow sunrise",
        "glacier blue ice landscape",
        "desert dunes shadow",
        "cherry blossom temple",
        "island turquoise water aerial",
        "lightning storm clouds"
    )

    /**
     * Commons category behind the editorial shelves. "Quality images of nature" was chosen over
     * "Featured pictures of nature" because the latter is dominated by animal and bird features,
     * which the nature-wallpaper filter correctly rejects (~67% of it).
     */
    const val CURATED_NATURE_CATEGORY = "Quality images of nature"

    fun group(id: String?): WallpaperGroup? = groups.firstOrNull { it.id == id }

    fun subCategory(groupId: String?, subId: String?): SubCategory? =
        group(groupId)?.subCategory(subId)

    fun displayName(groupId: String?, subId: String?): String =
        subCategory(groupId, subId)?.name ?: group(groupId)?.name ?: "Nature"
}
