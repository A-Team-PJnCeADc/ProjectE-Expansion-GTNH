# BigInteger EMC API（供 AutoEMC-GTNH / ProjectE-Team 等联动使用）

## 背景：为什么物品 EMC 只有 2,147,483,647

ProjectE 本身把**物品 EMC 价**存成 `int`：

* 1.7.10 fork：`EMCMapper.emc = Map<SimpleStack, Integer>`、`EMCHelper.getEmcValue(...) → int`、
  `IEMCProxy.registerCustomEMC(ItemStack, int)`。
* 上游 PE master（1.21）**同样是 int**（`int getValue(...)`、`registerCustomEMC(..., int)`）。

上游 Project Expansion 的 `BigInteger` 只用在**机器**上：`IEmcStorageBigInteger`（继承 PE 的
`IEmcStorage`，转回 long 时用 `Util.safeLongValue` 截断）与机器缓冲/上限（EMC link `16^level`、
relay transfer = `Long.MAX_VALUE`、power flower 输出等）。物品价依旧受 PE 的 int 表限制。

因此本 mod 采取「选项 C」：**PE 表保持 int（保证与所有 PE 附属兼容），精确值由本 mod 用
BigInteger 维护并对外暴露**；同时用 Mixin 在运行时给 PE 的转化桌（收费点、ECM 上限、GUI 显示）
与冷凝器打补丁，让这些界面用的是**精确值**，不需要重编 ProjectE。

* 精确值来源：`registry/PXEmcTable`（BigInteger，运行时由 `registry/PXEmc` 在
  `FMLServerStartedEvent` 计算并填充）。
* 短刻度显示：`util/EMCFormatPX`（M / B / T / Qa … No，最大 1e60）。
* 向量换算：写回 PE 时用 `toProjectEInt`（int 封顶）/`toProjectEDouble`（PE 玩家 EMC 是 double，
  大数可容纳）。

## 公开 API

类：`com.projecte.expansion.api.PXEmcValues`（全部 `public static`，服务端安全，无客户端类依赖）
`API_VERSION = "2"`

| 方法 | 说明 |
| --- | --- |
| `boolean hasValue(ItemStack)` | 本 mod / provider / PE 是否有该物品的价 |
| `BigInteger getValue(ItemStack)` | 精确值（无则 null） |
| `BigInteger getValue(Item)` | 按物品取（本 mod 自己的表，无则 null） |
| `BigInteger getValueOrZero(ItemStack)` | 精确值（无则 0） |
| `BigInteger getOwnValue(Item)` | 本 mod 自己算出的值（不含 PE 回退） |
| `Map<Item, BigInteger> snapshot()` | 全部精确值的不可变快照 |
| `boolean exceedsProjectE(ItemStack)` | 是否超出 PE 的 int 表 |
| `void register(Item, BigInteger)` | **写入**：注册/覆盖精确值，并镜像进 PE int 表（截断） |
| `void register(ItemStack, BigInteger)` | 同上（按 stack 的物品） |
| `void addProvider(IPXEmcProvider)` | 注册**惰性取值源**（见下），适合无法逐项注册的场景 |
| `String format(BigInteger)` | 短刻度：`2.72Qi` |
| `String formatExact(BigInteger)` | 千分位：`2,720,389,434,207,968,576` |
| `int toProjectEInt(BigInteger)` | 写进 PE int 表时的截断值 |
| `double toProjectEDouble(BigInteger)` | 转 PE 玩家 EMC（double） |
| `BigInteger fromProjectEDouble(double)` | 反向换算 |

### 接口：`IPXEmcProvider`

```java
public interface IPXEmcProvider {
    BigInteger getEmcValue(ItemStack stack);   // 不处理则返回 null（不要返回 0）
}
```

查询顺序：**本 mod 自己的表 → 已注册的 provider → ProjectE 的 int 表**。
provider 的值会被转化桌/冷凝器的运行时补丁直接使用（含 >21 亿的值），因此 AutoEMC 不必逐项注册。
实现要求：快、线程安全、异常会被吞掉（不影响其它 provider 与 PE 回退）。

## 用法（有编译依赖）

