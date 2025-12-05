# GameDock API v1 Documentation (Logical Contract)

This document describes the **logical API contract** used by the GameDock project.

- In the current course version, all of these endpoints can be implemented purely on the **Android client** (Repository + Retrofit + Room + WorkManager).
- In a future version, the same paths and payloads can be implemented as real REST APIs on a backend server **without changing the client significantly**.

---

## 0. Conventions

### 0.1 Basic Info

- **App name**: GameDock — Game Deals & Freebies Aggregator  
- **Main purpose**: Aggregate multi-store PC game prices and freebies; provide comparison, historical low tags, watchlist, and alerts.  
- **Target stores**: Steam, Epic Games Store, GOG, Humble, Fanatical, etc.  
- **Data format**: `application/json; charset=utf-8`  
- **Time format**: ISO-8601, e.g. `2025-10-19T10:30:00Z`  
- **Currency format**: ISO 4217, e.g. `"USD"`, `"CNY"`

### 0.2 Pagination

List endpoints may support pagination parameters:

Code (text):

    page: int      // page index starting from 1, default 1
    pageSize: int  // items per page, default 20, max recommended 50

Standard paged response example:

Code (json):

    {
      "items": [ /* list items */ ],
      "page": 1,
      "pageSize": 20,
      "total": 134
    }

---

## 1. Authentication

### 1.1 GameDock Account (optional)

If a dedicated GameDock account system is added later, it can use this HTTP header:

Code (http header):

    Authorization: Bearer <access_token>

The current course version may **omit** this entirely.

### 1.2 Store Account Linking (Steam / Epic / GOG, etc.)

Store account linking is currently done on the client via **official OAuth / web flows** and **deep link callbacks**.

- Logical endpoint: `GET /me/accounts`

Example response:

Code (json):

    [
      {
        "store": "steam",
        "displayName": "My Steam Account",
        "linkedAt": "2025-10-20T09:00:00Z"
      },
      {
        "store": "epic",
        "displayName": "My Epic Account",
        "linkedAt": "2025-10-21T12:30:00Z"
      }
    ]

> In the course version, this can be backed by local storage (Room / SharedPreferences) instead of a real HTTP endpoint.

---

## 2. Games (Basic Game Info)

### 2.1 Search Games

**Endpoint**: `GET /games/search`  
**Purpose**: Search games by keyword, return basic info (no prices yet).

**Query parameters:**

Code (text):

    q      (string, required)  // search keyword, e.g. "Cyberpunk 2077"
    region (string, optional)  // region, default "global"

**Example response:**

Code (json):

    {
      "items": [
        {
          "id": "cyberpunk-2077",
          "title": "Cyberpunk 2077",
          "coverUrl": "https://cdn.steam.com/cover/1091500.jpg",
          "stores": ["steam", "gog", "epic"],
          "tags": ["RPG", "Open World"],
          "releaseDate": "2020-12-10"
        }
      ],
      "page": 1,
      "pageSize": 20,
      "total": 1
    }

---

### 2.2 Get Game Detail

**Endpoint**: `GET /games/{gameId}`  
**Purpose**: Get a single game’s basic information (no price data).

**Path parameter:**

Code (text):

    gameId (string)  // unified game ID, e.g. "cyberpunk-2077"

**Example response:**

Code (json):

    {
      "id": "cyberpunk-2077",
      "title": "Cyberpunk 2077",
      "coverUrl": "https://cdn.steam.com/cover/1091500.jpg",
      "stores": ["steam", "gog", "epic"],
      "tags": ["RPG", "Open World"],
      "releaseDate": "2020-12-10",
      "shortDescription": "An open-world, action-adventure story set in Night City.",
      "developer": "CD PROJEKT RED",
      "publisher": "CD PROJEKT RED"
    }

---

## 3. Freebies (Free Games)

### 3.1 Get Freebies List

**Endpoint**: `GET /freebies`  
**Purpose**: Return current free games (limited-time or permanently free to claim) across supported stores.

**Query parameters:**

Code (text):

    region (string, optional)  // default "global"
    store  (string, optional)  // e.g. "steam", "epic"
    page, pageSize (int, optional)  // pagination parameters

**Example response:**

