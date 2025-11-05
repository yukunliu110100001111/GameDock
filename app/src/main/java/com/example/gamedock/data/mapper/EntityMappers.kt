package com.example.gamedock.data.mapper

import com.example.gamedock.data.local.entity.GameEntity
import com.example.gamedock.data.local.entity.OfferEntity
import com.example.gamedock.data.local.entity.WatchItemEntity
import com.example.gamedock.domain.model.Game
import com.example.gamedock.domain.model.Offer
import com.example.gamedock.domain.model.WatchItem

/**
 * Mapping helpers between persistence layer and domain.
 */
fun GameEntity.toDomain(): Game = Game(
    id = id,
    title = title,
    store = store,
    imageUrl = imageUrl
)

fun OfferEntity.toDomain(): Offer = Offer(
    id = id,
    gameTitle = gameTitle,
    store = store,
    currentPrice = currentPrice,
    lowestPrice = lowestPrice
)

fun WatchItemEntity.toDomain(game: Game): WatchItem = WatchItem(
    id = id,
    game = game,
    targetPrice = targetPrice,
    notificationsEnabled = notificationsEnabled
)
