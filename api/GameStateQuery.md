# GameStateQuery 状态查询

提供一组静态方法，供外部模组在任意时刻主动查询 `killsayApiSdk` 的当前运行状态。

所有方法均为 `KillsayEvents` 类的静态方法。

## 方法列表

| 方法 | 返回值 | 说明 |
|------|------|------|
| `isEnabled()` | `boolean` | 检测模块是否启用 |
| `getCooldownRemainingMs()` | `long` | 当前剩余冷却时间，单位毫秒 |
| `isDeadPause()` | `boolean` | 是否处于死亡暂停状态 |
| `isVictory()` | `boolean` | 是否处于胜利状态 |
| `getDoneName()` | `String` | 最近一次检测完成的目标玩家名 |
| `getTrackedPlayers()` | `List<PlayerTrackingInfo>` | 当前追踪中的玩家快照 |
| `getConfig()` | `ConfigSnapshot` | 当前配置快照 |

## 说明

### `isEnabled()`

返回检测模块总开关当前值。

### `getCooldownRemainingMs()`

返回剩余冷却时间。

- 返回 `0` 表示当前不在冷却期
- 冷却由一次检测完成后开始计时

### `isDeadPause()`

玩家死亡后会进入一个短暂暂停窗口，此方法在该时间内返回 `true`。

### `isVictory()`

检测到胜利提示后会进入一个短暂胜利窗口，此方法在该时间内返回 `true`。

### `getDoneName()`

返回最近一次检测完成的目标玩家名。

- 从未触发过时返回空字符串
- 不返回 `null`

## 示例

```java
if (!KillsayEvents.isEnabled()) return;

long cd = KillsayEvents.getCooldownRemainingMs();
if (cd > 0) {
    System.out.println("cooldown: " + cd);
}

if (KillsayEvents.isDeadPause()) {
    System.out.println("dead pause");
}
```
