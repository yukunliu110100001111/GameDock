package com.example.gamedock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.gamedock.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    // Settings surface: handles notification toggle and Epic auto-refresh preference.
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = LocalUriHandler.current
    val notifier = remember(context) { com.example.gamedock.notifications.PriceDropNotifier(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                notifier.createNotificationChannel()
                viewModel.setNotificationsEnabled(true)
            } else {
                // keep switch off
                viewModel.setNotificationsEnabled(false)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = Dimens.cardSpacing)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Deal alerts")
                Text(
                    text = "Enable notifications for watchlist price drops.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = uiState.notificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notifier.createNotificationChannel()
                            viewModel.setNotificationsEnabled(true)
                        }
                    } else {
                        viewModel.setNotificationsEnabled(false)
                    }
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = Dimens.cardSpacing)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Auto-refresh Epic tokens")
                Text(
                    text = "Refresh Epic access tokens on app launch.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = uiState.epicAutoRefresh,
                onCheckedChange = { enabled -> viewModel.setEpicAutoRefresh(enabled) }
            )
        }

        Column(
            modifier = Modifier
                .padding(top = Dimens.cardSpacing)
                .fillMaxWidth()
        ) {
            Text(text = "About", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Source code: github.com/yukunliu110100001111/GameDock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { uriHandler.openUri("https://github.com/yukunliu110100001111/GameDock") }
            )
            Text(
                text = "Powered by ITAD (IsThereAnyDeal) and GamePower data.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val epicAutoRefresh: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observe stored preferences and expose them to UI.
        viewModelScope.launch {
            launch {
                settingsRepository.notificationsEnabled.collect { enabled ->
                    _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
                }
            }
            launch {
                settingsRepository.epicAutoRefreshEnabled.collect { enabled ->
                    _uiState.value = _uiState.value.copy(epicAutoRefresh = enabled)
                }
            }
        }
    }

    fun toggleNotifications() {
        // Flip global notifications preference.
        viewModelScope.launch {
            val next = !_uiState.value.notificationsEnabled
            settingsRepository.setNotificationsEnabled(next)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        // Explicitly persist notification preference (used after permission flow).
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setEpicAutoRefresh(enabled: Boolean) {
        // Persist Epic token auto-refresh preference.
        viewModelScope.launch {
            settingsRepository.setEpicAutoRefresh(enabled)
        }
    }
}
