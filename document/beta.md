# GameDock – Beta Summary

## What’s Done
- Core features live: freebie aggregation, cross-store price compare, bundle search, watchlist with price alerts, and account login with in-app WebView claiming.
- Live data sources: Epic official free-games API + GamerPower aggregator; IsThereAnyDeal (ITAD) price and bundle APIs.
- End-to-end Hilt injection (`GameDockApplication` + `NetworkModule` / `RepositoryModule`) with Retrofit/OkHttp hitting external sources.
- Background alerts: WorkManager `PriceCheckWorker` polls the watchlist every 6 hours and pushes notifications.

## Delivery Details
- **Freebies & claiming**: `DealsRepositoryImpl` merges `EpicStoreAdapter` + `GamerPowerStoreAdapter`; `FreebiesScreen` splits active/upcoming freebies, and on claim picks an account by platform then opens `CustomClaimActivity` (extends `AccountWebViewActivity`, auto-injects Authorization or Steam cookies).
- **Price compare**: `ItadAdapter` calls the ITAD API and derives region from system locale; `CompareViewModel` adds debounced search, ascending/descending sort, `BestOfferSummary`, and one-tap add-to-watchlist.
- **Bundle search**: `BundlesViewModel` reuses the ITAD bundles endpoint, supports input search, price sorting, remaining-time badges, and card deep links to store pages.
- **Accounts**: Steam login `SteamLoginActivity` captures full cookies, AES-encrypted in `SteamAccountStore`; Epic login `EpicLoginActivity` + `EpicAuthRepositoryImpl` exchange/refresh tokens stored in `EpicAccountStore`; `HomeScreen` lists/deletes accounts and can jump to profile pages.
- **Watchlist & alerts**: `WatchlistRepositoryImpl` persists via SharedPreferences (overwrite/delete/update price); `WatchlistScreen` shows and deletes; `CompareViewModel.addToWatchlist` adds from results; `MainActivity.schedulePriceCheckWorker` registers periodic work, and `PriceCheckWorker` triggers `PriceDropNotifier` for drop/free notifications.
- **UI & navigation**: Single-activity Compose + bottom nav; `NavGraph` covers Home/Freebies/Compare/Watchlist/Bundles/Settings and account-add routes; components like `PriceCard` and `WatchlistCard` include skeletons, badges, and store jumps.

## Gaps / Risks
- Price history/charts not implemented; bundles only searchable with no default feed.
- Room/DAO are placeholders; watchlist and accounts rely on SharedPreferences with no offline cache or migration plan.
- Background jobs `PriceSyncWorker` / `AlertWorker` remain TODO; `SettingsScreen` toggle is not persisted and not tied to notification permission.
- Limited error/empty handling (network failures, API throttling); ITAD API key is hardcoded client-side.
- Claim flow picks the first account per platform; no account picker or unified multi-account UI.

## Verification
- No local build/tests run yet (CLI has not executed `./gradlew assembleDebug`); on-device verification is needed for live data, login, and notification chain.

## Next Priorities
1) Add price history and charts to complete Compare/Detail; provide a default bundles feed for browsing without search.  
2) Implement Room + DataStore (watchlist/settings), offline cache, and migration; add network/battery constraints to background work.  
3) Improve account selection and error UX (multi-account chooser, claim retry); externalize the ITAD key/config for swapping.  
4) Optimize UI for aesthetics, performance, and accessibility.
