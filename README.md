# GameDock

GameDock is a small Jetpack Compose demo that surfaces free game giveaways, price comparisons, bundles, and a lightweight watchlist. The goal is to keep the architecture lean so a three‑person squad can own feature slices end-to-end without drowning in ceremony.

---

## Program Structure

```
app/
└─ src/main/java/com/example/gamedock/
   ├─ ui/                         # All Compose surfaces + lightweight VMs
   │   ├─ MainActivity.kt         # Single-activity entry point
   │   ├─ GameDockApp.kt          # Scaffold + NavHost
   │   ├─ NavGraph.kt / Screen.kt / BottomNavBar.kt
   │   ├─ HomeScreen.kt
   │   ├─ FreebiesScreen.kt       # Loads freebies via DealsRepository
   │   ├─ CompareScreen.kt        # Price query + results
   │   ├─ WatchlistScreen.kt      # Placeholder in-memory state
   │   ├─ BundlesScreen.kt        # Placeholder bundles state
   │   ├─ SettingsScreen.kt       # Notification toggle
   │   ├─ UiDefaults.kt           # Shared Dimens + Strings
   │   └─ components/             # GameCard, PriceCard, SectionHeader
   │
   ├─ data/
   │   ├─ model/                  # Game, Offer, BundleInfo, WatchItem, etc.
   │   ├─ repository/
   │   │   ├─ DealsRepository.kt
   │   │   ├─ FakeDealsRepository.kt
+  │   │   └─ DealsRepositoryImpl.kt (placeholder for real data stack)
   │   ├─ remote/                 # DealsApi + DTO shells for Retrofit
   │   ├─ local/                  # Room placeholders (entities + DAO)
   │   └─ util/                   # Currency/Date helpers
   │
   ├─ di/
   │   ├─ RepositoryModule.kt     # Manual providers until Hilt lands
   │   ├─ NetworkModule.kt
   │   └─ DatabaseModule.kt
   │
   └─ workers/
       ├─ PriceSyncWorker.kt
       └─ AlertWorker.kt
```

---

## Getting Started

1. **Prerequisites**
   - Android Studio Giraffe+ with Compose tooling
   - JDK 11+

2. **Build**
   ```bash
   ./gradlew assembleDebug
   ```
   > If Gradle cannot write to `~/.gradle`, set `GRADLE_USER_HOME` to a writable folder first.

3. **Run**
   - Deploy from Android Studio to an emulator or device
   - The bottom navigation exposes Home, Freebies, Compare, Watchlist, Bundles, Settings

---

## Roadmap

| Area            | Next Steps                                                                 |
|-----------------|-----------------------------------------------------------------------------|
| Data layer      | Replace `FakeDealsRepository` with Retrofit + Room in `DealsRepositoryImpl` |
| Watchlist       | Persist entries locally, integrate alerts via `PriceSyncWorker`            |
| Bundles/Compare | Flesh out actual bundle feeds and price-history data                       |
| DI              | Swap manual modules for Hilt once the codebase grows                       |

---

## Contributing

1. Fork / checkout a branch
2. Keep changes scoped per feature slice (`ui/`, `data/`, etc.)
3. Run `./gradlew lintDebug testDebug`
4. Open a PR summarizing the slice and impacted files

Happy hacking! 🎮
