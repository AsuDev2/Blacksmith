package asudev.blacksmith;

import com.asthereon.dots.DotS;
import com.asthereon.dungeoneditor.DungeonEditor;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.item.ItemStackBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.manager.ItemManager;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Manager {

    private static Manager instance = new Manager();
    private JavaPlugin plugin;
    private MMOItems mmoitems = MMOItems.plugin;

    private static HashMap<Integer, HashMap<String, HashMap<Integer, ItemStack>>> mmitems = new HashMap<>();

    public static HashMap<String, String> mmoitemslores = new HashMap<>();
    static {
        mmoitemslores.put("MMOITEMS_ATTACK_DAMAGE", "&7➸ Attack Damage: &f");
        mmoitemslores.put("MMOITEMS_CRITICAL_STRIKE_CHANCE", "%&7■ Crit Strike Chance: &f");
        mmoitemslores.put("MMOITEMS_CRITICAL_STRIKE_POWER", "%&7■ Crit Strike Power: &f");
        mmoitemslores.put("MMOITEMS_RANGE", "&7■ Range: &f");
        mmoitemslores.put("MMOITEMS_PVE_DAMAGE", "%&7■ PvE Damage: &f");
        mmoitemslores.put("MMOITEMS_PVP_DAMAGE", "%&7■ PvP Damage: &f");
        mmoitemslores.put("MMOITEMS_MAGIC_DAMAGE", "%&7■ Magic Damage: &f");
        mmoitemslores.put("MMOITEMS_WEAPON_DAMAGE", "%&7■ Weapon Damage: &f");
        mmoitemslores.put("MMOITEMS_UNDEAD_DAMAGE", "%&7■ Undead Damage: &f");
        mmoitemslores.put("MMOITEMS_SKILL_DAMAGE", "%&7■ Skill Damage: &f");
        mmoitemslores.put("MMOITEMS_PHYSICAL_DAMAGE", "%&7■ Physical Damage: &f");
        mmoitemslores.put("MMOITEMS_PROJECTILE_DAMAGE", "%&7■ Projectile Damage: &f");
        mmoitemslores.put("MMOITEMS_BLOCK_POWER", "%&7■ Block Power: &f");
        mmoitemslores.put("MMOITEMS_BLOCK_RATING", "%&7■ Block Rating: &f");
        mmoitemslores.put("MMOITEMS_DODGE_RATING", "%&7■ Dodge Rating: &f");
        mmoitemslores.put("MMOITEMS_PARRY_RATING", "%&7■ Parry Rating: &f");
        mmoitemslores.put("MMOITEMS_ARMOR", "&7✠ Armor: &f");
        mmoitemslores.put("MMOITEMS_ARMOR_TOUGHNESS", "&7✠ Armor Toughness: &f");
        mmoitemslores.put("MMOITEMS_KNOCKBACK_RESISTANCE", "%&7✠ Knockback Resistance: &f");
        mmoitemslores.put("MMOITEMS_MAX_HEALTH", "&c❤ Health: ");
        mmoitemslores.put("MMOITEMS_REGENERATION", "%&7■ Health Regeneration: &f");
        mmoitemslores.put("MMOITEMS_DAMAGE_REDUCTION", "%&7■ Damage Reduction: &f");
        mmoitemslores.put("MMOITEMS_FIRE_DAMAGE_REDUCTION", "%&7■ Fire Damage Reduction: &f");
        mmoitemslores.put("MMOITEMS_PROJECTILE_DAMAGE_REDUCTION", "%&7■ Projectile Damage Reduction: &f");
        mmoitemslores.put("MMOITEMS_MAGIC_DAMAGE_REDUCTION", "%&7■ Magic Damage Reduction: &f");
        mmoitemslores.put("MMOITEMS_PHYSICAL_DAMAGE_REDUCTION", "%&7■ Physical Damage Reduction: &f");
        mmoitemslores.put("MMOITEMS_MAX_MANA", "&7■ Max Mana: &f");
        mmoitemslores.put("MMOITEMS_MANA_REGENERATION", "&7■ Mana Regeneration: &f");
    }

    public static Manager getInstance() {
        return instance;
    }

    public void init(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public HashMap<String, String> getMmoitemslores() {
        return mmoitemslores;
    }

    // Get tier from level
    public Integer getTier(Integer level) {
        Double divider = level.doubleValue() / 3;
        Double divider2 = Math.ceil(divider);
        Integer tier = divider2.intValue();
        return tier;
    }

    // get cost from tier
    public Double getCost(Integer tier) {
        File config = new File(plugin.getDataFolder(), "forge.yml");
        FileConfiguration main = new YamlConfiguration();
        try {
            main.load(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        List<Integer> values = main.getIntegerList("blacksmithvalue");
        Integer level = (tier * 3);
        Integer cost = values.get(level);
        return cost.doubleValue();
    }

    // Initialize items from config + tags
    public void setupItems() {
        mmitems.clear();
        File config = new File(plugin.getDataFolder(), "main.yml");
        FileConfiguration main = new YamlConfiguration();
        try {
            main.load(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Integer tier = 1;
        Integer level = null;
        String rarity, type = null;
        ItemStack testitem, item;
        MMOItem mmoitem;
        Integer counter = 0;
        ItemManager itemManager = mmoitems.getItems();
        for (String key : main.getKeys(false)) {
            level = main.getInt(key + ".Level");
            rarity = main.getString(key + ".Rarity");
            type = main.getString(key + ".Type");
            try {
                mmoitem = itemManager.getMMOItem(mmoitems.getTypes().get(type), key);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(getColored("&c&lBlacksmith Error! &7The mmoitem &4" + key + " &7was not able to be connected."));
                continue;
            }
            mmoitem = itemManager.getMMOItem(mmoitems.getTypes().get(type), key);
            MMOItemBuilder builder;
            try {
                builder = new MMOItemBuilder(mmoitem);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(getColored("&c&lBlacksmith Error! &7The mmoitem &4" + key + " &7was not able to be connected."));
                continue;
            }
            item = builder.build();
            tier = getTier(level);
            if (rarity != null) {
                try {
                    if (mmitems.get(tier) == null) {
                        mmitems.put(tier, new HashMap<String, HashMap<Integer, ItemStack>>());
                    }
                    if (mmitems.get(tier).get(rarity) == null) {
                        mmitems.get(tier).put(rarity, new HashMap<Integer, ItemStack>());
                    }
                    mmitems.get(tier).get(rarity).put(counter, item);
                    counter++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Open blacksmith gui for player
    public void openBlacksmithGui(Player player) {
        Inventory blacksmith = Bukkit.getServer().createInventory(null, 54, "Blacksmith");
        Double dmoney = DungeonEditor.getBalance(player.getUniqueId());
        Integer money = dmoney.intValue();
        Integer tier = getTier(DotS.getPlayerLevel(player.getUniqueId()));
        Integer cost = getCost(tier).intValue();
        String canAfford;
        if (money < cost) {
            canAfford = getColored("&cNot enough &eGold&c!");
        } else {
            canAfford = getColored("&aClick to forge!");
        }
        //ItemStack ingot = ItemStackBuilder.of(Material.IRON_INGOT).name("&cBlacksmith").lore("&7Click to purchase anything from my store.").build();
        String[] lore = new String[]{getColored("&7 "), getColored("&7Trade &eGold &7for the Blacksmith to"), getColored("&7forge you a random piece of gear"), getColored("&7appropriate to your level."), getColored("&7 "), getColored("&aCost: &e" + cost.toString()), getColored("&7 "), canAfford};
        String[] lore2 = new String[]{getColored("&7Click an item in your inventory"), getColored("&7to select it for upgrading.")};
        ItemStack maingui = ItemStackBuilder.of(Material.STONE_PICKAXE).damageValue(89).name(getColored("&7")).flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
        ItemStack empty = ItemStackBuilder.of(Material.STONE_PICKAXE).damageValue(1).name(getColored("&aForge Random Item")).lore(lore).flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
        ItemStack empty2 = ItemStackBuilder.of(Material.STONE_PICKAXE).damageValue(1).name(getColored("&aUpgrade Item")).lore(lore2).flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
        setInventorySlots(blacksmith, new Integer[]{19, 20, 21, 28, 29, 30, 37, 38, 39, 0, 43}, new ItemStack[]{empty, empty, empty, empty, empty, empty, empty, empty, empty, maingui, empty2});
        player.openInventory(blacksmith);
    }

    // Get all items from tier/rarity
    public ArrayList<ItemStack> getMMOItems(Integer tier, String rarity) {
        if (mmitems.get(tier) == null) {
            ItemStack failitem = ItemStackBuilder.of(Material.STONE).name("FAIL ITEM").build();
            ArrayList<ItemStack> failarray = new ArrayList<ItemStack>();
            failarray.add(failitem);
            return failarray;
        }
        if (mmitems.get(tier).get(rarity) == null) {
            ItemStack failitem = ItemStackBuilder.of(Material.STONE).name("FAIL ITEM").build();
            ArrayList<ItemStack> failarray = new ArrayList<ItemStack>();
            failarray.add(failitem);
            return failarray;
        }
        Collection<ItemStack> values = mmitems.get(tier).get(rarity).values();
        ArrayList<ItemStack> mmoitems = new ArrayList<ItemStack>(values);
        return mmoitems;
    }

    //get random itemstack from arraylist
    public ItemStack getRandomMMOItem(ArrayList<ItemStack> items) {
        Random randomGenerator = new Random();
        Integer index = randomGenerator.nextInt(items.size());
        return items.get(index);
    }

    // get tier based on chance
    public String getRandomRarity() {
        Random randomNumber = new Random();
        Integer randomInt = randomNumber.nextInt((100 - 1) + 1);
        if (randomInt.equals(1)) {
            return "Mythical";
        } else if (randomInt >= 2 && randomInt <= 8) {
            return "Legendary";
        } else if (randomInt >= 9 && randomInt <= 17) {
            return "Epic";
        } else if (randomInt >= 18 && randomInt <= 35) {
            return "Rare";
        } else if (randomInt >= 36 && randomInt <= 64) {
            return "Uncommon";
        } else if (randomInt >= 65 && randomInt <= 100) {
            return "Common";
        } else {
            return "Common";
        }
    }

    public ItemStack getItemRarityItem(String rarity) {
        switch (rarity) {
            case "Common":
                return new ItemStack(Material.AIR);
            case "Uncommon":
                return ItemStackBuilder.of(Material.STONE_PICKAXE).name("&7").damageValue(74).flag(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
            case "Rare":
                return ItemStackBuilder.of(Material.STONE_PICKAXE).name("&7").damageValue(71).flag(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
            case "Epic":
                return ItemStackBuilder.of(Material.STONE_PICKAXE).name("&7").damageValue(72).flag(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
            case "Legendary":
                return ItemStackBuilder.of(Material.STONE_PICKAXE).name("&7").damageValue(73).flag(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
            case "Mythical":
                return ItemStackBuilder.of(Material.STONE_PICKAXE).name("&7").damageValue(75).flag(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
            default:
                return new ItemStack(Material.AIR);
        }
    }

    public String capitalizeWords(String str) {
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for (String w: words) {
            String first = w.substring(0, 1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterfirst + " ";
        }
        return capitalizeWord.trim();
    }

    public Integer removeNonNumbers(String s) {
        String newString = s.replaceAll("[^0-9]", "");
        Integer newint;
        try {
            newint = Integer.parseInt(newString);
        } catch (NumberFormatException e) {
            newint = -1;
        }
        return newint;
    }

    public Integer getUpgradeItemValue(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        Integer itemvalue;
        for (String line : lore) {
            if (line.contains("Sell Value")) {
                line = ChatColor.stripColor(getColored(line));
                itemvalue = removeNonNumbers(line);
                if (itemvalue == -1) {
                    return -1;
                }
                Double val = itemvalue * 2.5;
                itemvalue = val.intValue() / 2;
                return itemvalue;
            }
        }
        return 0;
    }

    public Integer getUpgradeCost(Integer upgradelevel, Integer value) {
        Double upgradecost = value*(Math.pow(1.2, upgradelevel));
        return upgradecost.intValue();
    }

    public ItemStack getBlacksmithUpgradeDisplay(ItemStack item, Player player) {
        Integer upgradelevelmoney = 0;
        Integer upgradelevel = 1;
        Boolean firstTime = false;
        NBTItem nbtitem = new NBTItem(item);
        if (nbtitem.hasKey("UpgradeLevel")) {
            upgradelevel = nbtitem.getInteger("UpgradeLevel");
            upgradelevel += 1;
            upgradelevelmoney = nbtitem.getInteger("UpgradeLevel");
        } else {
            firstTime = true;
        }
        Set<String> keys = nbtitem.getKeys();
        List<String> lore = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        for (String key : keys) {
            if (mmoitemslores.containsKey(key)) {
                tags.add(key);
            }
        }
        DecimalFormat df = new DecimalFormat("0.00");
        Double value;
        Double newvalue;
        Double basevalue;
        String format;
        String attributedisplay;
        lore.add(getColored("&7"));
        if (firstTime) {
            lore.add(getColored("&aUpgrade: &fTier 0/? &a→ Tier 1/?"));
        } else {
            lore.add(getColored("&aUpgrade: &fTier " + (upgradelevel-1) + "/" + nbtitem.getInteger("MaxUpgradeLevel") + " &a→ Tier " + upgradelevel + "/" + nbtitem.getInteger("MaxUpgradeLevel")));
        }
        for (String attribute : tags) {
            if (firstTime) {
                format = df.format(nbtitem.getDouble(attribute));
            } else {
                format = df.format(nbtitem.getDouble("BASE_" + attribute));
            }
            basevalue = Double.parseDouble(format);
            format = df.format(nbtitem.getDouble(attribute));
            value = Double.parseDouble(format);
            newvalue = basevalue*(Math.pow(1.07, upgradelevel));
            format = df.format(newvalue);
            newvalue = Double.parseDouble(format);
            attributedisplay = mmoitemslores.get(attribute);
            if (!newvalue.equals(value)) {
                if (attributedisplay.contains("%")) {
                    attributedisplay = attributedisplay.substring(1, attributedisplay.length());
                    lore.add(getColored(attributedisplay + value + "% &a→ " + newvalue + "%"));
                } else {
                    lore.add(getColored(attributedisplay + value + " &a→ " + newvalue));
                }
            }
        }
        Boolean extraSpace = false;
        Boolean extraSpace2 = false;
        if (nbtitem.hasKey("MMOITEMS_ABILITIES")) {
            JsonArray jsonArray = new JsonParser().parse(nbtitem.getString("MMOITEMS_ABILITIES")).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                AbilityData data = new AbilityData(jsonArray.get(i).getAsJsonObject());
                String ability = data.getAbility().getName();
                List<String> modifiers = new ArrayList<>(Arrays.asList("damage", "heal" , "drain", "extra-damage", "duration", "amplifier", "radius"));
                for (String mod : modifiers) {
                    if (data.hasModifier(mod)) {
                        if (data.getModifier(mod) != 0) {
                            if (extraSpace2) {
                                lore.add(getColored("&7 "));
                                extraSpace2 = false;
                            }
                            if (!extraSpace) {
                                extraSpace = true;
                                lore.add(getColored("&7 "));
                            }
                            if (firstTime) {
                                format = df.format(data.getModifier(mod));
                            } else {
                                format = df.format(nbtitem.getDouble("BASE_" + ability + "_" + mod));
                            }
                            Double modifierbasevalue = Double.parseDouble(format);
                            format = df.format(data.getModifier(mod));
                            Double modifier = Double.parseDouble(format);
                            Double finalmodifier = modifierbasevalue*(Math.pow(1.07, upgradelevel));
                            format = df.format(finalmodifier);
                            finalmodifier = Double.parseDouble(format);
                            data.setModifier(mod, finalmodifier);
                            mod = mod.replaceAll("-", " ");
                            mod = capitalizeWords(mod);
                            if (!lore.contains("&e" + data.getAbility().getName())) {
                                lore.add("&e" + ability);
                                lore.add(" &7" + mod + ": &f" + modifier + " &a→ " + finalmodifier);
                            } else {
                                lore.add(" &7" + mod + ": &f" + modifier + " &a→ " + finalmodifier);
                            }
                        }
                    }
                }
                extraSpace2 = true;
                jsonArray.set(i, data.toJson());
            }
        }
        Integer cost = getUpgradeItemValue(item);
        cost = getUpgradeCost(upgradelevelmoney, cost);
        lore.add(getColored("&7 "));
        lore.add(getColored("&aCost: &e" + cost));
        lore.add(getColored("&7"));
        Double dmoney = DungeonEditor.getBalance(player.getUniqueId());
        Integer money = dmoney.intValue();
        String canAfford;
        if (money < cost) {
            canAfford = getColored("&cNot enough &eGold&c!");
        } else {
            canAfford = getColored("&aClick to upgrade!");
        }
        lore.add(getColored(canAfford));
        ItemStack displayitem = ItemStackBuilder.of(Material.STONE_PICKAXE).name("&aUpgrade Item").lore(lore).damageValue(1).flag(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS).breakable(false).build();
        return displayitem;
    }

    // Better send
    public void send(CommandSender sender, String s) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
    }

    public void send(Player player, String s) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
    }

    public Inventory setInventorySlots(Inventory inventory, Integer[] slots, ItemStack[] items) {
        Integer counter = 0;
        for (Integer slot : slots) {
            inventory.setItem(slot, items[counter]);
            counter++;
        }
        return inventory;
    }

    public String getColored(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
