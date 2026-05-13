# PlayerTrackingInfo 玩家追踪信息

## 简介

提供当前 killsay 正在追踪的目标玩家列表快照，包含每个目标的名字、追踪窗口截止时间、追踪来源。适合用于 HUD 显示追踪目标、调试追踪逻辑、统计攻击方式分布等场景。

---

## 数据结构

```java
public record KillsayEvents.PlayerTrackingInfo(
    String name,           // 玩家 ID
    long   windowDeadline, // 追踪窗口截止时间（System.currentTimeMillis() 时间戳）
    String source,         // 追踪来源："attack" 或 "chat"
    float  predictedHealth // 预测血量 (1.21.4 特性)
) {}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 被追踪玩家的游戏 ID |
| `windowDeadline` | `long` | 追踪窗口的过期时间戳（毫秒），超过此时间该追踪条目会被丢弃 |
| `source` | `String` | 追踪来源，见下表 |
| `predictedHealth` | `float` | 预测的精确血量。如果来源是 `"attack"` 且正在模拟中，则为当前预测值；否则为 `-1.0f` |

### source 取值

| 值 | 含义 |
|----|------|
| `"attack"` | 通过直接攻击玩家（鼠标左键）加入追踪。此模式下会开启高精度血量模拟。 |
| `"chat"` | 通过聊天消息匹配（投掷物命中、伤害反馈或实体消失后转移）加入追踪。此模式下无法获取实时血量。 |

---

## 血量模拟算法说明

由于服务器通常会伪造实体的 `max_health` 属性，客户端直接读取的属性往往是虚假的。本 Mod 采用以下混合预测算法：

1. **整数锚定 (Integer Anchor)**：强制同步服务器发送的 Scoreboard (计分板) `BELOW_NAME` 整数分值。
2. **损伤模拟 (Damage Simulation)**：通过 Mixin 拦截实体的红闪信号（Entity Status 2），结合攻击者的攻击力、力量等级以及目标的护甲、韧性（这些属性是广播的，无法隐藏）进行实时伤害扣除。
3. **动态钳制 (Dynamic Clamping)**：将模拟结果实时钳制在计分板显示的 [score, score + 1) 区间内，消除累积误差。

---

## 使用示例

### 在 HUD 中显示追踪目标及预测血量

```java
List<KillsayEvents.PlayerTrackingInfo> targets = KillsayEvents.getTrackedPlayers();
long now = System.currentTimeMillis();

int y = 10;
for (KillsayEvents.PlayerTrackingInfo info : targets) {
    long remaining = info.windowDeadline() - now;
    if (remaining <= 0) continue;

    String healthStr = info.predictedHealth() != -1.0f 
            ? String.format("%.1f", info.predictedHealth()) 
            : "N/A";

    String text = String.format("%s [%s] HP:%s 剩余%ds", 
            info.name(), info.source(), healthStr, remaining / 1000);
            
    ctx.drawTextWithShadow(textRenderer, text, 5, y, 0xFFFFFF);
    y += 12;
}
```

### 判断特定玩家是否在追踪列表中

```java
String targetName = "SomePlayer";
boolean isTracked = KillsayEvents.getTrackedPlayers()
        .stream()
        .anyMatch(t -> t.name().equals(targetName));
```

### 计算追踪窗口剩余时间

```java
KillsayEvents.getTrackedPlayers().forEach(info -> {
    long remainingMs = info.windowDeadline() - System.currentTimeMillis();
    System.out.printf("追踪目标: %-16s 来源: %-8s 剩余: %dms%n",
            info.name(), info.source(), Math.max(0, remainingMs));
});
```

---

## 追踪生命周期说明

```
攻击玩家 ──→ pending["attack"]
              │
              ├─ 实体死亡/隐身 ──→ chatWatch["chat"]（等待聊天确认）
              │                       │
              │                       └─ 聊天命中 ──→ 入发送队列 ──→ killsay 发送
              │
              └─ 实体消失/超时 ──→ chatWatch["chat"]（同上）

投掷物命中/伤害反馈 ──→ chatWatch["chat"]
```

追踪条目在以下情况下会被移除：
- 追踪窗口过期（`windowDeadline` 小于当前时间）
- killsay 成功发送
- 玩家死亡或切服（队列清空）

---

## 注意事项

- 返回的列表为不可变副本，遍历时不需要加锁
- `windowDeadline` 是绝对时间戳，用 `windowDeadline - System.currentTimeMillis()` 换算剩余时间
- 列表为某一时刻的快照，获取后内部状态可能已经变化，不要缓存列表用于多帧
- 该方法在 Minecraft 主线程以外调用时存在轻微的数据竞争风险（内部使用普通 HashMap），建议仅在主线程调用
