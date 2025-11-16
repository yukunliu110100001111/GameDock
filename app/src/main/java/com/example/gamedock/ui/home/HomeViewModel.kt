package com.example.gamedock.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.local.EpicAccountStore
import com.example.gamedock.data.local.SteamAccountStore
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.SteamAccount
import com.example.gamedock.data.remote.SteamApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _accounts = MutableStateFlow<List<PlatformAccount>>(emptyList())
    val accounts: StateFlow<List<PlatformAccount>> = _accounts

    fun loadAllAccounts(context: Context) {
        viewModelScope.launch {

            val steam = SteamAccountStore.loadAll(context)
            val epic = EpicAccountStore.loadAll(context)

            val merged: MutableList<PlatformAccount> = mutableListOf()
            merged.addAll(steam)
            merged.addAll(epic)

            _accounts.value = merged
        }
    }

    fun deleteAccount(context: Context, account: PlatformAccount) {
        when (account.platform) {
            PlatformType.Steam -> SteamAccountStore.delete(context, account.id)
            PlatformType.Epic -> EpicAccountStore.delete(context, account.id)
        }
        loadAllAccounts(context)
    }
}
