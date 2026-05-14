# UserKill 检测完成事件

当目标被检测为完成条件时触发，携带目标玩家名。

这是 `killsayApiSdk` 最核心的事件，适合用于统计、联动其他模组、显示提示等场景。

## 接口

```java
public interface KillsayEvents.UserKill {
    void onUserKill(String victimName);
}
```

## 注册方式

```java
KillsayEvents.registerUserKill(listener);
KillsayEvents.unregisterUserKill(listener);
```

## 触发时机

- 攻击追踪目标后，目标被判定为完成
- 聊天补追踪命中后，目标被判定为完成
- 投掷物追踪链路命中后，目标被判定为完成

## 示例

```java
KillsayEvents.registerUserKill(victimName -> {
    System.out.println("detected: " + victimName);
});
```

## 说明

- 回调异常会被静默忽略
- `victimName` 不应为 `null`