```java
// 读：ProjectE-Team 记团队 EMC 时用精确价
BigInteger exact = PXEmcValues.getValue(stack);
if (exact == null) exact = BigInteger.valueOf(EMCHelper.getEmcValue(stack)); // PE 回退
if (PXEmcValues.exceedsProjectE(stack)) {
    LOG.info("{} = {}", PXEmcValues.format(exact), PXEmcValues.formatExact(exact));
}

// 写：AutoEMC-GTNH 求出的值直接注册（>21 亿也能完整保留）
PXEmcValues.register(item, bigIntegerValue);

// 惰性：值太多/动态计算时实现接口
PXEmcValues.addProvider(new IPXEmcProvider() {
    @Override public BigInteger getEmcValue(ItemStack stack) {
        return MyEmcEngine.lookup(stack);   // 不处理返回 null
    }
});
```

## 用法（不想加编译依赖时，反射）

```java
Class<?> api = Class.forName("com.projecte.expansion.api.PXEmcValues");
Object version = api.getField("API_VERSION").get(null);            // "2"
Object value = api.getMethod("getValue", ItemStack.class).invoke(null, stack);   // BigInteger 或 null
String text  = (String) api.getMethod("formatExact", BigInteger.class).invoke(null, value);
api.getMethod("register", Item.class, BigInteger.class).invoke(null, item, bigValue);
```

## 运行时被改动的 ProjectE 行为（Mixin，无需重编 PE）

| 补丁 | 作用 |
| --- | --- |
| 转化桌 卖（`SlotConsume`） | 拖入消耗槽 → 按精确 BigInteger 价入账 |
| 转化桌 卖/买（`TransmutationContainer.transferStackInSlot`） | shift-click 按精确价 |
| 转化桌 买（`SlotOutput`） | 点击输出槽按精确价扣款 |
| 转化桌 EMC 上限（`TransmutationInventory.addEmc/hasMaxedEmc`） | 掀掉 PE 的 `Constants.TILE_MAX_EMC`（2^30）夹取 |
| 转化桌 EMC 显示（`GUITransmutation`） | 不再用 `(int) inv.emc` 截断，显示 `2.72Qi` 级数值 |
| 锁定槽（`SlotLock`） | EMC 变大后余量计算不再变负（否则放克莱因星会倒扣 EMC） |
| 冷凝器 MKI / MKII（`condense`） | 烧本 mod 物品按精确价储能 |

补丁失效是**静默**的（`require = 0`），并在首次触发时向日志打一行 `PX patch active: ...` 便于诊断。

## 注意事项

* 值由服务端在 `FMLServerStartedEvent` 计算：专用服务器上只有服务端有；单机两端同 JVM 都有；
  多人客户端调用会回退到 PE 的 int 值。
* 玩家 EMC 本身是 double（`TransmutationProps.transmutationEmc`，存档与网络包都是 double），
  所以转化桌可以持有远超 int 的值；但 double 只有 53 位尾数，超过约 9e15 后不再精确到个位
  （1e18 量级误差约几百，1e60 量级只能表示量级）。需要逐位精确的场合请用本 mod 的
  BigInteger 表 / 机器缓冲，而不是 PE 的 double 字段。
* 若要**彻底**让 PE 自己的合成台/EMC 映射表以 BigInteger 交易，只能改 PE 本身
  （`EMCMapper` 改 long/BigInteger + API 签名 + GUI/包同步），并让所有 PE 附属重编——本 mod 不这么做。

## 所有者 EMC / 知识访问（转化接口用）

接口：`com.projecte.expansion.api.IPXEmcAccess`

```java
double getEmc(EntityPlayer owner);                   // 团队共享时为团队池
void   addEmc(EntityPlayer owner, double amount);    // 增量：>0 记入，<0 扣除
List<ItemStack> getKnowledge(EntityPlayer owner);     // 团队共享知识时为团队列表
boolean isShared(EntityPlayer owner);                // 是否在使用团队数据
```

入口：`com.projecte.expansion.registry.PXEmcAccess`

```java
PXEmcAccess.register(myAccess);   // 覆盖默认实现（例如 ProjectE-Team 想直接对接时）
PXEmcAccess.getEmc(player);
PXEmcAccess.addEmc(player, +amount);
```

选择顺序：**其它 mod 注册的实现** → 装了 ProjectE-Team 时的反射实现（`ProjectETeamAccess`，调用
`TeamManager.isSharingEMC/isSharingKnowledge/getTeamEmc/onEmcAdd/onEmcRemove/getTeamKnowledge`）
→ 纯 ProjectE 实现（`Transmutation.getEmc/setEmc/getKnowledge`）。

要点：

* **必须用增量（`addEmc`）而不是绝对写入**：团队共享时 ProjectE 的绝对 `setEmc` 会被 ProjectE-Team
  忽略（团队池是权威值）。
