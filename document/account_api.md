# GameDock 账号集成接口说明

本文档仅围绕“账号系统”进行说明，便于其他功能（如领取游戏、权益校验等）在不用了解具体实现的情况下复用现有能力。

---

## 1. Repository 层

### `AccountsRepository`
路径：`com.example.gamedock.data.repository.AccountsRepository`

```kotlin
interface AccountsRepository {
    suspend fun loadAllAccounts(): List<PlatformAccount>
    suspend fun saveSteamAccount(account: SteamAccount)
    suspend fun saveEpicAccount(account: EpicAccount)
    suspend fun deleteSteamAccount(id: String)
    suspend fun deleteEpicAccount(id: String)
    suspend fun findAccount(platform: PlatformType, id: String): PlatformAccount?
}
```

- `loadAllAccounts()`：返回当前设备上所有已保存账号。
- `findAccount(platform, id)`：当已知平台和账号 id（例如用户列表中选中某个账号）时，可直接获取该账号对象。

> 通过 Hilt 注入即可使用，例如：
> ```kotlin
> @HiltViewModel
> class SomeViewModel @Inject constructor(
>     private val accountsRepository: AccountsRepository
> ) : ViewModel()
> ```

### `AccountCredentialsProvider`
路径：`com.example.gamedock.data.repository.AccountCredentialsProvider`

```kotlin
interface AccountCredentialsProvider {
    suspend fun getCredentials(
        platform: PlatformType,
        accountId: String
    ): AccountCredentials?
}

data class AccountCredentials(
    val platform: PlatformType,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val cookies: Map<String, String> = emptyMap()
)
```

- Epic 账号：返回 `accessToken / refreshToken`。
- Steam 账号：返回 cookies（`steamLoginSecure`、`sessionid` 等）。

当需要调用平台接口或在 WebView 中注入身份信息时，只需请求该接口即可。

---

## 2. 内置账号 WebView

### `AccountWebViewActivity`
位置：`com.example.gamedock.ui.home.AccountWebViewActivity`

这是一个可复用的基础 Activity，负责：
1. 读取 Intent 中的账号信息（平台、账号 ID、目标 URL）。
2. 通过 `AccountCredentialsProvider` 获取凭证。
3. 根据平台自动注入 Authorization 头（Epic）或 cookies（Steam）。
4. 在 WebView 中打开目标网址。

你只需要继承 `AccountWebViewActivity` 并覆写 `resolveConfig()`，指定要使用的账号和目标地址即可。

### 已提供的 Activity

| Activity | 作用 | 需要传入的 Extra |
|----------|------|------------------|
| `EpicProfileActivity` | 打开 Epic 个人资料页 | `EXTRA_ACCOUNT_ID` |
| `SteamProfileActivity` | 打开 Steam 个人资料页（可选自定义 URL） | `EXTRA_ACCOUNT_ID`、可选 `EXTRA_PROFILE_URL` |
| `SteamLoginActivity` | 在内置 WebView 中执行 Steam 登录并回传 Cookie | 无（直接调用即可） |

`SteamLoginActivity` 在加载登录页前会清空 WebView 的 Cookie，并在用户完成登录后捕获 **完整的 Cookie 集合**（包含 `steamLoginSecure`、`sessionid` 等），通过 `Intent` extras 一并返回。调用方可以在 `ActivityResult` 中读取：

```kotlin
val launcher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val secure = result.data?.getStringExtra(SteamLoginActivity.EXTRA_STEAM_LOGIN_SECURE)
        val sessionId = result.data?.getStringExtra(SteamLoginActivity.EXTRA_SESSION_ID)
        val cookies = result.data
            ?.getSerializableExtra(SteamLoginActivity.EXTRA_COOKIES) as? HashMap<String, String>
        if (!secure.isNullOrBlank() && !sessionId.isNullOrBlank()) {
            accountsRepository.saveSteamAccount(
                SteamAccount(
                    id = extractSteamId(secure),
                    steamLoginSecure = secure,
                    sessionid = sessionId,
                    cookies = cookies ?: emptyMap()
                )
            )
        }
    }
}
launcher.launch(Intent(context, SteamLoginActivity::class.java))
```

如果要打开其它页面，只需创建新的 `AccountWebViewActivity` 子类，例如：

```kotlin
class CustomClaimActivity : AccountWebViewActivity() {
    override fun resolveConfig(): WebViewConfig? {
        val accountId = intent.getStringExtra("account_id") ?: return null
        val url = intent.getStringExtra("target_url") ?: return null
        return WebViewConfig(
            platform = PlatformType.Epic,
            accountId = accountId,
            targetUrl = url
        )
    }
}
```

---

## 3. 标准使用方式

1. 通过 `AccountsRepository.loadAllAccounts()` 或 `findAccount(...)` 获取需要的账号。
2. 启动 `EpicProfileActivity`、`SteamProfileActivity`，或你自定义的 `AccountWebViewActivity` 子类，并传入账号 ID 以及目标 URL（如适用）。
3. Activity 会自动注入凭证并在内置 WebView 中打开页面，用户无需离开 App。

---

## 4. 扩展建议

若要支持新的平台或功能，遵循以下步骤：

1. 在 `AccountCredentialsProviderImpl` 中添加新平台的凭证处理逻辑（必要时存储完整 cookie）。
2. 如需其它页面或内置登录，可编写新的 `AccountWebViewActivity` 子类，或复用 `SteamLoginActivity` / `SteamProfileActivity` 等现有实现。
3. 在业务层根据需要选择账号（自定义对话框或其它 UI），然后启动对应 Activity。

这样可以保持所有账号敏感信息只在 Repository/Provider 层处理，业务层只负责“选择账号 + 打开页面”，实现简单而安全的复用。
