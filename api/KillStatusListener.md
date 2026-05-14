# KillStatusListener 检测状态监听

监听 `killsayApiSdk` 的全局状态变化。

保留事件：

- 检测完成
- 本地玩家死亡
- 游戏胜利

## 接口

```java
public interface KillsayEvents.KillStatusListener {
    default void onDone(String victimName) {}
    default void onDeath() {}
    default void onVictory() {}
}
```

## 方法说明

### `onDone(String victimName)`

当一次目标检测完成时触发。

### `onDeath()`

当本地玩家被判定进入死亡暂停窗口时触发。

### `onVictory()`

当检测到胜利状态时触发。

## 注册方式

```java
KillsayEvents.registerStatusListener(listener);
KillsayEvents.unregisterStatusListener(listener);
```

## 示例

```java
KillsayEvents.registerStatusListener(new KillsayEvents.KillStatusListener() {
    @Override
    public void onDone(String victimName) {
        System.out.println("done: " + victimName);
    }

    @Override
    public void onDeath() {
        System.out.println("dead");
    }

    @Override
    public void onVictory() {
        System.out.println("victory");
    }
});
```
