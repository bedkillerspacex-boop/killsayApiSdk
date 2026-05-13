# UserKill 事件

## 简介

每次 killsay 成功发送后触发，携带被击杀玩家的名字。这是 killsay 最核心的事件，适合用于统计击杀数、触发成就系统、联动其他模组等场景。

---

## 接口定义

```java
@FunctionalInterface
public interface KillsayEvents.UserKill {
    void onUserKill(String victimName);
}
```

### 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `victimName` | `String` | 被击杀玩家的游戏 ID |

---

## 注册 / 注销

```java
// 注册
KillsayEvents.registerUserKill(listener);

// 注销
KillsayEvents.unregisterUserKill(listener);
```

同一个 listener 实例重复注册只会生效一次（内部做了去重）。

---

## 触发时机

- 非古法模式：`net.sendChatMessage()` 发送击杀消息后立即触发
- 古法模式（gufa）：在 `trigger()` 方法入口处触发，即击杀确认时，与 GufaChatScreen 的实际发送时间无关

> **注意**：此事件触发早于冷却计时器更新，即触发时 `getCooldownRemainingMs()` 可能仍为 0。

---

## 使用示例

### Lambda 写法

```java
KillsayEvents.registerUserKill(victimName -> {
    System.out.println("击杀了: " + victimName);
});
```

### 类写法（支持注销）

```java
public class MyMod implements ClientModInitializer {

    private final KillsayEvents.UserKill killListener = this::onKill;

    @Override
    public void onInitializeClient() {
        KillsayEvents.registerUserKill(killListener);
    }

    private void onKill(String victimName) {
        // 处理击杀事件
    }

    public void onDisable() {
        KillsayEvents.unregisterUserKill(killListener);
    }
}
```

---

## 注意事项

- 回调在 **Minecraft 主线程** 执行，不要在回调内做耗时操作
- 回调抛出的任何异常都会被静默忽略，不会影响 killsay 本身
- victimName 与服务器玩家列表中的 ID 一致，不含颜色代码
