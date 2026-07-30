# CBC Firepower Components Ponder 教程技术设计

## 1. 总体方案

两个游戏版本分别实现一个客户端 `PonderPlugin`，使用各自已经声明的 Ponder 依赖：

- NeoForge 1.21.1：Ponder 1.0.82；
- Forge 1.20.1：Ponder 1.0.80。

教程入口、场景注册、场景脚本和结构资源均属于客户端表现层，不修改服务器功能逻辑，也不增加网络包。

## 2. 注册结构

### 2.1 类结构

每个版本增加：

```text
client/ponder/
├── MTPonderPlugin.java
├── MTPonderScenes.java
├── CannonMountScenes.java
├── AmmunitionScenes.java
├── FireControlScenes.java
└── EquipmentScenes.java
```

1. `MTPonderPlugin` 实现 `PonderPlugin`，返回模组 ID，并委托 `MTPonderScenes.register`。
2. `MTPonderScenes` 使用物品注册表键函数，把一个场景关联到一个或多个方块/物品条目。
3. 现有客户端初始化事件在 `FMLClientSetupEvent` 中执行一次：

```java
PonderIndex.addPlugin(new MTPonderPlugin());
```

4. Ponder 只在客户端类中加载，专用服务器不会解析场景类。

### 2.2 可选模组隔离

场景注册只引用：

- 本模组注册对象；
- Minecraft 原版方块；
- Create 公共方块和物品；
- CBC 本体中已经是必需依赖的公共内容。

不直接导入 CBCMS、CBCAT、CBCMW/CBCNW 或 Drive By Wire 的类。兼容性通过文字和本模组实际通用接口说明，因此可选模组未安装时不会产生类加载错误。

## 3. 场景与条目关联

### 3.1 NeoForge 1.21.1

| 场景 ID | 关联条目 | 主要分镜 |
|---|---|---|
| `mounts/compact` | 两种紧凑炮座 | 炮座出现、放置炮体、动力连接、正确方向 |
| `mounts/vertical` | 竖向紧凑炮座 | 两侧支架、横向连接杆、向上/向下装炮 |
| `large_autocannon/single` | 单管炮闩、三种单管炮管部件、两种炮弹 | 组装、供弹、开火、复进、制退器效果 |
| `large_autocannon/twin` | 双联炮闩、双联炮管、双联制退器 | 双管组装、左右交替开火、炮口发射点 |
| `ammo/autocannon_feed` | 机炮供弹器、大型机炮弹药箱 | 弹药箱只以单件使用、供弹方向、普通/高爆弹 |
| `ammo/magazine_loader` | 火炮弹匣装填器 | 弹头与发射药配对、形成完整发射弹、向火炮输出 |
| `ammo/ready_compartment` | 智能待发弹药舱 | 单发仓位、顺延输入、自动前移、机械臂输入输出、直接供炮 |
| `ammo/carousel` | 转盘式待发弹药架 | 3×3 结构、24 发、躺平显示、输出口、旋转进位、GUI 排序和取回 |
| `logistics/spent_collector` | 空药筒回收器 | 范围收集、筒口俯视、空/部分/接近满、机械臂取出 |
| `control/automatic_controller` | 自动火炮控制器 | 短按、长按 GUI、潜行右键、背面上升沿、点射/连续、轮询/齐射、可选线控 |
| `equipment/limiter` | 火炮限制器 | 选择并限制火炮活动范围 |
| `equipment/rangefinding` | 望远镜 | 按键测距、碰撞点、实际世界空间距离、无目标识别 |

结构方块本身不作为可见索引条目；转盘结构由转盘主方块场景统一解释。

### 3.2 Forge 1.20.1

复用相同命名和叙述习惯，但只注册：

- `mounts/compact`；
- `ammo/autocannon_feed`；
- `ammo/magazine_loader`；
- `equipment/limiter`；
- `equipment/shield`。

套筒式机枪盾仅存在于 1.20.1 注册表，因此只在 1.20.1 注册教程。

同一场景可以关联多个相关条目，避免重复场景。

## 4. 结构资源

结构文件放在：

```text
assets/cbc_firepower_components/ponder/<scene_id>.nbt
```

### 4.1 布局原则

- 普通单方块功能：5×5 或 7×5 基板；
- 紧凑炮座和大型机炮：7×7 基板；
- 转盘：至少 9×9 基板，保证完整显示 3×3 多方块和输出侧；
- 控制器：控制器居中，炮座与弹药设备分列两侧，背面保留红石输入空间；
- 测距：使用长条场景，在视线末端设置实体方块目标。

### 4.2 结构生成

