package com.example.gamedock.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.data.repository.SettingsRepository
import com.example.gamedock.data.repository.WatchlistRepository
import com.example.gamedock.notifications.PriceDropNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class PriceCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val watchlistRepository: WatchlistRepository,
    private val dealsRepository: DealsRepository,
    private val settingsRepository: SettingsRepository,
    private val notifier: PriceDropNotifier
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        // Respect global notification toggle
        val notificationsEnabled = settingsRepository.notificationsEnabled.first()
        if (!notificationsEnabled) return Result.success()
        notifier.createNotificationChannel()

        // 获取 Watchlist 的所有项
        val items = watchlistRepository.watchlistFlow().first()

        items.forEach { item ->
            if (!item.notificationsEnabled) return@forEach
            try {
                val offers = dealsRepository.comparePrices(item.title)

                val filteredOffers =
                    if (item.preferredStores.isEmpty()) offers
                    else offers.filter { it.store in item.preferredStores }

                if (filteredOffers.isEmpty()) return@forEach


                val selected = filteredOffers.minByOrNull { it.currentPrice } ?: return@forEach

                val newPrice = selected.currentPrice
                val oldPrice = item.lastKnownPrice

                if (newPrice == 0.0 && oldPrice != 0.0) {
                    notifier.notifyFreeNow(
                        title = item.title,
                        url = selected.url,
                        imageUrl = selected.imageUrl
                    )
                    watchlistRepository.updateLastKnownPrice(item.gameId, newPrice)
                    return@forEach
                }

                // normal promotions
                if (newPrice < oldPrice) {
                    notifier.notifyPriceDrop(
                        title = item.title,
                        old = oldPrice,
                        new = newPrice,
                        url = selected.url,
                        imageUrl = selected.imageUrl
                    )
                    watchlistRepository.updateLastKnownPrice(item.gameId, newPrice)
                    return@forEach
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Result.success()
    }
}
