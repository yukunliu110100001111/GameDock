package com.example.gamedock.data.remote.dto

/**
 * DTO mirroring the payload returned by the remote freebies endpoint.
 */
data class FreebieDto(
    val id: String,
    val title: String,
    val store: String,
    val imageUrl: String,
    val endTimeMillis: Long?
)
