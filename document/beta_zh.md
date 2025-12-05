# GameDock – Beta 阶段工作总结

## 已完成
- 核心功能上线：限免聚合、跨商店比价、礼包搜索、愿望单与价格提醒、账号登录与内置 WebView 领取。
- 实时数据源：Epic 官方免费游戏接口 + GamerPower 聚合；IsThereAnyDeal (ITAD) 价格与礼包接口。
- 端到端 Hilt 注入（`GameDockApplication` + `NetworkModule` / `RepositoryModule`），Retrofit/OkHttp 直接访问外部源。
- 背景提醒：WorkManager `PriceCheckWorker` 每 6 小时轮询愿望单并推送通知。

## 交付细节
- **限免与领取**：`DealsRepositoryImpl` 汇总 `EpicStoreAdapter` + `GamerPowerStoreAdapter`；`FreebiesScreen` 将活动分为进行中/即将开始，Claim 时按平台选账号并打开 `CustomClaimActivity`（继承 `AccountWebViewActivity`，自动注入 Authorization 或 Steam Cookie）。
- **价格比价**：`ItadAdapter` 调用 ITAD API，按系统语言推断地区；`CompareViewModel` 实现防抖搜索、升降序切换、`BestOfferSummary` 最佳价摘要，并支持一键加入愿望单。
- **礼包搜索**：`BundlesViewModel` 复用 ITAD 礼包接口，支持输入搜索、按价格排序、剩余时间徽章，卡片可跳转商店链接。
- **账号体系**：Steam 登录 `SteamLoginActivity` 捕获完整 Cookie，AES 加密存储在 `SteamAccountStore`；Epic 登录 `EpicLoginActivity` + `EpicAuthRepositoryImpl` 交换/刷新 Token，并存入 `EpicAccountStore`；`HomeScreen` 列表/删除账号并可跳转个人页。
- **愿望单与提醒**：`WatchlistRepositoryImpl` 通过 SharedPreferences 持久化（覆盖/删除/更新价格）；`WatchlistScreen` 展示与删除；`CompareViewModel.addToWatchlist` 支持从结果加入；`MainActivity.schedulePriceCheckWorker` 注册周期任务，`PriceCheckWorker` 触发 `PriceDropNotifier` 发送降价/免费通知。
- **UI 与导航**：单 Activity Compose + 底部导航；`NavGraph` 覆盖 Home/Freebies/Compare/Watchlist/Bundles/Settings 及账号添加路由；`PriceCard`、`WatchlistCard` 等组件包含骨架、徽章和商店跳转。

## 缺口与风险
- 礼包仅支持搜索，无默认 feed。
- 目前没有本地缓存愿望单机制，愿望单与账号基于 SharedPreferences，缺少离线缓存与迁移方案。
- `SettingsScreen` 开关未持久化，也未与通知权限联动。
- 错误与空态处理有限（网络失败、API 限流）；ITAD API Key 硬编码在客户端。
- Claim 流程默认取首个同平台账号，缺少账号选择或多账号统一 UI。
- UI缺少整体美化，观感不佳

## 下一步
1) 为 Bundles 提供默认 feed，支持无搜索浏览。  
2) 落地 Room + DataStore（愿望单/设置），补全离线缓存与迁移；为后台任务添加网络/电量约束。  
3) 完善账号选择与错误提示（多账号选择、Claim 重试），并将 ITAD Key/配置外置以便切换。  
4) 添加 UI 优化，添加整体美化。
