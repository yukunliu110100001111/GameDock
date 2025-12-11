package com.example.gamedock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.model.BundleDeal
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.ui.components.PriceCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.gamedock.R
import com.example.gamedock.data.watchlist.WatchlistEntity
import com.example.gamedock.data.watchlist.WatchlistRepository
import com.example.gamedock.data.util.CurrencyUtils
import com.example.gamedock.data.remote.itad.ItadSearchItem
import com.google.gson.Gson

// Keep Dimens reference here; replace with concrete values (e.g. 16.dp) if Dimens is absent
// import com.example.gamedock.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    queryFromRoute: String,
    viewModel: CompareViewModel = hiltViewModel()
) {
    // Entry of price comparison: observe state and delegate UI rendering.
    val uiState by viewModel.uiState.collectAsState()
    val gsonLocal = remember { Gson() }

    var savedQuery by rememberSaveable { mutableStateOf("") }
    var savedSelectedJson by rememberSaveable { mutableStateOf<String?>(null) }

    // Seed initial query when coming from a route with pre-filled text.
    LaunchedEffect(queryFromRoute) {
        if (queryFromRoute.isNotBlank()) {
            viewModel.setInitialQuery(queryFromRoute)
        }
    }

    // Mirror ViewModel state into local saveable state for better navigation restore.
    LaunchedEffect(uiState.query) { savedQuery = uiState.query }
    LaunchedEffect(uiState.selectedGame) {
        savedSelectedJson = uiState.selectedGame?.let { gsonLocal.toJson(it) }
    }

    // Try to restore selection/query when coming back to the screen.
    LaunchedEffect(Unit) {
        if (uiState.query.isBlank() && savedQuery.isNotBlank()) {
            viewModel.onSearchQueryChange(savedQuery)
        }
        if (uiState.selectedGame == null && !savedSelectedJson.isNullOrBlank()) {
            runCatching {
                gsonLocal.fromJson(savedSelectedJson, ItadSearchItem::class.java)
            }.getOrNull()?.let { restored ->
                viewModel.onSelectGame(restored)
            }
        }
    }

    CompareScreenContent(
        uiState = uiState,
        savedQuery = savedQuery,
        onQueryChange = { newQuery ->
            savedQuery = newQuery
            viewModel.onSearchQueryChange(newQuery)
        },
        onSearchNow = { viewModel.searchNow() },
        onToggleSort = { viewModel.toggleSort() },
        onSelectGame = { game -> viewModel.onSelectGame(game) },
        onToggleWatchlist = { offer -> viewModel.toggleWatchlist(offer) }
    )
}

