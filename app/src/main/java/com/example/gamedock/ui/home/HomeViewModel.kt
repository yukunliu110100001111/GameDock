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

    private val _accounts = MutableStateFlow<List<PlatformAccount>>(emptyList())
    val accounts: StateFlow<List<PlatformAccount>> = _accounts
    private var hasLoadedOnce = false

    fun loadAllAccounts(force: Boolean = false) {
        viewModelScope.launch {
            if (!force && hasLoadedOnce && _accounts.value.isNotEmpty()) return@launch
            _accounts.value = accountsRepository.loadAllAccounts()
            hasLoadedOnce = true
        }
    }

    fun deleteAccount(account: PlatformAccount) {
        viewModelScope.launch {
            when (account.platform) {
                PlatformType.Steam -> accountsRepository.deleteSteamAccount(account.id)
                PlatformType.Epic -> accountsRepository.deleteEpicAccount(account.id)
            }
            _accounts.value = accountsRepository.loadAllAccounts()
        }
    }

    suspend fun refreshEpicAccount(account: EpicAccount): Boolean {
        val tokens = epicAuthRepository.refreshTokens(account.refreshToken) ?: return false
        accountsRepository.saveEpicAccount(
            account.copy(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                nickname = tokens.displayName ?: account.nickname
            )
        )
        withContext(Dispatchers.Main) {
            _accounts.value = accountsRepository.loadAllAccounts()
        }
        return true
    }
}