* 物品价一律取 `PXEmcTable`（BigInteger 精确值）；只有**余额**受 ProjectE 存储类型限制是 double。
* ProjectE-Team 无需任何改动即可生效（本 mod 走反射）；它也可以注册自己的 `IPXEmcAccess` 实现来替代反射。

## 转化接口（Transmutation Interface）的运行时行为

| 项 | 实现 |
| --- | --- |
| 对外存储 | 1.7.10 原生 `IInventory` + `ISidedInventory` → AE2 的 ME 存储总线（`StorageBusInventoryHandler`）与各类管道直接可用，**不需要 AE 编译依赖** |
| 槽位 0（输入） | 放入的物品在下一 tick 转化为所有者（团队）的 EMC 并学习该物品；物品先暂存在槽内，所有者离线时不吞物品 |
| 槽位 1..n | 所有者已学习物品；报出的数量 = EMC ÷ 精确价（受 `transmutationInterfaceItemCount` 上限约束，默认 `Integer.MAX_VALUE`）；取出按精确价扣 EMC |
| NBT 过滤 | 元数据 0（红，默认）= 带 NBT 或已消耗耐久的物品不接收；1（蓝）= 忽略 NBT。所有者用贤者之石潜行右击切换 |
| 显示 | 普通右击显示所有者；Waila 显示所有者/生效 EMC/已学习数量/过滤模式（可选依赖 `libs/Waila-1.8.15.jar`）；NEI 面板走物品 tooltip（精确 EMC + 用法说明） |
| 团队 | 所有者所在团队开启共享时，EMC 与知识都走团队池（见上） |

## 团队 EMC 的 BigInteger 模式(与 ProjectE-Team 的软依赖)

团队池的权威值有两种形态,由 **ProjectE-Team 侧**按"装了 PE-E-GTNH 没有"自动切换:

| 环境 | 团队池 | 上限 | 行为 |
| --- | --- | --- | --- |
| 装 PE-E-GTNH | `TeamData.emcBig`(BigInteger,存档用十进制字符串) | 无(只受 double 视图限制) | 高价物品按精确值累加,不再被写成上限值 |
| 未装 | `TeamData.emc`(double) | `Constants.TILE_MAX_EMC`(2^30) | 保持原行为(避免团队池超上限导致成员容器 shift-click 存不进去) |

判定只依赖 modid(`TeamEmcMode.BIG_EMC_MODID = "projectexpansiongtnh"`),ProjectE-Team 不需要对
本 mod 有任何编译期依赖。

**本 mod 的接入点**(反射,缺失则自动回退):

* `TeamManager.getTeamEmcBig(EntityPlayer) → BigInteger`
* `TeamManager.addTeamEmcBig(EntityPlayer, BigInteger) → boolean`(增量,负数为扣除;返回 false 表示当前不是 big 模式)

本 mod 通过 `IPXEmcAccess#getEmcBig / addEmcBig` 使用它们(默认实现回退到 double 路径):

```java
BigInteger exact = PXEmcAccess.getEmcBig(owner);          // 团队模式 = BigInteger 团队池
PXEmcAccess.addEmcBig(owner, price.multiply(count));      // 精确入账;自动回退
```

转化接口(以及后续机器)因此**全程精确**:物品价取 `PXEmcTable`,余额在团队模式下取 BigInteger 池。
只有 ProjectE 自己的 double 视图(玩家 props、客户端显示、`TeamEmcPKT`)仍是 double —— 超过约 9e15
后是近似值,这是 PE 存储类型决定的,不影响服务端记账。

## AutoEMC-GTNH 的接入方式(已实现)

AutoEMC 通过 `com.gtnh.autoemc.compat.ProjectExpansionCompat`(全反射,无编译依赖)对接本 mod:

| 方向 | 调用 | 行为 |
| --- | --- | --- |
| 读基础价 | `PXEmcValues.getValue(ItemStack)` / `hasValue` | 优先用本 mod 的精确值(超 int 时按原逻辑钳到 `Integer.MAX_VALUE - 1`,因为 AutoEMC 的运算仍是 int) |
| 写算出的值 | `PXEmcValues.register(Item, BigInteger)` | 写进本 mod 的精确表,并镜像进 ProjectE 的 int 表 → 转化桌/冷凝器/机器按精确值记账 |

没装本 mod 时 `ProjectExpansionCompat` 全部返回 null/false,AutoEMC 完全走原来的 ProjectE 路径(行为不变)。
