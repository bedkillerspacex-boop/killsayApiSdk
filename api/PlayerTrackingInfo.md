# PlayerTrackingInfo 追踪目标信息

提供当前检测模块正在追踪的目标玩家列表快照。

适合用于：

- 调试追踪逻辑
- 显示当前追踪目标
- 外部统计

## 数据结构

```java
public record KillsayEvents.PlayerTrackingInfo(
    String name,
    long windowDeadline,
    String source,
    float predictedHealth
) {}
```

## 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 目标玩家名 |
| `windowDeadline` | `long` | 当前追踪窗口截止时间戳 |
| `source` | `String` | 追踪来源，`attack` 或 `chat` |
| `predictedHealth` | `float` | 预测血量，若不可用则为 `-1.0f` |

## 来源说明

### `attack`

通过攻击实体直接进入追踪。

### `chat`

通过聊天命中、投掷物补追踪、实体消失后的补追踪进入追踪。

## 示例

```java
for (KillsayEvents.PlayerTrackingInfo info : KillsayEvents.getTrackedPlayers()) {
    System.out.println(info.name() + " " + info.source());
}
```
