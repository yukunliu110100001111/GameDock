# GameDock — Game Deals & Freebies Aggregator

## App Category

Entertainment / Shopping / Utility

## About The App (Brief Introduction)

GameDock is an Android app that consolidates PC game information from multiple stores (e.g., Steam, Epic Games Store, GOG, EA App/Origin, Ubisoft Connect, Humble) into one place. The final goals are:
  - Tracking free game giveaways across stores and letting users claim them via safe deep links or webviews.
  - Comparing current prices for the same game across multiple stores, showing regional currency and historical lows where available.
  - Maintaining a watchlist with price-drop notifications.
  - Maintaining account logins to multiple stores and enabling users to genuinely add games to their libraries.

Stretch goals include multi-store account sign-in, library import, bundle/charity pack integration (e.g., Humble/Fanatical-style bundles), and eventually purchase and activation flows via official store hand-off (no automated checkout in v1; respecting store policies).

## Related Apps / Services (Positioning)

| Name            | App Details                                                                                                                     | Source Code Link                 |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------|--------------------------------|
| GamePP          | GamePP is an all-in-one PC game utility platform that includes a basic free-game claiming feature, but it isn’t very convenient. | None                           |
| XiaoHeihe       | XiaoHeihe is a game news mobile app that provides a daily sale calendar and some news. It also can purchase games, but it can't activate games automatically. | None                           |
| SteamDB         | SteamDB is a free-to-use website that provides a comprehensive database of Steam games. It contains comprehensive game data, but only data. | [LINK](https://steamdb.info)   |
| Steam Mobile App | A mobile app that provides a comprehensive list of PC games, including free-to-play games, and allows users to claim free-to-play games. | None                           |
| Epic Mobile App | Epic mobile app is a game store app that provides a comprehensive list of Epic Games Store games. It also provides a way to purchase games. It enables users to claim free games on its platform. | None                           |

## Main New Added Features And Enhancements

Although there are many game-related apps and websites, most of them are either too narrowly focused or too cluttered, introducing many unnecessary media features. While similar functions exist in scattered forms, PC-based tools are often inconvenient to use. Currently, there is no mobile app dedicated specifically to helping users save money when buying games.

Our app fills this gap — by leveraging the convenience of smartphones, it ensures that users can instantly receive notifications about discounts or giveaways for the games they care about.

| Ser | Existing Features             | Proposed Improvements / New Features                                                                 | Remarks        |
|-----|------------------------------|----------------------------------------------------------------------------------------------------|----------------|
| 1   | Basic giveaway list from single source | **Giveaways Feed** — Unified list of active/expiring free games across multiple stores; each item includes countdown timer and deep link to claim | **Improvement** |
| 2   | Individual store price listings | **Game Price Compare** — Cross-store price comparison with regional currency selector and “historical lowest” tag | **New Feature** |
| 3   | Occasional bundle announcements | **Bundles View** — Display charity or discount bundles with clear source labeling (Humble, Fanatical, etc.) | **New Feature** |
| 4   | Manual account linking only    | **Activate Accounts** — Allow login to multiple stores and enable users to genuinely add games to their libraries | **New Feature** |
| 5   | Simple favorites list          | **Watchlist & Alerts** — Add games to watchlist; background worker checks for price drops or freebies and sends push notifications | **New Feature** |
| 6   | Basic search                  | **Search & Details** — Title search; details screen shows stores, prices, regions, and metadata      | **Improvement** |
| 7   | Static data view              | **Offline Cache** — Store last-known prices and giveaways for offline viewing                         | **New Feature** |

## Data & APIs

- Official store APIs and public deal aggregators that permit client usage.
- Maintain a source registry (name, URL, API terms, rate limits, region coverage).
- Implement a StoreAdapter interface per source; normalize to internal models (Game, Offer, PricePoint, Bundle, Freebie).
- Cache normalized entities locally; attribute each field to its source; show source badges in the UI.

## Architecture Overview

- Presentation: Jetpack Compose screens (Home, Freebies, Compare, Game Detail, Bundles, Watchlist, Settings).
- Domain: Use-cases (GetGiveaways, GetPriceCompare, ManageWatchlist…).
- Data: Repositories (OffersRepo, GamesRepo) backed by Room and RemoteDataSources via Retrofit.
- Background: WorkManager for periodic sync + notifications.

## Development Timeline (Weeks Align To Course Milestones)

| Week           | Work To Be Completed                                                                 | Comments                     |
|----------------|-------------------------------------------------------------------------------------|------------------------------|
| **Week 2**     | Finalize **data model** and **StoreAdapter** abstraction; prepare mock data and low-fi wireframes | Establish core entities and API schema |
| **Week 4**     | Implement **Retrofit + Room** integration; build Home + Freebies list; add deep-links and a basic sync worker | First functional prototype ready |
| **Week 7 (Alpha)** | Complete **Game Search + Compare view**; integrate watchlist CRUD + notifications; stabilize MVP and usability pass | Record short Alpha demo video |
| **Week 9 (Final)** | Add **bundles view + price chart**; optimize performance and caching; finalize documentation + presentation video | Submission-ready Play build   |