@Composable
private fun CompareScreenContent(
    uiState: CompareUiState,
    savedQuery: String,
    onQueryChange: (String) -> Unit,
    onSearchNow: () -> Unit,
    onToggleSort: () -> Unit,
    onSelectGame: (ItadSearchItem) -> Unit,
    onToggleWatchlist: (Offer) -> Unit
) {
    val errorMessage = uiState.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Search text field ---
        OutlinedTextField(
            value = savedQuery,
            onValueChange = onQueryChange,
            label = { Text("Search game...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchNow() }),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Header row: title + actions ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val countText = if (!uiState.isLoading && uiState.results.isNotEmpty())
                " (" + uiState.results.size + ")" else ""
            Text(
                text = "Price Comparison$countText",
                style = MaterialTheme.typography.headlineSmall,
            )
            Row {
                IconButton(onClick = onSearchNow) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onToggleSort) {
                    Icon(Icons.Filled.SwapVert, contentDescription = "Toggle sort")
                }
            }
        }

        val selectedGame = uiState.selectedGame

        // --- Mode 1: no game selected yet ---
        if (selectedGame == null) {
            when {
                // 1) Searching games: show full-screen loading
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // 2) We have search results: let user pick a game
                uiState.searchResults.isNotEmpty() -> {
                    Text(
                        text = "Select a game to compare:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.searchResults) { game ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectGame(game) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        game.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = game.slug,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 3) No search results yet: distinguish "empty query" vs "no matches"
                else -> {
                    when {
                        // 3a) User typed something, search finished, no error -> no matches
                        uiState.query.isNotBlank() &&
                                !uiState.isLoading &&
                                errorMessage == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(id = R.drawable.no_results_found),
                                        contentDescription = "No games",
                                        modifier = Modifier.size(220.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No games found for \"${uiState.query}\".",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 3b) Initial state or cleared query -> generic hint
                        else -> {
                            Text(
                                text = "Type to search and choose a game to compare prices.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Do not render bundles/offers until a game is selected
            return
        }

        // --- Mode 2: game selected, show comparison details ---
        BestOfferSummary(uiState.results)

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Error message (e.g., network failure)
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Selected game but no offers/bundles and no error -> "no offers" placeholder
        if (!uiState.isLoading &&
            uiState.results.isEmpty() &&
            uiState.bundles.isEmpty() &&
            uiState.query.isNotBlank() &&
            errorMessage == null
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.no_results_found),
                        contentDescription = "No offers",
                        modifier = Modifier.size(220.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No offers found for \"${uiState.query}\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }

        // --- Main list: skeleton / bundles / offers ---
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Skeleton cards while offers are loading
            if (uiState.isLoading) {
                items(4) {
                    SkeletonPriceCard()
                }
            }

            // Bundles section
            if (uiState.bundles.isNotEmpty()) {
                item {
                    Text(
                        text = "Bundles (${uiState.bundles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(uiState.bundles) { bundle ->
                    BundleCard(bundle)
                }
            }

            // Offers list
            itemsIndexed(uiState.results) { _, offer ->
                val isBest = uiState.resultsMinPrice() == offer.currentPrice
                Box(Modifier.fillMaxWidth()) {
                    PriceCard(
                        offer = offer,
                        isWatchlisted = uiState.watchlistedIds.contains(offer.id),
                        onToggleWatchlist = onToggleWatchlist
                    )
                    if (isBest) {
                        BestBadge(Modifier.align(Alignment.TopStart))
                    }
                }
            }
        }
    }
}

@Composable
private fun BestOfferSummary(results: List<Offer>) {
    // Highlight the cheapest offer and potential savings.
    if (results.isEmpty()) return
    val min = results.minByOrNull { it.currentPrice } ?: return
    val max = results.maxByOrNull { it.currentPrice } ?: min
    val diff = max.currentPrice - min.currentPrice
    val diffPercent = if (max.currentPrice > 0) (diff / max.currentPrice * 100) else 0.0
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Best: ${min.store} ${CurrencyUtils.format(min.currentPrice, min.currencyCode)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (results.size > 1 && diff > 0) {
                Text(
                    text = "Vs Highest: save ${CurrencyUtils.format(diff, min.currencyCode)} (~${"%.0f".format(diffPercent)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BestBadge(modifier: Modifier = Modifier) {
    // Small badge shown on the best-priced card.
    Surface(
        modifier = modifier
            .padding(start = 6.dp, top = 6.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Text(
            text = "BEST",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SkeletonPriceCard() {
    // Simple card used while offers are loading.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(100.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {}
}

@Composable
private fun BundleCard(bundle: BundleDeal) {
    // Card showing bundle store, expiry, price, and included games.
    val uriHandler = LocalUriHandler.current
    val expiry = parseExpiry(bundle.expiry)
    val remaining = remainingLabel(expiry)
    val urgent = remaining?.contains("h") == true
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = bundle.link.isNotBlank()) {
                if (bundle.link.isNotBlank()) uriHandler.openUri(bundle.link)
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
        tonalElevation = if (urgent) 2.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreChip(store = bundle.store)
                if (remaining != null) {
                    StatusBadge(
                        text = remaining + if (urgent) " left" else "",
                        color = if (urgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(bundle.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = CurrencyUtils.format(bundle.price, bundle.currency),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (bundle.games.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Includes: " + bundle.games.take(3).joinToString { it.title } +
                            if (bundle.games.size > 3) " +" + (bundle.games.size - 3) else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Open bundle",
                style = MaterialTheme.typography.labelMedium,
                color = if (bundle.link.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(if (bundle.link.isNotBlank()) 1f else 0.5f)
            )
        }
    }
}

@Composable
private fun StoreChip(store: String) {
    // Compact chip to display the store name.
    val bg = storeColor(store)
    Surface(
        color = bg.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp
    ) {
        Text(
            text = store,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = bg
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    // Badge used for time-remaining or status labels.
    Surface(
        color = color.copy(alpha = 0.25f),
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color.darken(0.25f)
        )
    }
}

@Composable
private fun storeColor(store: String): Color = when {
    // Map common stores to distinguishable colors.
    store.contains("Steam", true) -> Color(0xFF1B6FBC)
    store.contains("Epic", true) -> Color(0xFF9146FF)
    store.contains("GOG", true) -> Color(0xFF673AB7)
    store.contains("Humble", true) -> Color(0xFFE9642E)
    store.contains("Ubisoft", true) -> Color(0xFF2E82D8)
    else -> MaterialTheme.colorScheme.primary
}

private fun Color.darken(factor: Float): Color {
    // Darken RGB channels slightly to improve contrast.
    val r = (red * (1 - factor)).coerceIn(0f, 1f)
    val g = (green * (1 - factor)).coerceIn(0f, 1f)
    val b = (blue * (1 - factor)).coerceIn(0f, 1f)
    return Color(r, g, b, alpha)
}

private fun parseExpiry(raw: String?): Long? =
    // Parse ISO-8601 timestamp to epoch millis; null if parsing fails.
    runCatching { java.time.Instant.parse(raw).toEpochMilli() }.getOrNull()

private fun remainingLabel(expiryMillis: Long?): String? {
    // Convert expiry millis into a short label like "3d", "5h" or "30m".
    expiryMillis ?: return null
    val diff = expiryMillis - System.currentTimeMillis()
    if (diff <= 0) return null
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days >= 1 -> "${days}d"
        hours >= 1 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun CompareUiState.resultsMinPrice(): Double? =
    results.minByOrNull { it.currentPrice }?.currentPrice

data class CompareUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Offer> = emptyList(),
    val bundles: List<BundleDeal> = emptyList(),
    val watchlistedIds: Set<String> = emptySet(),
    val searchResults: List<ItadSearchItem> = emptyList(),
    val selectedGame: ItadSearchItem? = null,
    val errorMessage: String? = null,
    val sortAscending: Boolean = true
)

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val repository: DealsRepository,
    private val watchlistRepository: WatchlistRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Backing state for the screen; exposed as StateFlow for Compose.
    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private var searchJob: Job? = null

    init {
        // Restore saved state and keep watchlist membership in sync.
        restoreState()
        viewModelScope.launch {
            watchlistRepository.watchlistFlow().collect { list ->
                _uiState.value = _uiState.value.copy(
                    watchlistedIds = list.map { it.gameId }.toSet()
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        // Called on each query change; update state and debounce search.
        _uiState.value = _uiState.value.copy(query = query)
        savedStateHandle["compare_query"] = query
        scheduleSearchResults(query)
    }

    fun searchNow() {
        // Immediate search triggered by keyboard or refresh icon.
        val selected = _uiState.value.selectedGame
        if (selected != null) {
            fetchOffersFor(selected)
        } else {
            scheduleSearchResults(_uiState.value.query, immediate = true)
        }
    }

    fun toggleSort() {
        // Flip sort direction and re-order current offers.
        val newAsc = !_uiState.value.sortAscending
        _uiState.value = _uiState.value.copy(
            sortAscending = newAsc,
            results = sortOffers(_uiState.value.results, newAsc)
        )
    }

    private fun sortOffers(list: List<Offer>, ascending: Boolean): List<Offer> =
        if (ascending) list.sortedBy { it.currentPrice } else list.sortedByDescending { it.currentPrice }

    private fun scheduleSearchResults(query: String, immediate: Boolean = false) {
        // Debounce ITAD search to avoid spamming the API while typing.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (!immediate) delay(300)
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    selectedGame = null,
                    results = emptyList(),
                    bundles = emptyList(),
                    isLoading = false,
                    errorMessage = null
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            loadSearchResults(query)
        }
    }

    private suspend fun loadSearchResults(query: String) {
        // Call repository search and update search results or error.
        runCatching { repository.searchGames(query) }
            .onSuccess { games ->
                _uiState.value = _uiState.value.copy(
                    searchResults = games,
                    selectedGame = null,
                    isLoading = false,
                    errorMessage = null
                )
            }
            .onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    selectedGame = null,
                    isLoading = false,
                    errorMessage = throwable.message ?: "Unable to search games"
                )
            }
    }

    fun toggleWatchlist(offer: Offer) {
        // Add or remove an offer from the watchlist.
        viewModelScope.launch {
            val exists = _uiState.value.watchlistedIds.contains(offer.id)
            if (exists) {
                watchlistRepository.remove(offer.id)
            } else {
                val entry = WatchlistEntity(
                    gameId = offer.id,
                    title = offer.gameTitle,
                    imageUrl = offer.imageUrl,
                    url = offer.url,
                    lastKnownPrice = offer.currentPrice,
                    currency = offer.currencyCode,
                    preferredStores = listOf(offer.store)
                )
                watchlistRepository.addOrUpdate(entry)
            }
        }
    }

    fun onSelectGame(game: ItadSearchItem) {
        // User picked a game; reset lists and load new offers/bundles.
        _uiState.value = _uiState.value.copy(
            selectedGame = game,
            isLoading = true,
            errorMessage = null,
            results = emptyList(),
            bundles = emptyList()
        )
        savedStateHandle["compare_selected"] = gson.toJson(game)
        fetchOffersFor(game)
    }

    private fun fetchOffersFor(game: ItadSearchItem) {
        // Fetch offers and bundles for a specific game.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val offers = repository.comparePricesById(game)
                val bundles = repository.searchBundles(game.title)
                offers to bundles
            }.onSuccess { (offers, bundles) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = sortOffers(offers, _uiState.value.sortAscending),
                    bundles = bundles
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = emptyList(),
                    bundles = emptyList(),
                    errorMessage = throwable.message ?: "Unable to compare prices"
                )
            }
        }
    }

    fun setInitialQuery(q: String) {
        // Used when arriving with a pre-filled query from navigation arguments.
        if (q.isNotBlank() && _uiState.value.query != q) {
            _uiState.value = _uiState.value.copy(query = q)
            savedStateHandle["compare_query"] = q
            searchNow()
        }
    }

    private fun restoreState() {
        // Restore query/selection from SavedStateHandle after recreation.
        val savedQuery: String? = savedStateHandle["compare_query"]
        val savedGameJson: String? = savedStateHandle["compare_selected"]

        if (!savedQuery.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(query = savedQuery)
        }

        val savedGame = runCatching {
            savedGameJson?.let { gson.fromJson(it, ItadSearchItem::class.java) }
        }.getOrNull()

        if (savedGame != null) {
            _uiState.value = _uiState.value.copy(selectedGame = savedGame, isLoading = true)
            fetchOffersFor(savedGame)
        } else if (!savedQuery.isNullOrBlank()) {
            scheduleSearchResults(savedQuery, immediate = true)
        }
    }
}
