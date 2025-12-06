package com.example.gamedock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.repository.AccountsRepository
import com.example.gamedock.data.repository.EpicAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val epicAuthRepository: EpicAuthRepository
) : ViewModel() {

    data class HomeUiState(
        val accounts: List<PlatformAccount> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState
    private var hasLoadedOnce = false

    fun loadAllAccounts(force: Boolean = false) {
        // Load Steam/Epic accounts from storage, optionally forcing a refresh.
        viewModelScope.launch {
            if (!force && hasLoadedOnce && _uiState.value.accounts.isNotEmpty()) return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                accountsRepository.loadAllAccounts()
            }.onSuccess {
                _uiState.value = HomeUiState(accounts = it, isLoading = false, errorMessage = null)
                hasLoadedOnce = true
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to load accounts"
                )
            }
        }
    }

    fun deleteAccount(account: PlatformAccount) {
        // Delete the given account and refresh the list.
        viewModelScope.launch {
            when (account.platform) {
                PlatformType.Steam -> accountsRepository.deleteSteamAccount(account.id)
                PlatformType.Epic -> accountsRepository.deleteEpicAccount(account.id)
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val accounts = accountsRepository.loadAllAccounts()
            _uiState.value = _uiState.value.copy(accounts = accounts, isLoading = false)
        }
    }

    suspend fun refreshEpicAccount(account: EpicAccount): Boolean {
        // Refresh Epic tokens for a single account and update UI state.
        val tokens = epicAuthRepository.refreshTokens(account.refreshToken) ?: return false
        accountsRepository.saveEpicAccount(
            account.copy(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                nickname = tokens.displayName ?: account.nickname
            )
        )
        withContext(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(accounts = accountsRepository.loadAllAccounts())
        }
        return true
    }
}
