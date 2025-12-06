package com.example.gamedock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.gamedock.data.model.platformType
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.data.model.startDateMillis
import com.example.gamedock.data.model.endDateMillis
import com.example.gamedock.data.model.remainingText
import com.example.gamedock.ui.components.SectionHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamedock.R
import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.repository.AccountsRepository
import com.example.gamedock.ui.home.AccountWebViewActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject


@Composable
fun FreebiesScreen(
    viewModel: FreebiesViewModel = hiltViewModel()
) {
    // Freebies list surface: shows active/upcoming giveaways and handles claim flows.

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var accountPicker by remember { mutableStateOf<AccountPickerData?>(null) }

    // Listen for claim events
    LaunchedEffect(Unit) {
        viewModel.claimEvent.collect { event ->
            when (event) {

                is ClaimUiEvent.NoAccount -> {
                    snackbarHostState.showSnackbar("Please log in ${event.platform}")

                }

                is ClaimUiEvent.OpenWebView -> {
                    val intent = Intent(context, CustomClaimActivity::class.java)
                    intent.putExtra("account_id", event.accountId)
                    intent.putExtra("target_url", event.url)
                    intent.putExtra("platform", event.platform.name)
                    context.startActivity(intent)
                }

                is ClaimUiEvent.ExternalBrowser -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    context.startActivity(intent)
                }

                is ClaimUiEvent.SelectAccount -> {
                    accountPicker = AccountPickerData(
                        platform = event.platform,
                        accounts = event.accounts,
                        url = event.url
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Retry"
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.refresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎁 ${Strings.freebiesTitle}",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh freebies")
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Dimens.screenPadding)
            ) {
                item { SectionHeader("🟢 Free now!!") }

                items(uiState.active) { freebie ->
                    FreebieCard(
                        freebie = freebie,
                        onClick = { viewModel.onClaimClicked(freebie) }
                    )
                }

                item { SectionHeader("⏳ Upcoming freebies") }

                items(uiState.upcoming) { freebie ->
                    FreebieCard(
                        freebie = freebie,
                        onClick = { viewModel.onClaimClicked(freebie) }
                    )
                }
            }


            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (!uiState.isLoading && uiState.active.isEmpty() && uiState.errorMessage == null) {
            Text(
                text = "No freebies found. Pull to refresh to try again.",
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
            TextButton(onClick = viewModel::refresh) {
                Text("Refresh")
            }
        }
    }

    accountPicker?.let { data ->
        AccountSelectDialog(
            data = data,
            onDismiss = { accountPicker = null },
            onSelect = { accountId ->
                viewModel.onAccountChosen(accountId, data.platform, data.url)
                accountPicker = null
            }
        )
    }
}

@Composable
fun FreebieCard(
    freebie: Freebie,
    onClick: () -> Unit = {}
) {
    // Card displaying a single freebie with store info and claim action.
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { /* default card tap is disabled; actions handled via buttons */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            // Left cover image (will show automatically if imageUrl is provided later)
            if (freebie.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(freebie.imageUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = freebie.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = freebie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Store name
                Text(
                    text = freebie.store,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = freebie.remainingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Spacer(Modifier.weight(1f))

                    //viewModel -> claim
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Claim")
                    }
                }
            }
        }
    }
}


data class FreebiesUiState(
    val isLoading: Boolean = false,
    val active: List<Freebie> = emptyList(),
    val upcoming: List<Freebie> = emptyList(),
    val errorMessage: String? = null
)