Code (json):

    {
      "items": [
        {
          "id": "epic-ghostrunner-free-2025-11",
          "gameId": "ghostrunner",
          "title": "Ghostrunner",
          "store": "epic",
          "storeName": "Epic Games Store",
          "isFree": true,
          "originalPrice": 29.99,
          "currency": "USD",
          "startsAt": "2025-10-30T15:00:00Z",
          "endsAt": "2025-11-06T23:59:59Z",
          "url": "https://store.epicgames.com/free-games",
          "thumbnail": "https://cdn.epicgames.com/ghostrunner.jpg",
          "tags": ["Action"]
        }
      ],
      "page": 1,
      "pageSize": 20,
      "total": 12
    }

> UI uses `endsAt` to show countdown timers. Offline mode can display the last cached list from Room.

---

## 4. Offers (Multi-store Price Comparison)

### 4.1 Get Price Offers

**Endpoint**: `GET /offers`  
**Purpose**: Return latest offers for a game from multiple stores, with **historical low** flags.

**Query parameters:**

Code (text):

    q      (string)  // optional, search keyword
    gameId (string)  // optional, unified game ID; at least one of q/gameId is required
    region (string, optional)  // default "global"
    stores (string, optional)  // comma-separated list, e.g. "steam,epic,gog"

**Example response:**

Code (json):

    [
      {
        "gameId": "cyberpunk-2077",
        "title": "Cyberpunk 2077",
        "store": "steam",
        "storeName": "Steam",
        "price": 29.99,
        "currency": "USD",
        "originalPrice": 59.99,
        "discountPercent": 50,
        "isHistoricalLow": true,
        "lastCheckedAt": "2025-10-19T10:12:00Z",
        "url": "https://store.steampowered.com/app/1091500",
        "region": "US"
      },
      {
        "gameId": "cyberpunk-2077",
        "title": "Cyberpunk 2077",
        "store": "gog",
        "storeName": "GOG",
        "price": 32.99,
        "currency": "USD",
        "originalPrice": 59.99,
        "discountPercent": 45,
        "isHistoricalLow": false,
        "lastCheckedAt": "2025-10-19T10:13:00Z",
        "url": "https://www.gog.com/en/game/cyberpunk_2077",
        "region": "US"
      }
    ]

> In the Compare screen, offers are typically sorted by `price` ascending, with `isHistoricalLow == true` highlighted.

---

## 5. Price History

### 5.1 Get Price History

**Endpoint**: `GET /price-history/{gameId}`  
**Purpose**: Provide time-series price data for 30/90-day charts.

**Path parameter:**

Code (text):

    gameId (string)  // unified game ID

**Query parameters:**

Code (text):

    store (string, optional)  // filter by store, e.g. "steam"
    days  (int, optional)     // time window, default 90

**Example response:**

Code (json):

    [
      {
        "store": "steam",
        "date": "2025-08-01",
        "price": 39.99,
        "currency": "USD",
        "isHistoricalLow": false
      },
      {
        "store": "steam",
        "date": "2025-09-01",
        "price": 29.99,
        "currency": "USD",
        "isHistoricalLow": false
      },
      {
        "store": "steam",
        "date": "2025-10-01",
        "price": 19.99,
        "currency": "USD",
        "isHistoricalLow": true
      }
    ]

> If no history is available, the endpoint returns an empty array `[]`; the UI can show a “No history data” placeholder.

---

## 6. Bundles (Charity and Discount Bundles)

### 6.1 Get Bundles

**Endpoint**: `GET /bundles`  
**Purpose**: List current bundles / charity packs from sources like Humble and Fanatical.

**Query parameters:**

Code (text):

    source     (string, optional)  // e.g. "humble", "fanatical"
    activeOnly (boolean, optional) // default true
    page, pageSize (int, optional) // pagination

**Example response:**

Code (json):

    {
      "items": [
        {
          "id": "humble-rpg-legends-2025-10",
          "name": "Humble RPG Legends Bundle",
          "source": "humble",
          "url": "https://www.humblebundle.com/games/rpg-legends",
          "thumbnail": "https://hb.img/rpg_legends.jpg",
          "minPrice": 12.0,
          "currency": "USD",
          "endsAt": "2025-10-31T23:59:59Z",
          "includedGames": [
            "Pathfinder: Kingmaker",
            "Pillars of Eternity",
            "Torment: Tides of Numenera"
          ]
        }
      ],
      "page": 1,
      "pageSize": 10,
      "total": 3
    }

---

## 7. Watchlist

> In the course version, the watchlist can be fully implemented with **local Room tables**.  
> These endpoints represent the logical contract; they map directly to local CRUD operations.

### 7.1 Create Watchlist Item

**Endpoint**: `POST /watchlist`

**Request body example:**

