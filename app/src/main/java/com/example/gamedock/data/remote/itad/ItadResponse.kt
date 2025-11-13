package com.example.gamedock.data.remote.itad

/**
 * Response DTO for the ITAD Search API.
 * The API returns a list; this represents a single item in that list.
 */
data class ItadSearchItem(
    val id: String,
    val slug: String,
    val title: String,
    val type: String,
    val mature: Boolean,
    val assets: ItadAssets
)

/**
 * Image assets for a game referenced by an ItadSearchItem.
 */
data class ItadAssets(
    val banner145: String?,
    val banner300: String?,
    val banner400: String?,
    val banner600: String?,
    val boxart: String?
)