# killsayApiSdk

`killsayApiSdk` 是从 `Killsay Reborn` 拆出来的精简版 Fabric 客户端模组。

这个目录只保留两部分：

- 检测模块
- 对外 API

已经移除的内容：

- 所有 GUI
- 所有 say 发送逻辑
- wanxin
- gufa
- auto reply
- auto report
- HUD

当前保留的检测能力：

- 玩家攻击后进入追踪窗口
- 投掷物接触追踪
- 聊天命中补追踪
- 玩家死亡检测
- 胜利状态检测
- 冷却状态维护

对外入口类：

- `mojang.minecraft.uuidget.KillsayEvents`

主要源码：

- `src/main/java/mojang/minecraft/uuidget/ClientInitializer.java`
- `src/main/java/mojang/minecraft/uuidget/KillsayEvents.java`
- `src/main/java/mojang/minecraft/uuidget/ClientOptions.java`
- `src/main/java/mojang/minecraft/uuidget/HealthTracker.java`

构建产物名：

- `killsayApiSdk`