Code (json):

    {
      "gameId": "cyberpunk-2077",
      "title": "Cyberpunk 2077",
      "targetPrice": 20.0,
      "currency": "USD",
      "notifyOnFree": true,
      "notifyOnHistoricalLow": true
    }

**Response 201 example:**

Code (json):

    {
      "id": "watch-cyberpunk-2077",
      "createdAt": "2025-10-19T10:30:00Z"
    }

---

### 7.2 List Watchlist Items

**Endpoint**: `GET /watchlist`

**Example response:**

Code (json):

    [
      {
        "id": "watch-cyberpunk-2077",
        "gameId": "cyberpunk-2077",
        "title": "Cyberpunk 2077",
        "targetPrice": 20.0,
        "currency": "USD",
        "notifyOnFree": true,
        "notifyOnHistoricalLow": true,
        "createdAt": "2025-10-19T10:30:00Z"
      }
    ]

---

### 7.3 Update Watchlist Item (optional)

**Endpoint**: `PATCH /watchlist/{id}`

**Request body example:**

Code (json):

    {
      "targetPrice": 18.0,
      "notifyOnFree": false
    }

**Response 200 example:**

Code (json):

    {
      "id": "watch-cyberpunk-2077",
      "updatedAt": "2025-10-20T09:00:00Z"
    }

---

### 7.4 Delete Watchlist Item

**Endpoint**: `DELETE /watchlist/{id}`

- Success: `204 No Content`  
- Not found: `404 Not Found`

---

## 8. Notifications & Background Work

### 8.1 PriceCheckWorker

Implemented as a **WorkManager CoroutineWorker** (for example, every 6 hours).

**Responsibilities:**

Code (text):

    1) Read all WatchItem entries from local storage;
    2) For each gameId, call /offers and /freebies;
    3) Check conditions: targetPrice, notifyOnFree, notifyOnHistoricalLow;
    4) Send a local notification and mark the item as notified.

**Notification example (conceptual):**

Code (text):

    Title: "Cyberpunk 2077 is at its lowest price!"
    Body:  "Now $19.99 on Steam – historical low. Tap to view details."
    Click: deep-link into /detail/{gameId} (Game Detail screen).

---

## 9. Status & Health Check (optional)

### 9.1 Status Endpoint

**Endpoint**: `GET /status`  
**Purpose**: Optional health check endpoint (mainly for future backend).

**Example response:**

Code (json):

    {
      "status": "ok",
      "timestamp": "2025-10-19T11:00:00Z",
      "version": "1.0.0",
      "stores": {
        "steam": "ok",
        "epic": "ok",
        "gog": "degraded"
      }
    }

---

## 10. Error Handling

### 10.1 Standard Error Response

Code (json):

    {
      "error": {
        "code": "RATE_LIMIT",
        "message": "Too many requests. Please try again later.",
        "details": null
      }
    }

### 10.2 Common Error Codes

Code (text):

    NETWORK_ERROR  (503): network or upstream service unavailable
    RATE_LIMIT     (429): too many requests
    INVALID_REGION (400): invalid region parameter
    NOT_FOUND      (404): gameId or watchlist id not found
    INTERNAL_ERROR (500): unexpected server error

---

## 11. Data Model Summary

Code (text):

    Game:
      id, title, coverUrl, stores, tags, releaseDate

    Freebie:
      id, gameId, title, store, storeName, isFree,
      originalPrice, currency, startsAt, endsAt, url, thumbnail, tags

    Offer:
      gameId, title, store, storeName, price, currency,
      originalPrice, discountPercent, isHistoricalLow,
      lastCheckedAt, url, region

    PricePoint:
      store, date, price, currency, isHistoricalLow

    BundleDeal:
      id, name, source, url, thumbnail, minPrice, currency,
      endsAt, includedGames[]

    WatchItem:
      id, gameId, title, targetPrice, currency,
      notifyOnFree, notifyOnHistoricalLow, createdAt

---

## 12. Implementation Notes (Course Version)

In the current course version, all of these endpoints can be implemented **entirely on the Android client**:

Code (text):

    - DealsRepository uses Retrofit to call public store APIs or existing aggregators.
    - Room caches Freebie / Offer / PricePoint data to support offline browsing.
    - /watchlist maps directly to a local WatchItemEntity table.
    - PriceCheckWorker periodically reads the watchlist and calls the repository
      to trigger local notifications when conditions are met.

If a backend is added later, the **same REST endpoints** can be implemented on the server, and the client only needs to **swap the repository implementation** from local/mock to network-backed, without major changes to UI or view models.
