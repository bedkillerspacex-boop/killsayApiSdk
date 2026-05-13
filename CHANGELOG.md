# Changelog

## nextgen-4.0
- **新增：高精度玩家血量预测 API**
  - 核心逻辑：结合计分板整数锚定 (Integer Anchor) 与 Mixin 拦截红闪信号进行实时损伤模拟 (Damage Simulation)。
  - 动态钳制：将模拟血量实时钳制在服务器计分板数值范围内，消除累积误差。
  - API 扩展：`PlayerTrackingInfo` 现在包含 `predictedHealth` 字段，支持实时获取目标血量。
- **重构：万信齐发 (Wanxin) 逻辑**
  - 自定义识别：移除旧版正则限制，改用更稳健的字符串提取方式，**原生支持含特殊符号和空格的中文 ID**。
  - 独立延迟队列：引入 `pendingWanxins` 队列，万信 Relay 延迟不再与系统冷却 (Cooldown) 冲突，支持排队自动发送。
  - GUI 恢复：重新开放设置界面中的万信齐发开关，不再硬编码为关闭。
- **API 增强**：
  - 新增 `WanxinListener` 接口：支持监听万信匹配 (`onWanxinMatch`) 和实际发送 (`onWanxinSent`) 事件。
  - 新增 `getPendingWanxins()` 接口：可查询当前万信延迟队列的任务快照。
  - API 文档同步更新，明确了版本号和配置文件说明。

## nextgen-3.9
- 优化 API 触发机制：击杀检测与广播事件现已移出 killsay 开关守卫，即使 killsay 关闭，API 仍能正常追踪目标并广播 UserKill/KillStatus 等事件（仅抑制聊天消息发送）
- 修复打开任意界面时狂暴举报快捷键仍会触发的问题
- 使用须知新增：不可跳脸、不得发布包含 GUI 的任何文件
- 新增广播 API（KillsayEvents）：每次 killsay 成功发送后触发 USER_KILL 事件，携带被击杀玩家 ID
- 修复 USER_KILL 事件受古法 NoMove 影响的问题：事件现在在击杀确认时立即触发，与发送模式无关
- 扩展广播 API：新增 KillStatusListener（冷却开始/killsay 发送成功/死亡/胜利）、ReportSystemListener（举报目标/举报成功/狂暴举报开始与进度）、AutoReplyListener（自动回复匹配/发送）三个事件监听接口
- 新增只读状态查询 API：isEnabled、getCooldownRemainingMs、isDeadPause、isVictory、isReportSuccess、isAutoReplySuccess、getReportedName、getDoneName、isGufaPending
- 新增 getTrackedPlayers() 接口：返回当前正在追踪的目标玩家列表（名字、窗口截止时间、来源：attack/chat）
- 新增 getConfig() 接口：返回 killsay 当前配置快照（开关、冷却、窗口时间等）
- **修复攻击检测 Mixin 冲突问题**：移除 `ClientPlayerInteractionManagerMixin`，改用 Fabric API `AttackEntityCallback` 事件监听，兼容性大幅提升（兼容 BetterCombat 等修改战斗逻辑的 mod）
- 修复实体死亡判定：硬死亡（isDead/血量归零）现直接入队，不再等待聊天公告确认，漏触发概率大幅降低
## nextgen-3.8
- 修复粘贴发送（GLFW 路径）在 GUI 打开时误触发的问题：现在只在游戏内（无界面）时响应
- 修复自动回复直接发送模式下 cooldown 在发送前提前设置的问题：现改为发送完成后再设置
- 修复古法 NoMove 模式下自动回复 cooldown 被双倍延迟的问题：停止移动后发送时不再重复设置 cooldown
- 举报设置界面新增"立即狂暴举报"按钮：无需绑定快捷键即可在 GUI 内一键触发狂暴举报
- 新增 HeyPixel 防刷屏 Bypass 开关（主设置界面）：检测到服务器返回"请不要刷屏或者发送重复消息哦!(Xs)"后，自动将冷却延长至 X+0.5 秒；开启显示 BYPASS，关闭显示 OFF

## nextgen-3.7
- 新增举报次数限制检测：聊天框检测到"举报次数限制"后自动关闭自动举报和狂暴举报功能
- 狂暴举报延迟范围扩大：从 0.1~1.0秒 扩大到 0.1~5.0秒，适应不同服务器限制

## nextgen-3.6
- 修复狂暴举报只举报部分玩家的问题
- 修复狂暴举报不拿钻石剑导致举报无效的问题：每次举报后都触发开箱拿剑
- 狂暴举报进度显示：在箱子界面顶部显示举报进度（已举报/总数）

