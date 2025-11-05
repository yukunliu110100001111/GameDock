package com.example.gamedock.data.mapper

import com.example.gamedock.data.remote.dto.FreebieDto
import com.example.gamedock.data.remote.dto.OfferDto
import com.example.gamedock.domain.model.Game
import com.example.gamedock.domain.model.Offer

/**
 * Mapping extension functions from network DTOs into domain models.
 */
fun FreebieDto.toDomain(): Game = Game(
    id = id,
    title = title,
    store = store,
    imageUrl = imageUrl,
    endTimeMillis = endTimeMillis,
    isFree = true
)

fun OfferDto.toDomain(): Offer = Offer(
    id = id,
    gameTitle = gameTitle,
    store = store,
    currentPrice = currentPrice,
    lowestPrice = lowestPrice,
    currencyCode = currencyCode,
    url = url
)
