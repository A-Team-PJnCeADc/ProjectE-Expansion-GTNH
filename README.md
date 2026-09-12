# ProjectE-Expansion-GTNH

ProjectE-Expansion-GTNH 是 **[Project Expansion](https://github.com/DonovanDMC/Project-Expansion)**面向 **Minecraft 1.7.10 / GT New Horizons** 的移植版,依赖经典等价交换模组 **[ProjectE](https://github.com/sinkillerj/ProjectE/tree/MC17)**。上游的收集器、中继器、EMC 链接、力量花、能量凝聚器、高级炼金箱、转化接口、物质/燃料/星星体系与全部配方都在本模组里,资源域统一为 `projectexpansiongtnh`。

本项目**不重新编译 ProjectE**——PE 的数值上限(int 物品价、2^30 转化桌上限)全部用 Mixin 在运行时打补丁,并对外提供一套精确 EMC 的 API/接口给 ProjectE-Team 与 AutoEMC-GTNH 使用。上游的 EMC 是 BigInteger(量级可达约 1e60),而 PE 1.7.10 只有 int 的物品价与 double 的玩家 EMC,所以本模组自己维护一张精确值表,PE 侧只在边界收窄。

## Progress

- [x] M1 注册骨架:方块/物品/TileEntity/创造标签,注册名与贴图沿用上游
- [x] M2 EMC 数值与全部配方:自有求价引擎(`PXEmc`)求解 171 项自有物品(每槽最低替代 + min 配方 + long 递归 + 多轮 fixpoint),最高 2.72Qi
- [x] M3 机器与容器:转化接口(精确记账)、凝聚器运行时补丁、接口的原生 `IInventory`/`ISidedInventory`
- [x] 渲染:箱子类 3D 化、物品栏 3D 图标、压缩收集器 3D、力量花复合模型(JSON 模型在 1.7.10 不可用,自写 Tessellator)
- [x] 精确 EMC:BigInteger 表 + 对外 API/接口(`PXEmcValues` / `IPXEmcProvider` / `IPXEmcAccess`)
- [x] PE 运行时补丁(全部 Mixin,不重编 PE):转化桌收银口三个入口、2^30 上限、GUI EMC 行、锁定槽、凝聚器
- [x] 与 ProjectE-Team 联动:团队池按 BigInteger 记账(软依赖),转化桌团队 EMC 逐位精确显示
- [x] 与 AutoEMC-GTNH 联动:读价优先精确值,写价走栈级注册(软依赖)
- [x] 汉化:en_US / zh_CN 
- [ ] 高级炼金箱的物品栏与 GUI
- [ ] 能量凝聚器 MK3 的逻辑与 GUI
- [ ] 收集器 / 中继器 / 力量花的生产 EMC 与 EMC 传输
- [ ] EMC 链接的传输逻辑
- [ ] 炼金书 / 知识共享书 / 奥术转化桌的功能

## 核心机制

- **精确 EMC 表**:`PXEmcTable` 维护三张表——栈级(物品 + 元数据 + NBT)、元数据级(只按物品 + damage,兜底带 NBT/充能状态的同一变体)、物品级(本模组自有物品);查询顺序为 栈级 → 元数据级 → 物品级 → 已注册的 `IPXEmcProvider` → 最后才回落到 ProjectE 的 int 表
- **只读式集成**:不重编 ProjectE。PE 的收银口(`SlotConsume` 拖入、`TransmutationContainer` shift-click、`SlotOutput` 点输出)、转化桌 EMC 上限(`addEmc`/`hasMaxedEmc`)、锁定槽、凝聚器全部由 Mixin 在运行时改写,名字对不上时静默失效而不是崩溃(`require = 0`)
- **Late mixin**:所有打向 ProjectE 的 mixin 走 `LateMixinLoader`(`ILateMixinLoader`),并从 jar 的 MANIFEST 里移除 `MixinConfigs` —— 否则配置会在 ProjectE 进入类加载器之前被处理,导致真实实例在 `PECore.preInit` 崩溃
- **价格上限 1e60**:上游量级在 1e60 一档,而 PE 的玩家 EMC 是 double(约 1.8e308)。写回 PE 的 int 表时夹取,本模组侧保留精确 BigInteger
- **团队模式**:装了 ProjectE-Team 且开启共享时,买卖走团队 BigInteger 池(由 ProjectE-Team 侧精确累加),客户端通过 `TeamEmcPKT` 的十进制字段拿到逐位精确值显示;没装则完全走个人 EMC
- **软依赖**:ProjectE-Team 与 AutoEMC-GTNH 都通过反射/API 接入,任一未安装时本模组行为不变;反之 AutoEMC 与 Team 在本模组缺席时也保持原样
- **与 AutoEMC-GTNH 的分工**:AutoEMC 负责为 GTNH 数万种物品补价,通过 `PXEmcValues.register(ItemStack, BigInteger)` 写进本模组的精确表,并镜像进 ProjectE 的 int 表

## 参考

- [Project Expansion](https://github.com/DonovanDMC/Project-Expansion)

## 依赖

- **Minecraft 1.7.10** / **Forge 10.13.4.1614**
- **[ProjectE 1.7.10](https://github.com/sinkillerj/ProjectE/tree/MC17)**
- **[Waila](https://www.mcmod.cn/class/246.html)(可选)**
- **[ProjectE-Team](https://github.com/A-Team-PJnCeADc/ProjectE-Team)(可选)**
- **[AutoEMC-GTNH](https://github.com/A-Team-PJnCeADc/AutoEMC-GTNH)(可选)**
- 构建环境:Java 8+(GTNH 约定构建脚本,支持 jabel 现代语法)

## API

对外 API:`PXEmcValues`(精确值读写、栈级注册)、`IPXEmcProvider`(惰性值来源)、`IPXEmcAccess`(玩家/团队 EMC 与知识的精确读写)。细节见 [`docs/emc-api.md`](docs/emc-api.md)。

## 构建与开发

```bash
# 克隆仓库
git clone git@github.com:A-Team-PJnCeADc/ProjectE-Expansion-GTNH.git
cd ProjectE-Expansion-GTNH

# 首次构建
./gradlew setupDecompWorkspace

# 构建模组(含 spotless 格式检查F)
./gradlew spotlessApply build

# 产物
build/libs/projectexpansiongtnh-*.jar
```

## 贡献

欢迎提交 Pull Request 或 Issue。在提交前请确保:

1. 构建没有错误(`./gradlew spotlessApply build` 通过,含 spotless 格式检查);仓库行尾一律 **LF**(见 `.editorconfig` / `.gitattributes`)
2. 打向 ProjectE 的补丁必须用 **Late mixin**(`@LateMixin` + `ILateMixinLoader`),并从 jar 的 MANIFEST 移除 `MixinConfigs`;
   注解里的方法/字段名要用**发布版 PE jar** 的 `javap` 核实(mod 自有成员加 `remap = false`,MC 成员用默认 remap),
   所有注入一律 `require = 0` —— 名字对不上时静默失效,而不是崩游戏
3. 精确 EMC 值一律进 `PXEmcTable`(栈级 / 元数据级 / 物品级),只在边界收窄:写回 ProjectE 用 `clampToPeInt`、显示用 `clampToInt`;
   带元数据的物品(如 GT metaitem)必须用栈级重载注册,未知值(`null`)绝不当 0 参与计算
4. 对外 API 的改动要同步 `docs/emc-api.md`,并保持**软依赖**:装了/没装 ProjectE-Team、AutoEMC-GTNH 时行为都要正确
5. 客户端专用代码放 `client/` 包,客户端 mixin 只能列在 `mixins.projectexpansiongtnh.json` 的 `client` 数组里
6不要重新编译 ProjectE:PE 侧的行为改动全部通过 Mixin 完成

## 致谢

- 所有参与开发、测试、建议的人员

## License

<a href="https://github.com/A-Team-PJnCeADc/ProjectE-Expansion-GTNH">ProjectE-Expansion-GTNH</a> © 2026 by <a href="https://github.com/A-Team-PJnCeADc">A-Team-PJnCeADc</a> is licensed under <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International</a>



