package com.example.gamedock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.account.SteamAccount
import com.example.gamedock.data.repository.AccountsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddSteamAccountUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddSteamAccountViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSteamAccountUiState())
    val uiState: StateFlow<AddSteamAccountUiState> = _uiState.asStateFlow()

    fun saveAccount(steamLoginSecure: String, sessionId: String, cookies: Map<String, String> = emptyMap()) {
        // Persist a Steam account using captured cookies; errors surface in UI state.
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val account = SteamAccount(
                    id = extractSteamId(steamLoginSecure),
                    steamLoginSecure = steamLoginSecure,
                    sessionid = sessionId,
                    cookies = if (cookies.isNotEmpty()) cookies else mapOf(
                        "steamLoginSecure" to steamLoginSecure,
                        "sessionid" to sessionId
                    ),
                    nickname = "Steam User"
                )
                accountsRepository.saveSteamAccount(account)
            }.onSuccess {
                _uiState.value = AddSteamAccountUiState(isSaved = true)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Failed to save, please try again."
                    )
                }
            }
        }
    }

    fun resetSavedFlag() {
        // Allow UI to navigate again after save.
        _uiState.update { it.copy(isSaved = false) }
    }

    private fun extractSteamId(steamLoginSecure: String): String {
        // Derive steamId from cookie value; fallback prevents crashes if format changes.
        return steamLoginSecure
            .split("%7C%7C", "||")
            .firstOrNull()
            ?: "Unknown"
    }
}
