package asudev.blacksmith;

import com.asthereon.asthcore.AsthCore;
import com.asthereon.dots.DotS;
import com.asthereon.dots.Utils.FireworkUtil;
import com.asthereon.dungeoneditor.DungeonEditor;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.item.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;

public class Events implements Listener {

    private Manager manager = Manager.getInstance();

    private Blacksmith plugin = Blacksmith.getPlugin(Blacksmith.class);

    private HashMap<String, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase("Blacksmith")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase("Blacksmith")) {
            if (event.getView().getTopInventory().getItem(25) != null) {
                ItemStack itemreturn = event.getView().getTopInventory().getItem(25);
                DotS.giveItemSafe(event.getPlayer().getUniqueId(), itemreturn);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory gui = event.getClickedInventory();
        if (gui != null) {
            if (gui.getType() != InventoryType.PLAYER) {
                if (event.getView().getTitle().equalsIgnoreCase("Blacksmith")) {
                    event.setCancelled(true);
                    if (event.getRawSlot() == 19 || event.getRawSlot() == 20 || event.getRawSlot() == 21 || event.getRawSlot() == 28 || event.getRawSlot() == 29 || event.getRawSlot() == 30 || event.getRawSlot() == 37 || event.getRawSlot() == 38 || event.getRawSlot() == 39) {
                        if (event.getCurrentItem().getType().equals(Material.AIR)) {
                            return;
                        }
                        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                        if (lore != null && lore.size() > 0) {
                            if (lore.contains(manager.getColored("&aClick to forge!"))) {
                                if (gui.getItem(25) != null) {
                                    manager.send(player, "&cRemove the item in the upgrade slot before forging a new item!");
                                    return;
                                }
                                Integer tier = manager.getTier(DotS.getPlayerLevel(player.getUniqueId()));
                                Boolean exit = false;
                                Integer counter = 0;
                                String rarity = "Common";
                                ItemStack reward = new ItemStack(Material.STONE);
                                ArrayList<ItemStack> mmoitems;
                                while (!exit) {
                                    if (counter == 250) {
                                        exit = true;
                                        break;
                                    }
                                    rarity = manager.getRandomRarity();
                                    mmoitems = manager.getMMOItems(tier, rarity);
                                    if (!mmoitems.get(0).getItemMeta().getDisplayName().equalsIgnoreCase("FAIL ITEM")) {
                                        reward = manager.getRandomMMOItem(mmoitems);
                                        break;
                                    }
                                    counter++;
                                }
                                if (exit) {
                                    manager.send(player, "&cNo forge item was found. Please report this to an admin!");
                                } else {
                                    DungeonEditor.takeGold(player.getUniqueId(), manager.getCost(tier));
                                    manager.send(player, "&aYou forged a " + reward.getItemMeta().getDisplayName() + "&a!");
                                    DotS.giveItemSafe(player.getUniqueId(), reward);
                                    ((Player) player).playSound(player.getLocation(), "dots.e.blacksmith", 10, 1);
                                    if (rarity.equalsIgnoreCase("Epic") || rarity.equalsIgnoreCase("Legendary") || rarity.equalsIgnoreCase("Mythical")) {
                                        FireworkUtil.createRandomFirework(player.getLocation());
                                    }
                                    ItemStack remover = new ItemStack(Material.AIR);
                                    ItemStack itemRarity = manager.getItemRarityItem(rarity);
                                    ItemStack newsquare = ItemStackBuilder.of(Material.STONE_PICKAXE).name("&7").damageValue(91).flag(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES).breakable(false).build();
                                    manager.setInventorySlots(gui, new Integer[]{19, 21, 28, 30, 37, 38, 39, 20, 29}, new ItemStack[]{newsquare, remover, remover, remover, remover, remover, remover, itemRarity, reward});
                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            if (player.getOpenInventory().getTopInventory().equals(gui)) {
                                                manager.openBlacksmithGui((Player) player);
                                            }
                                        }}, 80L);
                                }
                            } else if (lore.contains(manager.getColored("&cNot enough &eGold&c!"))){
                                    manager.send(player, "&cYou do not have enough &eGold &cto forge an item.");
                                    ((Player) player).playSound(player.getLocation(), "dots.e.click_1", 10, 1);
                            }
                        }
                    } else if (event.getRawSlot() == 25) {
                        Inventory blacksmithGui = event.getView().getTopInventory();
                        ItemStack slot25 = blacksmithGui.getItem(25);
                        if (slot25 != null) {
                            ItemStack returnitem = slot25.clone();
                            DotS.giveItemSafe(player.getUniqueId(), returnitem);
                            String[] lore2 = new String[]{manager.getColored("&7Click an item in your inventory"), manager.getColored("&7to select it for upgrading.")};
                            ItemStack empty2 = ItemStackBuilder.of(Material.STONE_PICKAXE).damageValue(1).name(manager.getColored("&aUpgrade Item")).lore(lore2).flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
                            blacksmithGui.setItem(43, empty2);
                            blacksmithGui.setItem(25, new ItemStack(Material.AIR));
                        }
                    } else if (event.getRawSlot() == 43) {
                        Inventory blacksmithGui = event.getView().getTopInventory();
                        ItemStack slot43 = blacksmithGui.getItem(43);
                        String[] lore2 = new String[]{manager.getColored("&7Click an item in your inventory"), manager.getColored("&7to select it for upgrading.")};
                        ItemStack empty2 = ItemStackBuilder.of(Material.STONE_PICKAXE).damageValue(1).name(manager.getColored("&aUpgrade Item")).lore(lore2).flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
                        if (!slot43.equals(empty2)) {
                            if (slot43.getItemMeta().getLore().contains(manager.getColored("&cNot enough &eGold&c!"))) {
                                manager.send(player, "&cYou do not have enough &eGold &cto upgrade that item.");
                                ((Player) player).playSound(player.getLocation(), "dots.e.click_1", 10, 1);
                                return;
                            }
                            int cooldownTime = 1;
                            if (cooldowns.containsKey(player.getName())) {
                                long secondsLeft = ((cooldowns.get(player.getName())/1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                                if (secondsLeft > 0) {
                                    return;
                                }
                            }
                            cooldowns.put(player.getName(), System.currentTimeMillis());
                            ItemStack item = blacksmithGui.getItem(25).clone();
                            Integer upgradelevelmoney = 0;
                            Integer upgradelevel = 1;
                            Integer maxupgradelevel;
                            String DisplayName;
                            Boolean firstTime = false;
                            NBTItem nbtitem = new NBTItem(item);
                            if (nbtitem.hasKey("UpgradeLevel")) {
                                upgradelevel = nbtitem.getInteger("UpgradeLevel");
                                upgradelevel += 1;
                                maxupgradelevel = nbtitem.getInteger("MaxUpgradeLevel");
                                upgradelevelmoney = nbtitem.getInteger("UpgradeLevel");
                                DisplayName = nbtitem.getString("ITEM_NAME");
                                DisplayName = manager.getColored(DisplayName);
                            } else {
                                nbtitem.setString("ITEM_NAME", manager.getColored(item.getItemMeta().getDisplayName()));
                                nbtitem.setInteger("UpgradeLevel", 1);
                                Integer randommax = AsthCore.getRandomInteger(2, 10);
                                nbtitem.setInteger("MaxUpgradeLevel", randommax);
                                maxupgradelevel = nbtitem.getInteger("MaxUpgradeLevel");
                                DisplayName = manager.getColored(item.getItemMeta().getDisplayName());
                                firstTime = true;
                            }
                            Integer cost = manager.getUpgradeItemValue(item);
                            cost = manager.getUpgradeCost(upgradelevelmoney, cost);
                            Set<String> keys = nbtitem.getKeys();
                            List<String> lore = item.getItemMeta().getLore();
                            ArrayList<String> tags = new ArrayList<>();
                            for (String key : keys) {
                                if (manager.getMmoitemslores().containsKey(key)) {
                                    tags.add(key);
                                }
                            }
                            DecimalFormat df = new DecimalFormat("0.00");
                            Double value;
                            Double newvalue;
                            String format;
                            String attributedisplay;
                            for (String attribute : tags) {
                                if (firstTime) {
                                    format = df.format(nbtitem.getDouble(attribute));
                                    nbtitem.setDouble("BASE_" + attribute, nbtitem.getDouble(attribute));
                                } else {
                                    format = df.format(nbtitem.getDouble("BASE_" + attribute));
                                }
                                value = Double.parseDouble(format);
                                newvalue = value*(Math.pow(1.07, upgradelevel));
                                format = df.format(newvalue);
                                newvalue = Double.parseDouble(format);
                                attributedisplay = manager.getMmoitemslores().get(attribute);
                                if (!newvalue.equals(value)) {
                                    nbtitem.setDouble(attribute, newvalue);
                                    if (attributedisplay.contains("%")) {
                                        attributedisplay = attributedisplay.substring(1);
                                        for (int i = 0; i < lore.size(); i++) {
                                            if (lore.get(i).contains(manager.getColored(attributedisplay))) {
                                                lore.set(i, manager.getColored(attributedisplay + newvalue + "%"));
                                            }
                                        }
                                    } else {
                                        for (int i = 0; i < lore.size(); i++) {
                                            if (lore.get(i).contains(manager.getColored(attributedisplay))) {
                                                lore.set(i, manager.getColored(attributedisplay + newvalue));
                                            }
                                        }
                                    }
                                }
                            }
                            if (nbtitem.hasKey("MMOITEMS_ABILITIES")) {
                                JsonArray jsonArray = new JsonParser().parse(nbtitem.getString("MMOITEMS_ABILITIES")).getAsJsonArray();
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    AbilityData data = new AbilityData(jsonArray.get(i).getAsJsonObject());
                                    String ability = data.getAbility().getName();
                                    List<String> modifiers = new ArrayList<>(Arrays.asList("damage", "heal" , "drain", "extra-damage", "duration", "amplifier", "radius"));
                                    for (String mod : modifiers) {
                                        if (data.hasModifier(mod)) {
                                            if (data.getModifier(mod) != 0) {
                                                if (firstTime) {
                                                    format = df.format(data.getModifier(mod));
                                                    nbtitem.setDouble("BASE_" + ability + "_" + mod, data.getModifier(mod));
                                                } else {
                                                    format = df.format(nbtitem.getDouble("BASE_" + ability + "_" + mod));
                                                }
                                                Double modifier = Double.parseDouble(format);
                                                Double finalmodifier = modifier*(Math.pow(1.07, upgradelevel));
                                                format = df.format(finalmodifier);
                                                finalmodifier = Double.parseDouble(format);
                                                data.setModifier(mod, finalmodifier);
                                                mod = mod.replaceAll("-", " ");
                                                mod = manager.capitalizeWords(mod);
                                                for(int line = 0; line < lore.size(); line++) {
                                                    if (lore.get(line).contains(ability)) {
                                                        for(int line2 = line; line2 < lore.size(); line2++) {
                                                            if (lore.get(line2).contains(mod) && !lore.get(line2).contains(ability)) {
                                                                lore.set(line2, manager.getColored("&a" + mod + ": " + finalmodifier));
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    jsonArray.set(i, data.toJson());
                                }
                                nbtitem.setString("MMOITEMS_ABILITIES", jsonArray.toString());
                            }
                            nbtitem.setInteger("UpgradeLevel", upgradelevel);
                            item = nbtitem.getItem();
                            ItemMeta metalore = item.getItemMeta();
                            metalore.setLore(lore);
                            metalore.setDisplayName(manager.getColored(DisplayName + " &7Tier " + upgradelevel + "/" + maxupgradelevel));
                            item.setItemMeta(metalore);
                            blacksmithGui.setItem(25, item);
                            ((Player) player).playSound(player.getLocation(), "dots.e.blacksmith", 10, 1);
                            DungeonEditor.takeGold(player.getUniqueId(), cost);
                            manager.send(player, "&aYou upgraded your " + DisplayName + " &ato " + "&7Tier " + upgradelevel + "/" + maxupgradelevel + "&a!");
                            if (upgradelevel >= maxupgradelevel) {
                                ItemStack air = new ItemStack(Material.AIR);
                                DotS.giveItemSafe(player.getUniqueId(), item);
                                blacksmithGui.setItem(25, air);
                                String[] lore5 = new String[]{manager.getColored("&7Click an item in your inventory"), manager.getColored("&7to select it for upgrading.")};
                                ItemStack empty = ItemStackBuilder.of(Material.STONE_PICKAXE).damageValue(1).name(manager.getColored("&aUpgrade Item")).lore(lore5).flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
                                blacksmithGui.setItem(43, empty);
                            } else {
                                blacksmithGui.setItem(43, manager.getBlacksmithUpgradeDisplay(item, (Player) player));
                            }
                        }
                    }
                }
            } else {
                if (event.getView().getTitle().equalsIgnoreCase("Blacksmith")) {
                    if (event.getRawSlot() == 54 || event.getRawSlot() == 55 || event.getRawSlot() == 56 || event.getRawSlot() == 63 || event.getRawSlot() == 64 || event.getRawSlot() == 65 || event.getRawSlot() == 72 || event.getRawSlot() == 73 || event.getRawSlot() == 74) {
                        event.setCancelled(true);
                        if (event.getCurrentItem().getType().equals(Material.AIR)) {
                            return;
                        }
                        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                        if (lore != null && lore.size() > 0) {
                            if (lore.get(0).equalsIgnoreCase(manager.getColored("&cSword")) || lore.get(0).equalsIgnoreCase(manager.getColored("&cStaff")) || lore.get(0).equalsIgnoreCase(manager.getColored("&cArmor")) || lore.get(0).equalsIgnoreCase(manager.getColored("&cShield"))) {
                                if (manager.getUpgradeItemValue(event.getCurrentItem()).equals(-1)) {
                                    manager.send(player, "&cThat item is bugged and cannot be upgraded.");
                                    return;
                                }
                                if (manager.getUpgradeItemValue(event.getCurrentItem()).equals(0)) {
                                    manager.send(player, "&cThat item cannot be upgraded.");
                                    return;
                                }
                                NBTItem nbtitem = new NBTItem(event.getCurrentItem());
                                if (nbtitem.hasKey("UpgradeLevel")) {
                                    Integer upgradelevel = nbtitem.getInteger("UpgradeLevel");
                                    Integer maxlevel = nbtitem.getInteger("MaxUpgradeLevel");
                                    if (upgradelevel >= maxlevel) {
                                        manager.send(player, "&cThat item is already fully upgraded.");
                                        return;
                                    }
                                }
                                Inventory blacksmithGui = event.getView().getTopInventory();
                                ItemStack slot25 = blacksmithGui.getItem(25);
                                if (slot25 == null) {
                                    blacksmithGui.setItem(25, event.getCurrentItem().clone());
                                    ItemStack upgradedisplay = manager.getBlacksmithUpgradeDisplay(event.getCurrentItem(), (Player) player);
                                    blacksmithGui.setItem(43, upgradedisplay);
                                    event.setCurrentItem(new ItemStack(Material.AIR));
                                } else {
                                    ItemStack returnitem = slot25.clone();
                                    ItemStack currentitem = event.getCurrentItem().clone();
                                    blacksmithGui.setItem(25, currentitem);
                                    event.setCurrentItem(returnitem);
                                    ItemStack upgradedisplay = manager.getBlacksmithUpgradeDisplay(currentitem, (Player) player);
                                    blacksmithGui.setItem(43, upgradedisplay);
                                }
                            } else {
                                manager.send(player, "&cThat item cannot be upgraded.");
                            }
                        }
                    }
                }
            }
        }
    }
}
