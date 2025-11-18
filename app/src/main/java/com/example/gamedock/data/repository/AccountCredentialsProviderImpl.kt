package com.example.gamedock.data.repository

import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.model.account.SteamAccount
import com.example.gamedock.data.repository.model.AccountCredentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountCredentialsProviderImpl @Inject constructor(
    private val accountsRepository: AccountsRepository
) : AccountCredentialsProvider {

    override suspend fun getCredentials(
        platform: PlatformType,
        accountId: String
    ): AccountCredentials? {
        val account = accountsRepository.findAccount(platform, accountId) ?: return null
        return when (account) {
            is EpicAccount -> AccountCredentials(
                platform = PlatformType.Epic,
                accessToken = account.accessToken,
                refreshToken = account.refreshToken
            )

            is SteamAccount -> AccountCredentials(
                platform = PlatformType.Steam,
                cookies = if (account.cookies.isNotEmpty()) account.cookies
                else mapOf(
                    "steamLoginSecure" to account.steamLoginSecure,
                    "sessionid" to account.sessionid
                )
            )

            else -> null
        }
    }
}