## nextgen-3.5
- 狂暴举报新增延迟配置：可在快捷键设置中配置每条举报消息的间隔时间（0.1~1.0秒，默认0.5秒），防止因发送过快被服务器踢出
- 狂暴举报改为异步队列发送，按配置的延迟逐条发送举报命令
- 狂暴举报期间自动关闭箱子：如果箱子界面打开超过1.5秒，自动按ESC关闭，防止卡在箱子界面
- 狂暴举报期间按ESC键可立即取消举报
- 狂暴举报进度显示：在箱子界面顶部显示举报进度（已举报/总数），解决打开箱子时看不到HUD的问题

## nextgen-3.4
- 新增狂暴举报模式：按下配置的快捷键后举报除自己外的所有玩家
- 举报名称过滤：自动移除玩家名中的方括号内容（如 `[VIP]PlayerName` → `PlayerName`）和 Minecraft 颜色代码（§ 符号）
- 快捷键配置移至 GUI：狂暴举报和粘贴发送的快捷键现可在「快捷键设置」界面中配置，默认均为未绑定
- 新增独立的「快捷键设置」二级菜单，可从主设置界面进入
- 举报设置界面新增「快捷键」入口按钮

## nextgen-3.3
- 修复 killsay 死后发送的问题：chatWatch/pending 命中后改为 200ms 延迟队列，死亡检测优先执行，死后自动丢弃
- 修复击杀后投掷物命中别人误触发死亡冷却的问题：击杀成功后 500ms 内屏蔽 selfname 死亡票，覆盖服务器伤害反馈消息的误触发窗口
- 自动举报 HUD 改为显示被举报玩家 ID：「举报成功: PlayerName」

## nextgen-3.2
- 新增自动回复功能（Auto Reply）：支持配置触发词与对应的回复列表，触发时将自动顺序循环回复。与 killsay 共用冷却和底层发送逻辑（支持古法发送、NoMove、死亡暂停等特性），并在独立二级菜单进行管理。

## nextgen-3.1
- 修复杀完人后触发死亡检测的问题：投 `selfname` 死亡票前检查消息是否同时含有 `pending`/`chatWatch` 中的目标名，含则视为击杀公告跳过投票
- 死亡检测不再覆盖更长冷却：`castDeathVote` 通过时改用 `Math.max` 更新 `cooldownUntil`，防止刚杀人设置的冷却被死亡判定缩短

## nextgen-3.0
- 修复杀死别人时自动举报误触发的问题：`findKillerInMessage` 找到的玩家名若已在 `pending` 或 `chatWatch` 中（即我们正在追踪的击杀目标），则跳过，不写入 `recentKiller`

## nextgen-2.9
- 自动举报凶手检测改为 Tab 列表匹配：消息含自己名字时遍历服务器玩家列表，找另一个出现在消息里的 ID，无需配置格式，天然支持中文 ID
- 修复自动举报无法触发的问题：旧格式方案在未配置时永远返回 null，新方案开箱即用
- 举报相关检测（凶手识别、举报成功）移出 killsay 开关守卫，死亡期间及 killsay 关闭时仍正常运行
- 删除举报设置中的"击杀公告格式"输入框，界面简化
- 「自动拿剑」按钮改名为「举报开箱」

## nextgen-2.8
### Auto Report（自动举报）
- 新增举报设置二级菜单（主设置→「举报设置」），可配置举报命令（默认 `/report {name}`）、死亡公告格式（`{killer}` / `{self}` 占位符）、古法/直接发送模式、自动拿剑开关
- 死亡投票通过后自动提取聊天中的凶手名，下一 tick 发送举报命令
- Chest Stealer：举报发送后等待服务器开箱，延迟 2 tick 自动 Shift 拾取钻石剑
- 聊天返回「举报成功」后，HUD 在「死亡 暂停发送」下方显示绿色「举报成功」，持续 15 秒，无倒计时
- 移除 HUD 中「发送完成!(id:X)」3 秒短暂显示

### 击杀检测优化
- 实体死亡/隐身不再立即触发发送，改为转入 `chatWatch` 等待聊天名字确认（双票机制）
- 适配「隐身=死亡」的服务器：本地玩家变为隐身时自动投 spectator 票，结合聊天 selfname 票即触发死亡投票
- 所有发送路径（chatWatch、pending、古法 NoMove）均改用 `isDeadPause()` 检查本地玩家是否已死
- 死亡投票通过时同步清空 `gufaNoMovePending`，防止死后古法消息延迟发出

### HUD
- 胜利状态不再显示冷却倒计时，改为黄色「Good Game」第二行
- 高级选项：移除投掷物置信度滑块，版面居中简洁化

