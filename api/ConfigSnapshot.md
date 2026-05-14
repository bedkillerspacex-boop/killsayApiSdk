# ConfigSnapshot 配置快照

返回当前检测模块的只读配置快照。

```java
public record KillsayEvents.ConfigSnapshot(
    boolean enabled,
    boolean chatProjectileDetect,
    boolean transferGuard,
    double windowSeconds
)
```

## 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `enabled` | `boolean` | 检测模块总开关是否开启 |
| `chatProjectileDetect` | `boolean` | 是否启用聊天投掷物追踪模式 |
| `transferGuard` | `boolean` | 是否启用切服 / 传送保护 |
| `windowSeconds` | `double` | 攻击目标后的追踪窗口时长 |

## 示例

```java
KillsayEvents.ConfigSnapshot config = KillsayEvents.getConfig();
System.out.println(config.windowSeconds());
```

## 说明

- 快照是只读的
- 配置变化后不会自动刷新，需重新调用 `KillsayEvents.getConfig()`
