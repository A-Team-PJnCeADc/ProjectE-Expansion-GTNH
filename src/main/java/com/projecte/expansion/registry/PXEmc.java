package com.projecte.expansion.registry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.projecte.expansion.MyMod;
import com.projecte.expansion.util.EMCFormatPX;
import com.projecte.expansion.util.Fuel;
import com.projecte.expansion.util.Matter;

import moze_intel.projecte.emc.EMCMapper;
import moze_intel.projecte.emc.SimpleStack;
import moze_intel.projecte.emc.mappers.APICustomEMCMapper;
import moze_intel.projecte.utils.EMCHelper;

/**
 * 用 BigInteger 运算为所有本模组物品求价(沿用上游 Project Expansion 的语义:
 * 本模组的 EMC 采用 BigInteger,因为机器和高阶物品会超出任何定宽类型),数据来自
 * 移植后的配方图,精确值保存在 {@link PXEmcTable} 中,并额外向 PE 注册
 * 经 int 夹取(clamp)的值(1.7.10 的 PE 只存储 int 物品价值和 double 玩家 EMC)。
 *
 * <p>
 * 循环处理:若某物品的所有配方最终都依赖它自身,则视为 UNKNOWN(不求价,也绝不
 * 缓存为 0)——否则方块&lt;-&gt;物质的逆向配方会通过 min() 把整条颜色链清零。
 * 在 FMLServerStartedEvent 时执行(位于 PE map#1 之后)。
 */
public final class PXEmc {

    private PXEmc() {}

    public static void registerValues() {
        long t0 = System.currentTimeMillis();
        Set<Item> ours = collectOurs();
        Map<Long, List<PXRecipes.ValueRecipe>> graph = new HashMap<>();
        for (PXRecipes.ValueRecipe r : PXRecipes.valueRecipes()) {
            Item out = r.out.getItem();
            if (out == null || !ours.contains(out)) continue;
            graph.computeIfAbsent(key(out, r.out.getItemDamage()), k -> new ArrayList<>())
                .add(r);
        }
        Map<Long, BigInteger> memo = new HashMap<>();
        Set<Long> stack = new HashSet<>();
        // 定点迭代:某物品的依赖在前几轮仍为 UNKNOWN
        // (例如终极收集器依赖稍后才求价的终极之星碎片)会在本轮得到结果
        List<Item> order = new ArrayList<>(ours);
        for (int round = 0; round < 8; round++) {
            boolean more = false;
            for (Item item : order) {
                if (memo.containsKey(key(item, 0))) continue;
                BigInteger v = valueOf(item, 0, graph, memo, stack, ours);
                if (v != null) {
                    memo.put(key(item, 0), v);
                    more = true;
                }
            }
            if (!more) break;
        }
        int registered = 0;
        for (Item item : ours) {
            BigInteger value = memo.get(key(item, 0));
            if (value == null || value.signum() <= 0) continue;
            // 精确值保留在我们这边;PE 拿到的是经 int 夹取(clamp)的副本
            PXEmcTable.put(item, value);
            // 只注册缺失/为零的槽位:PE/玩家已有的正值(含 /setemc)保持不变
            if (EMCHelper.getEmcValue(new ItemStack(item, 1, 0)) == 0) {
                APICustomEMCMapper.instance.registerCustomEMC(new ItemStack(item, 1, 0), PXEmcTable.toPeInt(value));
                registered++;
            }
        }
        EMCMapper.clearMaps();
        EMCMapper.map();
        // CraftingMapper 自带的 int 溢出推断可能会留下零值条目,从而盖过自定义
        // 固定值;这里把仍为零的槽位直接写入最终映射表。
        int stamped = 0;
        for (Item item : ours) {
            if (EMCHelper.getEmcValue(new ItemStack(item, 1, 0)) != 0) continue;
            BigInteger value = memo.get(key(item, 0));
            if (value == null || value.signum() <= 0) continue;
            EMCMapper.emc.put(new SimpleStack(new ItemStack(item, 1, 0)), PXEmcTable.toPeInt(value));
            stamped++;
        }
        BigInteger highest = memo.values()
            .stream()
            .max(Comparator.naturalOrder())
            .orElse(BigInteger.ZERO);
        MyMod.LOG.info(
            "PXEmc: {} own items evaluated (BigInteger), {} fixed EMC values registered, {} stamped post-map, max value {} = {} ({} ms)",
            ours.size(),
            registered,
            stamped,
            highest.toString(),
            EMCFormatPX.format(highest),
            System.currentTimeMillis() - t0);
    }

    private static long key(Item item, int meta) {
        return ((long) Item.getIdFromItem(item) << 8) | (meta & 0xFF);
    }

