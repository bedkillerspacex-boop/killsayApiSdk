# KillStatusListener 击杀状态监听

## 简介

监听 killsay 的全局运行状态变化，包括：冷却开始、击杀消息发送成功、本地玩家死亡、游戏胜利。适合用于制作状态 HUD、播放音效、同步外部统计系统等。

---

## 接口定义

```java
public interface KillsayEvents.KillStatusListener {
    default void onCooldownStart(long cooldownMs) {}
    default void onDone(String victimName) {}
    default void onDeath() {}
    default void onVictory() {}
}
```

所有方法都有默认空实现，只需重写感兴趣的方法即可。

---

## 方法说明

### `onCooldownStart(long cooldownMs)`

**触发时机**：killsay 击杀消息发送成功、冷却计时器开始计时时触发。

| 参数 | 类型 | 说明 |
|------|------|------|
| `cooldownMs` | `long` | 本次冷却的总时长（毫秒），由配置中的 `cooldownSeconds` 决定 |

> 注意：古法 NoMove 模式下，玩家停下后 GufaChatScreen 才实际发送消息，此事件不在该路径触发。冷却由 `trigger()` 返回 true 时设置。

---

### `onDone(String victimName)`

**触发时机**：`notifySent()` 被调用时触发，表示击杀消息已成功进入发送流程。

| 参数 | 类型 | 说明 |
|------|------|------|
| `victimName` | `String` | 本次 killsay 的目标玩家 ID，古法模式下可能为 `null` |

> `victimName` 为 `null` 的情况：古法模式中 GufaChatScreen 调用 `notifySent(null)` 时，此时名字已预存在 `doneName` 字段，可通过 `KillsayEvents.getDoneName()` 获取。

---

### `onDeath()`

**触发时机**：死亡投票系统判定本地玩家死亡时触发（投票通过，≥2票且含 selfname 票）。

触发后 killsay 内部会：
- 清空 pending / chatWatch / trackedProj / pendingKillsays 队列
- 设置 5 秒冷却和 5 秒死亡暂停期

> 此事件可能早于游戏实际显示死亡界面，因为死亡检测基于多信号投票（TP 瞬移、Spectator 模式、Title 文字、名字出现在消息中）。

---

### `onVictory()`

**触发时机**：检测到 Title 文字中包含"胜利"时触发。

触发后 killsay 会设置 10 秒胜利状态，期间 `KillsayEvents.isVictory()` 返回 `true`。

---

## 注册 / 注销

```java
KillsayEvents.registerStatusListener(listener);
KillsayEvents.unregisterStatusListener(listener);
```

---

## 使用示例

```java
KillsayEvents.registerStatusListener(new KillsayEvents.KillStatusListener() {

    @Override
    public void onCooldownStart(long cooldownMs) {
        // 例：显示倒计时进度条，时长为 cooldownMs 毫秒
        MyCooldownHud.startCountdown(cooldownMs);
    }

    @Override
    public void onDone(String victimName) {
        // 例：播放击杀音效
        MinecraftClient.getInstance().getSoundManager().play(...);
    }

    @Override
    public void onDeath() {
        // 例：停止所有持续型 HUD 动画
        MyHud.reset();
    }

    @Override
    public void onVictory() {
        // 例：触发胜利烟花特效
        MyEffects.playVictory();
    }
});
```

---

## 注意事项

- 所有回调在 **Minecraft 主线程** 执行
- 回调抛出的任何异常都会被静默忽略
- `onDeath` 与 `onVictory` 互斥：检测到胜利时不会投死亡票，因此二者不会同时触发