## nextgen-2.7
- HUD 死亡文字分隔符由 `·` 改为空格
- HUD 胜利状态颜色改为金色
- 修复未接受须知时旧配置 `enabled=true` 仍能使用的问题：加载配置时若须知未确认强制覆盖为关闭

## nextgen-2.6
- 新增胜利检测：收到含「胜利」的 Title 包时 HUD 显示金色「胜利」持续 10 秒，不触发死亡投票
- 修复胜利/进服被误判为死亡的问题：`resetOnTransfer` 新增 5s 切服屏蔽期，期间忽略所有死亡投票信号；死亡投票通过要求 `selfname` 必须为票之一，仅 title+TP 不再触发

## nextgen-2.5
- 新增首次使用须知页：首次打开设置时弹出，5 秒倒计时后方可确认；须知未确认时 mod 默认关闭
- HUD 新增「死亡 暂停发送」状态（红色），死亡投票通过后显示 5 秒
- 死亡投票通过后补设 5s `cooldownUntil`，防止 chatWatch 残留条目在死亡后继续触发

## nextgen-2.4
- 修复击杀公告先于实体消失导致漏触发的问题：`onGameMessage` 现同时检查 `pending` 中的名字，命中即直接触发，不再依赖先转入 `chatWatch`
- 新增聊天消息含自身名字作为第四路死亡投票信号（selfname）
- 修复击杀公告先于实体消失导致漏触发的问题：`onGameMessage` 现同时检查 `pending` 中的名字，命中即直接触发，不再依赖先转入 `chatWatch`

## nextgen-2.3
- 切服冷却期内 pending 实体消失不再输出 debug 日志（静默丢弃，不污染调试）
- 死亡检测补充：SURVIVAL 模式下 allowFlying 从 false→true 触发死亡投票，覆盖服务器自定义死亡处理（非旁观者模式）

## nextgen-2.2
- 修复敌人消失后误触发 / 不触发 killsay 的问题：实体从世界消失时（死亡或离开视野均会消失）自动转入 `chatWatch`，等服务器广播死亡消息再触发；超时无消息则自然过期不发送
- `chatWatch` 消费逻辑不再依赖 `chatProjectileDetect` 开关（该开关只控制投掷物是否加入监控，不影响消费）

## nextgen-2.1
- 死亡检测改为三信号 2/3 投票机制：TP(10格+) / Title包 / 旁观者模式切换，任意两个在 4s 窗口内触发即清空击杀队列，避免单一信号误判

## nextgen-2.0
- 修复本地玩家死亡后误触发 killsay 的问题：服务器死亡走旁观者模式而非原版死亡，现改为检测游戏模式切换至 SPECTATOR 时清空队列
- 伤害反馈消息的玩家名正则适配中文 ID（不再限制为 ASCII 字符）

## nextgen-1.9
- HUD "发送完成" 提示现在显示目标 ID，格式：`发送完成!(id:PlayerName)` / `Done! (id:PlayerName)`
- 语言切换改为 mod 内独立切换（不影响 MC 游戏语言），设置界面新增 EN/中文 按钮，点击即时生效
- 万信齐发：修复旧配置残留导致默认开启的问题，现强制关闭直至 UI 恢复

## nextgen-1.8
- **Heypixel Skywars**: 改用服务器伤害反馈消息（`你对玩家 X 造成了...`）识别命中目标，替代原先的客户端物理包围盒追踪，准确率大幅提升
- **Heypixel Skywars**: 命中目标在渲染距离内时走实体死亡监控，不在时转入聊天消息等待触发
- 按钮更名："测试性投掷物检测" → "Heypixel Skywars"

## nextgen-1.7
- HUD 新增半透明深灰矩形背景，可在 HUD 设置中开关
- HUD 新增文字泛光效果，采用全圆盘采样，强度可通过滑块调节（0.0–2.0）
- HUD 设置新增"背景"开关和"泛光"强度滑块
- 新增英文语言支持（`en_us`），所有界面文字改用 `Text.translatable()`
- 设置界面新增语言切换按钮（EN ↔ 中文），点击后重载资源即时生效
- 万信齐发相关按钮暂时隐藏，待修复后恢复

## nextgen-1.6
- HUD 新增居中模式，每行文字严格以屏幕中心对齐
- HUD 新增方块开关，可隐藏 ■ 前缀
- HUD 新增"发送完成!"状态，发送成功后持续显示 3 秒
- HUD 新增发送完成色（默认天蓝色），可在 HUD 设置中自定义
- HUD 颜色设置面板新增"完成色"输入框及色块预览
- "等待停步"文案改为"等待停止以发送"

## nextgen-1.5
- 修复古法(NoMove)模式下 delay 判定失效的问题