@HiltViewModel
class FreebiesViewModel @Inject constructor(
    private val repository: DealsRepository,
    private val accountsRepository: AccountsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreebiesUiState(isLoading = true))
    val uiState: StateFlow<FreebiesUiState> = _uiState.asStateFlow()

    private val _claimEvent = MutableSharedFlow<ClaimUiEvent>()
    val claimEvent = _claimEvent.asSharedFlow()

    init {
        loadCached()
        refresh()
    }

    fun onClaimClicked(freebie: Freebie) {
        // Determine how to claim based on platform and available accounts.
        viewModelScope.launch {

            val platform = freebie.platformType()

            if (platform == null) {
                freebie.claimUrl?.let {
                    _claimEvent.emit(ClaimUiEvent.ExternalBrowser(it))
                }
                return@launch
            }

            val accounts = accountsRepository
                .loadAllAccounts()
                .filter { it.platform == platform }

            if (accounts.isEmpty()) {
                _claimEvent.emit(ClaimUiEvent.NoAccount(platform))
                return@launch
            }

            if (accounts.size > 1) {
                _claimEvent.emit(
                    ClaimUiEvent.SelectAccount(
                        platform = platform,
                        accounts = accounts,
                        url = freebie.claimUrl ?: return@launch
                    )
                )
                return@launch
            }

            val account = accounts.first()

            _claimEvent.emit(
                ClaimUiEvent.OpenWebView(
                    accountId = account.id,
                    url = freebie.claimUrl ?: return@launch,
                    platform = platform
                )
            )
        }
    }

    fun onAccountChosen(accountId: String, platform: PlatformType, url: String) {
        // User picked an account from the dialog; proceed to WebView claim.
        viewModelScope.launch {
            _claimEvent.emit(
                ClaimUiEvent.OpenWebView(
                    accountId = accountId,
                    url = url,
                    platform = platform
                )
            )
        }
    }

    private fun loadCached() {
        // Seed UI with locally cached freebies to avoid blank screen on launch.
        val cached = repository.getCachedFreebies()
        if (cached.isEmpty()) return
        val (active, upcoming) = partitionFreebies(cached)
        _uiState.value = FreebiesUiState(
            active = active,
            upcoming = upcoming,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun partitionFreebies(freebies: List<Freebie>): Pair<List<Freebie>, List<Freebie>> {
        // Split freebies into active vs upcoming using start/end windows.
        val now = System.currentTimeMillis()
        val active = freebies.filter { freebie ->
            val start = freebie.startDateMillis ?: 0L
            val end = freebie.endDateMillis ?: Long.MAX_VALUE
            now in start..end
        }
        val upcoming = freebies.filter { freebie ->
            val start = freebie.startDateMillis ?: Long.MAX_VALUE
            now < start
        }
        return active to upcoming
    }

    fun refresh() {
        // Force refresh from network and update UI buckets.
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { repository.getFreebies() }
                .onSuccess { freebies ->
                    val (active, upcoming) = partitionFreebies(freebies)

                    _uiState.value = FreebiesUiState(
                        active = active,
                        upcoming = upcoming
                    )
                }

                .onFailure { throwable ->
                    _uiState.value = FreebiesUiState(
                        errorMessage = throwable.message ?: "Unable to load freebies"
                    )
                }
        }
    }
}

private data class AccountPickerData(
    val platform: PlatformType,
    val accounts: List<PlatformAccount>,
    val url: String
)

@Composable
private fun AccountSelectDialog(
    data: AccountPickerData,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    // Modal dialog prompting user to pick an account for claim flow.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose ${data.platform.name} account") },
        // Account selection dialog title
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.accounts.forEach { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(account.id) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = account.avatar,
                            contentDescription = null,
                            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                            error = painterResource(id = R.drawable.ic_launcher_foreground),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(account.nickname.ifBlank { account.id })
                            Text(
                                text = "${account.platform} · ID: ${account.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

class CustomClaimActivity : AccountWebViewActivity() {
    override fun resolveConfig(): WebViewConfig? {
        // Build WebView config using extras passed from FreebiesScreen.
        val accountId = intent.getStringExtra("account_id") ?: return null
        val url = intent.getStringExtra("target_url") ?: return null
        val platformName = intent.getStringExtra("platform") ?: return null
        val platform = PlatformType.valueOf(platformName)

        return WebViewConfig(
            platform = platform,
            accountId = accountId,
            targetUrl = url
        )
    }
}
