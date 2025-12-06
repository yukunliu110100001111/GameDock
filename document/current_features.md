# GameDock 功能实现概览（当前代码状态）

本文档描述当前代码库的结构与各功能实现方式，便于快速理解与维护。

## 代码结构（主要目录）
- `app/src/main/java/com/example/gamedock/`
  - `ui/`：Jetpack Compose 界面（导航、页面、组件）。
  - `ui/home/`：账号相关流程（添加、登录 WebView、详情、配置 WebView 凭据）。
  - `notifications/`：通知封装。
  - `workers/`：WorkManager 任务（价格检查）。
  - `data/`
    - `model/`：领域模型（Game/Freebie/Offer/BundleDeal 等）。
    - `local/`：本地存储（EncryptedSharedPreferences + Gson 缓存/账号存储）。
    - `remote/`：远端 API 适配（Epic freebies、GamerPower giveaways、ITAD 价格/Bundle、Steam 头像获取、Epic OAuth）。
    - `repository/`：仓库实现（账号、交易、认证、设置）。
    - `watchlist/`：Watchlist 模型与仓库。
    - `util/`：货币、时间等工具。
  - `di/`：Hilt Module 绑定 Retrofit/仓库。
  - `GameDockApplication.kt`：Application 配置、图片加载、启动时刷新 Epic Token。

## 功能实现
### 导航与主框架
- `GameDockApp` + `NavGraph` + `BottomNavBar` 提供单 Activity + Compose 多页导航；底栏路由 Home/Freebies/Compare/Watchlist/Settings。

### 账号管理（Steam/Epic）
- 本地存储：
  - Steam：`SteamAccountStore` 使用 EncryptedSharedPreferences 存 cookie、昵称、头像；加载时会尝试调用 `SteamApi.fetchSteamProfile` 补全头像/昵称并写回。
  - Epic：`EpicAccountStore` 存 access/refresh token 等。
- 账号仓库：`AccountsRepositoryImpl` 统一加载/保存/删除、查找；提供头像回退。
- 凭据提供：`AccountCredentialsProviderImpl` 把存储的 token/cookie 转为 WebView 可用的 header/cookie。
- WebView 登录/使用：
  - Steam 登录：`SteamLoginActivity` WebView 抓取 `steamLoginSecure`、`sessionid` 及 cookies；`AddSteamAccountViewModel` 保存并从 cookie 解析 SteamId。
  - Epic 登录：`EpicLoginActivity` 拦截授权码，`EpicAuthRepositoryImpl` 通过 `EpicAuthApi` 兑换 token；`AddEpicAccountViewModel` 保存账号。
  - WebView 使用：`AccountWebViewActivity` 根据平台注入 cookie 或 Bearer Header，加载目标页（Steam/Epic 个人页、freebie 领取页等），处理渲染进程崩溃重建。
  - 详情页：`SteamAccountDetailScreen` / `EpicAccountDetailScreen` 展示信息、打开 profile WebView、删除账号；Epic 详情可刷新 token。

### Freebies（免费游戏列表与领取）
- 数据：`DealsRepositoryImpl` 调用 `EpicStoreAdapter`（解析 Epic 免费促销）与 `GamerPowerStoreAdapter`（过滤非 Epic）合并；失败时回退本地缓存 `FreebiesCache`。
- UI：`FreebiesScreen` 按 active/upcoming 分组显示；点击“Claim”：
  - 平台无法识别：外部浏览器打开。
  - 需要账号：若无账号提示；多账号弹出选择；单账号直接打开继承 `AccountWebViewActivity` 的 `CustomClaimActivity`，自动注入凭据访问领取页。
- 倒计时/开始结束时间由 `FreebieExtensions` 解析 ISO 时间字符串。

### Compare（价格对比 + Bundles）
- 数据：`ItadAdapter` 使用 ITAD API 搜索游戏、获取价格、对比不同商店的当前价/历史低价，并拉取 bundles。
- UI：`CompareScreen` 支持搜索建议、选择游戏后展示 Offer 列表与 Bundle 列表：
  - 价格卡片显示商店、当前价、历史低价、图片等；支持收藏到 watchlist。
  - 最佳报价展示差价。
  - Bundles 展示剩余时间、价格、包含的游戏。
  - 排序/刷新、加载骨架、空态/错误提示。
- Watchlist 集成：心形按钮调用 `WatchlistRepository` 增删，实时同步收藏状态。

### Watchlist（收藏与通知偏好）
- 存储：`data/watchlist/` 下用 SharedPreferences+Gson 持久化 `WatchlistEntity`，含首选商店、最后价格、通知开关等；`WatchlistRepositoryImpl` 通过 StateFlow 暴露、支持增删改。
- UI：`WatchlistScreen` 列表展示，支持删除、通知开关，并可跳转 Compare 复用查询。

### 背景价格检查与通知
- Worker：`PriceCheckWorker` 周期性执行（MainActivity 注册 WorkManager），读取 watchlist，通过 `DealsRepository` 获取报价：
  - 过滤首选商店；价格变为 0 通知“FREE NOW”；降价触发价格通知；更新 `lastKnownPrice`。
  - 尊重全局通知开关 (`SettingsRepository.notificationsEnabled`) 与 per-item 开关。
- 通知：`PriceDropNotifier` 创建渠道，发送降价/免费通知；Android 13+ 检查通知权限。

### 设置
- `SettingsRepositoryImpl` 用 SharedPreferences 存全局通知开关和 Epic 自动刷新开关（应用启动 `GameDockApplication` 会读取并刷新 Epic token）。
- `SettingsScreen` 提供开关 UI，处理 Android 13+ 通知权限。

### 其他
- 货币格式化：`CurrencyUtils` 使用本地化货币符号。
- 时间格式化：`DateTimeUtils` 安全格式化时间戳。
- 应用入口：`MainActivity` 请求通知权限，创建渠道，调度价格检查 Worker，并预拉 freebies；`GameDockApplication` 自定义 Coil ImageLoader、启动刷新 Epic token。

## 关键流程摘要
- **账号登录**：Steam/Epic WebView 获取凭据 → 仓库保存 → 主页展示 → 凭据用于领取/个人页。
- **Freebies 领取**：仓库抓取 freebies → UI 分组展示 → 选择账号/浏览器 → WebView 注入凭据访问领取页。
- **价格对比/收藏/通知**：选择游戏 → ITAD 获取报价/bundles → 可收藏到 watchlist → 后台 Worker 定期比价 → 通知/更新价格。