    /** @return 物品价值;当物品仅含循环依赖时为 null(UNKNOWN,不缓存)。 */
    private static BigInteger valueOf(Item item, int meta, Map<Long, List<PXRecipes.ValueRecipe>> graph,
        Map<Long, BigInteger> memo, Set<Long> stack, Set<Item> ours) {
        long k = key(item, meta);
        BigInteger cached = memo.get(k);
        if (cached != null) return cached;
        if (stack.contains(k)) return null;
        if (!ours.contains(item)) return external(new ItemStack(item, 1, meta));
        List<PXRecipes.ValueRecipe> recipes = graph.get(k);
        if (recipes == null) return null;
        stack.add(k);
        BigInteger best = null;
        for (PXRecipes.ValueRecipe r : recipes) {
            BigInteger total = BigInteger.ZERO;
            boolean usable = true;
            for (List<Object> slot : r.slots) {
                BigInteger c = slotCost(slot, graph, memo, stack, ours);
                if (c == null) {
                    usable = false;
                    break;
                }
                total = total.add(c);
            }
            if (usable && (best == null || total.compareTo(best) < 0)) best = total;
        }
        stack.remove(k);
        if (best == null) return null;
        memo.put(k, best);
        return best;
    }

    private static BigInteger slotCost(List<Object> slot, Map<Long, List<PXRecipes.ValueRecipe>> graph,
        Map<Long, BigInteger> memo, Set<Long> stack, Set<Item> ours) {
        if (slot == null || slot.isEmpty()) return BigInteger.ZERO;
        BigInteger best = null;
        for (Object o : slot) {
            if (o instanceof ItemStack) {
                ItemStack s = (ItemStack) o;
                if (s.getItem() == null) continue;
                BigInteger v = ours.contains(s.getItem())
                    ? valueOf(s.getItem(), s.getItemDamage(), graph, memo, stack, ours)
                    : external(s);
                if (v != null && (best == null || v.compareTo(best) < 0)) best = v;
            } else if (o instanceof String) {
                List<ItemStack> ores = OreDictionary.getOres((String) o);
                if (ores.isEmpty()) return null;
                for (ItemStack s : ores) {
                    if (s.getItem() == null) continue;
                    BigInteger v = ours.contains(s.getItem())
                        ? valueOf(s.getItem(), s.getItemDamage(), graph, memo, stack, ours)
                        : external(s);
                    if (v != null && (best == null || v.compareTo(best) < 0)) best = v;
                }
            }
        }
        return best;
    }

    private static BigInteger external(ItemStack s) {
        return EMCHelper.doesItemHaveEmc(s) ? BigInteger.valueOf(EMCHelper.getEmcValue(s)) : BigInteger.ZERO;
    }

    private static Set<Item> collectOurs() {
        Set<Item> ours = new HashSet<>();
        for (Matter m : Matter.VALUES) {
            addItem(ours, PXItems.MATTER_ITEMS.get(m));
            addItem(ours, PXItems.COMPRESSED_COLLECTORS.get(m));
            addBlock(ours, PXItems.COLLECTORS.get(m));
            addBlock(ours, PXItems.RELAYS.get(m));
            addBlock(ours, PXItems.POWER_FLOWERS.get(m));
            addBlock(ours, PXItems.EMC_LINKS.get(m));
            addBlock(ours, PXItems.MATTER_BLOCKS.get(m));
        }
        for (Fuel f : Fuel.VALUES) {
            addItem(ours, PXItems.FUEL_ITEMS.get(f));
            addBlock(ours, PXItems.FUEL_BLOCKS.get(f));
        }
        for (Item[] type : PXItems.STARS) {
            for (Item star : type) addItem(ours, star);
        }
        for (Block chest : PXItems.CHESTS.values()) addBlock(ours, chest);
        addItem(ours, PXItems.finalStar);
        addItem(ours, PXItems.finalStarShard);
        addItem(ours, PXItems.knowledgeSharingBook);
        addItem(ours, PXItems.arcaneTransmutationTablet);
        addItem(ours, PXItems.basicAlchemicalBook);
        addItem(ours, PXItems.advancedAlchemicalBook);
        addItem(ours, PXItems.masterAlchemicalBook);
        addItem(ours, PXItems.arcaneAlchemicalBook);
        addBlock(ours, PXItems.transmutationInterface);
        addBlock(ours, PXItems.compactSun);
        addBlock(ours, PXItems.condenserMK3);
        return ours;
    }

    private static void addItem(Set<Item> set, Item item) {
        if (item != null) set.add(item);
    }

    private static void addBlock(Set<Item> set, Block block) {
        if (block != null) set.add(Item.getItemFromBlock(block));
    }
}
