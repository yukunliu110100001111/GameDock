package com.example.gamedock.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.local.SteamAccountStore
import com.example.gamedock.data.model.SteamAccount
import com.example.gamedock.data.remote.SteamApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _accounts = MutableStateFlow<List<SteamAccount>>(emptyList())
    val accounts: StateFlow<List<SteamAccount>> = _accounts

    /**
     * 从本地加载所有 Steam 账号（含自动获取昵称 & 头像）
     */
    fun loadAccounts(context: Context) {
        viewModelScope.launch {

            // 先从本地加载（立即显示）
            val list = SteamAccountStore.loadAll(context).toMutableList()
            _accounts.value = list

            // 后台更新头像 & 昵称（不会阻塞 UI）
            val updatedList = withContext(Dispatchers.IO) {
                list.map { acc ->
                    async {
                        val result = SteamApi.fetchSteamProfile(acc.id)
                        if (result != null) {
                            acc.nickname = result.first
                            acc.avatar = result.second
                        }
                        acc
                    }
                }.map { it.await() }
            }

            // 更新 UI
            _accounts.value = updatedList

            // 写回本地缓存
            SteamAccountStore.saveList(context, updatedList)
        }
    }

    /**
     * 删除账号
     */
    fun deleteAccount(context: Context, steamId: String) {
        viewModelScope.launch {
            val list = SteamAccountStore
                .loadAll(context)
                .filterNot { it.id == steamId }

            SteamAccountStore.saveList(context, list)
            _accounts.value = list
        }
    }

    /**
     * 从 AddAccountScreen 返回时刷新
     */
    fun refresh(context: Context) = loadAccounts(context)
}