# Killsay Reborn — 维护指南

> mod ID: `uuidget` | MC: 1.21.4 | Java 21 | Fabric

---

## 目录

- [项目结构](#项目结构)
- [核心模块速查](#核心模块速查)
- [功能模块详解](#功能模块详解)
- [Mixin 说明](#mixin-说明)
- [配置系统](#配置系统)
- [广播 API](#广播-api)
- [语言文件](#语言文件)
- [常见维护任务](#常见维护任务)
- [版本升级流程](#版本升级流程)
- [注意事项 / 已知约定](#注意事项--已知约定)

---

## 项目结构

```
killsay-reborn/
├── build.gradle                  构建配置
├── gradle.properties             版本号统一在这里改
├── CHANGELOG.md                  改动记录
├── api/                          公开 API 文档（给第三方 mod 看）
└── src/main/
    ├── java/mojang/minecraft/uuidget/
    │   ├── ClientInitializer.java       ← 核心逻辑（最重要的文件）
    │   ├── KillsayEvents.java           ← 广播 API
    │   ├── mixin/                       ← 所有 Mixin
    │   ├── *Options.java                ← 配置类（4个）
    │   ├── *Screen.java                 ← GUI 界面（7个）
    │   ├── HudRenderer.java             ← HUD 渲染
    │   └── ModLang.java                 ← 语言切换
    └── resources/
        ├── fabric.mod.json
        ├── uuidget.mixins.json          ← 注册 Mixin 的地方
        └── assets/uuidget/lang/
            ├── zh_cn.json
            └── en_us.json
```

---

## 核心模块速查

| 要改什么 | 找哪个文件 |
|---|---|
| 击杀检测逻辑 | `ClientInitializer.java` |
| 死亡检测逻辑 | `ClientInitializer.java` + `MessageHandlerMixin.java` |
| 自动举报 / 疯狂举报 | `ClientInitializer.java` + `ReportOptions.java` |
| 箱子偷剑 | `ClientInitializer.java` + `HandledScreenMixin.java` |
| 自动回复 | `ClientInitializer.java` + `AutoReplyOptions.java` |
| 古法发送 | `ClientInitializer.java` + `GufaChatScreen.java` |
| 万信齐发 | `ClientInitializer.java` |
| HUD 显示 | `HudRenderer.java` + `HudOptions.java` |
| 主设置界面 | `OptionsScreen.java` |
| 高级设置界面 | `AdvancedOptionsScreen.java` |
| 快捷键绑定 | `KeybindScreen.java` + `KeyBindingAccessor.java` |
| 对外广播事件 | `KillsayEvents.java` |
| 服务器跳转检测 | `ClientPlayNetworkHandlerMixin.java` |
| 聊天消息拦截 | `MessageHandlerMixin.java` |
| 语言文本 | `zh_cn.json` / `en_us.json` |

---

## 功能模块详解

### 击杀检测

**入口:** `ClientInitializer.java`

流程：
1. 玩家攻击目标时记录 UUID + 时间戳（攻击窗口内有效）
2. 目标实体从渲染距离消失时触发候选
3. 聊天消息或死亡标题包含目标名时确认击杀
4. 确认后调用 killsay 发送逻辑，并通过 `KillsayEvents` 广播

死亡检测（防止自己死了还发）用**投票机制**：
- 4 个信号：TP（位置瞬移）、Title 包、旁观者模式切换、聊天中出现自己名字
- 需要 ≥ 2/3 票才判定为死亡，压制 killsay

### 自动举报

**入口:** `ClientInitializer.java` → `ReportOptions.java`

- 普通举报：检测到杀死自己的玩家后发 `/report <name>`
- 疯狂举报：对服务器内所有玩家发举报，有可配置延迟
- 箱子偷剑：举报后等待服务器开箱，自动拿走钻石剑（海像素空岛战争机制）

### 万信齐发

检测聊天中 `[kw]PlayerName!` 格式的消息，替其他同 mod 用户转发 killsay。

### 古法发送

用隐藏 `GufaChatScreen` 绕过常规聊天发送限制。`NoMove` 变体会等玩家静止后再发。

---

## Mixin 说明

所有 Mixin 必须在 `uuidget.mixins.json` 中注册，否则不会生效。

| Mixin 文件 | 注入目标 | 用途 |
|---|---|---|
| `MinecraftClientMixin` | `MinecraftClient` | tick 钩子、世界事件 |
| `ClientPlayNetworkHandlerMixin` | `ClientPlayNetworkHandler` | 服务器跳转检测 |
| `MessageHandlerMixin` | 消息处理器 | 聊天消息拦截（死亡/击杀确认）|
| `GameOptionsMixin` | `GameOptions` | 注入选项 |
| `GameOptionsAccessor` | `GameOptions` | 读取内部字段（Accessor）|
| `KeyBindingAccessor` | `KeyBinding` | 读取按键内部 Map |
| `ChatScreenAccessor` | `ChatScreen` | 古法发送需要访问聊天框内部 |
| `InGameHudMixin` | `InGameHud` | HUD 渲染钩子 + 箱子界面覆盖层 |
| `HandledScreenMixin` | `HandledScreen` | 箱子偷剑进度覆盖层 |

**新增 Mixin 步骤：**
1. 在 `mixin/` 目录下新建类
2. 加 `@Mixin(目标类.class)` 注解
3. 在 `uuidget.mixins.json` 的 `"client"` 数组里加上类名
4. 重新构建

---

## 配置系统

4 个 Options 类各自独立保存 JSON 文件到 `.minecraft/config/`：

| 类 | 保存文件 | 内容 |
|---|---|---|
| `ClientOptions` | `uuidget.json` | 主开关、冷却、窗口时间等 |
| `HudOptions` | `uuidget_hud.json` | HUD 颜色、位置、背景 |
| `ReportOptions` | `uuidget_report.json` | 举报命令、疯狂举报延迟 |
| `AutoReplyOptions` | `uuidget_autoreply.json` | 自动回复规则列表 |

**新增配置项步骤：**
1. 在对应 Options 类加字段（带默认值）
2. 确认 `load()` / `save()` 方法覆盖了新字段
3. 在对应 `*Screen.java` 里加 UI 控件
4. 在语言文件加翻译 key

---

## 广播 API

**文件:** `KillsayEvents.java`

第三方 mod 可通过以下接口监听事件（无需 fabric-api 直接依赖）：

| 接口 | 触发时机 |
|---|---|
| `UserKill` | 击杀确认时 |
| `KillStatusListener` | killsay 状态变化时 |
| `ReportSystemListener` | 举报系统触发时 |
| `AutoReplyListener` | 自动回复触发时 |

只读状态查询方法也在 `KillsayEvents` 中，详见 `api/` 目录文档。

**新增广播事件步骤：**
1. 在 `KillsayEvents.java` 定义新的 `@FunctionalInterface`
2. 添加注册/注销方法和监听器列表
3. 在 `ClientInitializer.java` 合适位置调用广播
4. 在 `api/` 目录补文档

---

## 语言文件

路径：`src/main/resources/assets/uuidget/lang/`

- `zh_cn.json` — 中文（主要维护语言）
- `en_us.json` — 英文

**添加新文本：**
1. 两个文件都要加，key 必须一致
2. 代码里用 `ModLang.get("your.key")` 而非直接用 MC 的 `Text.translatable()`（mod 有独立语言切换逻辑）

---

## 常见维护任务

### 修改 killsay 发送逻辑

定位：`ClientInitializer.java`，搜索 `sendKillsay` 或 `killsay` 相关方法。

### 新增一个设置界面

1. 新建 `XxxScreen.java`，继承 `Screen`
2. 参考 `OptionsScreen.java` 的按钮/滑块写法
3. 从父界面（通常是 `OptionsScreen`）添加跳转按钮

### 新增快捷键

1. 在 `ClientInitializer.java` 用 `KeyBinding` 注册
2. 在 `KeybindScreen.java` 加对应显示行
3. 语言文件加 key 描述

### 修复 Mixin 冲突

先确认注入点（`@At`）是否因 MC 版本更新失效。用 yarn mappings 查新的方法名，更新 `@At(value="INVOKE", target=...)` 的目标描述符。

---

## 版本升级流程

1. **`gradle.properties`** — 更新 `minecraft_version`、`yarn_mappings`、`fabric_version`
2. **`fabric.mod.json`** — 更新 `"minecraft": "~x.xx.x"` 依赖约束
3. 重新构建，看 Mixin 有没有因方法签名变化报错
4. 逐一测试击杀检测、死亡检测、举报、HUD
5. **`gradle.properties`** — 更新 `mod_version`（格式：`nextgen-X.X`）
6. **`CHANGELOG.md`** — 记录本次改动

---

## 注意事项 / 已知约定

- **纯客户端 mod**，不要加任何服务端逻辑，`fabric.mod.json` 的 `environment` 必须保持 `"client"`
- 包名是 `mojang.minecraft.uuidget`，是历史遗留，不要改（改了要同步改 `fabric.mod.json`、`uuidget.mixins.json` 等所有引用）
- 死亡投票阈值（≥2/3）是故意设低的，改之前想清楚误判代价
- 箱子偷剑逻辑与海像素空岛战争的具体箱子格式强绑定，换服务器/换版本大概率要重写
- 古法发送依赖 `ChatScreenAccessor`，MC 更新聊天框内部结构时最先出问题
- `KillsayEvents` 是公开 API，改接口签名是 breaking change，要在 CHANGELOG 里明确标注