优先通过开发环境结构方块保存真实摆放结果，避免手写方块状态造成朝向错误。若无法启动客户端，则使用 NBT 生成脚本创建最小结构，并通过结构解析检查验证方块 ID、尺寸和调色板。

## 5. 场景脚本

### 5.1 静态结构与动态状态

结构 NBT 只保存初始状态。场景脚本负责：

- 分层显示结构；
- 框选输入、输出和红石面；
- 显示右键、潜行右键、物品输入等操作图标；
- 切换红石状态；
- 生成运动中的物品实体；
- 移动独立炮管区段表示复进；
- 修改本模组方块实体的教程状态，表现队列、弹壳堆积和调度变化。

Ponder 世界不会依赖真实服务器持续运行，因此涉及队列和状态变化的画面采用脚本化状态推进。推进结果必须使用与实际方块相同的仓位顺序、容量和模式枚举。

### 5.2 公共辅助方法

`MTPonderScenes` 或独立包内辅助类提供：

- 统一的标题、镜头和基板设置；
- 输入/输出箭头和颜色；
- 单发物品输入动画；
- 红石上升沿动画；
- 场景结束停顿。

不建立过度抽象的场景 DSL；各场景仍保留明确坐标，便于结合结构文件检查。

### 5.3 方块实体演示接口

优先调用现有公开方法改变演示状态。若现有方块实体没有安全的客户端演示入口，则增加包内可见的、仅负责设置显示数据的方法，例如：

- 设置教程队列快照；
- 设置教程回收量；
- 设置教程控制模式。

这些方法不改变真实游戏规则，不接受玩家调用，不参与网络同步。

## 6. 文本与本地化

Ponder 自动按场景 ID 生成如下键：

```text
cbc_firepower_components.ponder.<scene>.header
cbc_firepower_components.ponder.<scene>.text_1
cbc_firepower_components.ponder.<scene>.text_2
...
```

所有场景脚本中的英文文本作为默认文本写入，随后在：

- `assets/cbc_firepower_components/lang/en_us.json`
- `assets/cbc_firepower_components/lang/zh_cn.json`

补齐稳定键值。中文使用项目现有术语：

- 智能待发弹药舱；
- 转盘式待发弹药架；
- 空药筒回收器；
- 自动火炮控制器；
- 轮询、齐射、三发点射、连续射击。

每段文字控制在 Ponder 可读长度内；复杂交互拆成多个关键帧，不显示类似当前方块提示中那种横跨屏幕的长句。

## 7. 兼容与降级

1. 场景不实例化可选模组方块。
2. 兼容弹药以通用弹药图标和文字说明表达。
3. Drive By Wire 只在控制器场景最后作为可选连接方式出现；红石部分先完整演示。
4. 若 Ponder API 在 1.20.1 缺少某个 1.21.1 使用的视觉指令，则使用基础文字、框选和物品实体动画降级，不通过反射调用新 API。

## 8. 验证方案

### 8.1 静态检查

- 每个注册场景均存在对应 NBT；
- NBT 调色板中的模组方块 ID 均已注册；
- 每个场景的 `header` 和所有 `text_N` 同时存在于中英文语言文件；
- 所有关联条目均有有效物品注册键；
- 客户端 Ponder 类不会从通用初始化路径在服务端加载。

### 8.2 构建检查

- `:neoforge-1.21.1:build`；
- `:forge-1.20.1:build`；
- 检查最终 JAR 内的场景类、NBT 和双语文本；
- 计算新的测试 JAR SHA-256；
- 将 1.21.1 测试 JAR复制到 `G:\.minecraft\versions\迷彩战车测试\mods`。

### 8.3 游戏内检查

由用户测试：

- 每个关联条目可进入 Ponder；
- 结构朝向和镜头无遮挡；
- 文字不越界；
- 动画与真实功能一致；
- 可选模组缺失时仍可查看；
- 切换中文和英文后无翻译键泄漏。

## 9. 风险与处理

| 风险 | 处理 |
|---|---|
| 两版 Ponder API 细节不同 | 分版本实现插件和场景类，只共享命名与验收规则 |
| 多方块结构朝向再次出错 | 用真实方块状态生成 NBT，并做调色板/属性检查 |
| 方块实体在 Ponder 世界不自动同步 | 使用场景脚本推进显示快照 |
| 场景文字过长或遮挡 | 每段只解释一个动作，关键点分帧 |
| 可选模组类加载失败 | 场景代码不直接引用可选模组类 |
| 教程与后续功能发生偏差 | 集中定义容量、模式和术语；功能变更时同步更新场景 |
