# killsayApiSdk API

当前版本：`1.0.0`

入口类：`mojang.minecraft.uuidget.KillsayEvents`

这个 SDK 只保留检测模块和只读 API。

已移除：

- wanxin
- gufa
- auto reply
- report
- GUI
- HUD
- 所有聊天发送逻辑
- 所有发送后状态 API

## API 列表

| 文档 | 类型 | 用途 |
|------|------|------|
| [UserKill](api/UserKill.md) | 事件监听 | 检测完成后，获得目标玩家名 |
| [KillStatusListener](api/KillStatusListener.md) | 事件监听 | 监听检测完成 / 玩家死亡 / 游戏胜利 |
| [PlayerTrackingInfo](api/PlayerTrackingInfo.md) | 数据查询 | 获取当前正在追踪的目标玩家列表 |
| [GameStateQuery](api/GameStateQuery.md) | 状态查询 | 查询死亡暂停、胜利、最近一次完成目标等状态 |
| [ConfigSnapshot](api/ConfigSnapshot.md) | 配置查询 | 获取当前检测配置快照 |

## 快速开始

```java
import mojang.minecraft.uuidget.KillsayEvents;

KillsayEvents.registerUserKill(victimName -> {
    System.out.println("detected: " + victimName);
});

KillsayEvents.getTrackedPlayers().forEach(info ->
    System.out.println(info.name() + " [" + info.source() + "]")
);
```

## 设计原则

- 只读：API 只提供检测结果与状态读取
- 不发送：SDK 不会发送任何聊天消息
- 不改行为：外部模组不能通过 API 改写内部检测逻辑
- 容错：监听器异常会被吞掉，不影响主模块
