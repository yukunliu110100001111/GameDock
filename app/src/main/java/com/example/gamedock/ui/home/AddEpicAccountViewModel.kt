package com.example.gamedock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.repository.AccountsRepository
import com.example.gamedock.data.repository.EpicAuthRepository
import com.example.gamedock.data.repository.model.EpicAuthTokens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEpicAccountUiState(
    val statusMessage: String = "点击下方按钮，在官方页面登录你的 Epic 账号。",
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
)

@HiltViewModel
class AddEpicAccountViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val epicAuthRepository: EpicAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEpicAccountUiState())
    val uiState: StateFlow<AddEpicAccountUiState> = _uiState.asStateFlow()

    fun completeAuthorization(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            val tokens = epicAuthRepository.exchangeAuthCode(code)
            if (tokens == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "授权失败，请重试。"
                    )
                }
                return@launch
            }

            accountsRepository.saveEpicAccount(tokens.toAccount())
            _uiState.value = AddEpicAccountUiState(
                statusMessage = "账号已保存！",
                isCompleted = true
            )
        }
    }

    fun onLoginCancelled() {
        _uiState.update {
            it.copy(
                statusMessage = "已取消 Epic 登录流程。",
                errorMessage = null,
                isProcessing = false
            )
        }
    }

    fun onMissingCode() {
        _uiState.update {
            it.copy(
                statusMessage = "未能获取授权 code，请重试。",
                errorMessage = null,
                isProcessing = false
            )
        }
    }

    fun resetCompletionFlag() {
        _uiState.update { it.copy(isCompleted = false) }
    }

    private fun EpicAuthTokens.toAccount(): EpicAccount {
        return EpicAccount(
            id = accountId.ifBlank { accessToken.takeLast(16) },
            accessToken = accessToken,
            refreshToken = refreshToken,
            nickname = displayName ?: "Epic User",
            avatar = ""
        )
    }
}
