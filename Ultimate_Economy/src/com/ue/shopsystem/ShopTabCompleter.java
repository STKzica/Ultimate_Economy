package com.ue.shopsystem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ShopTabCompleter implements TabCompleter{
	
	private FileConfiguration config;
	
	public ShopTabCompleter(FileConfiguration config) {
		this.config = config;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if (command.getName().equals("adminshop") || command.getName().equals("playershop")) {
			if (args[0].equals("")) {
				list.add("create");
				list.add("delete");
				list.add("move");
				list.add("editShop");
				list.add("addItem");
				list.add("removeItem");
				list.add("editItem");
				list.add("addEnchantedItem");
				list.add("addPotion");
				if (command.getName().equals("adminshop")) {
					list.add("addSpawner");
					list.add("removeSpawner");
				} else {
					list.add("changeOwner");
				}
			} else if (args[0].equals("delete") || args[0].equals("addItem") || args[0].equals("removeItem")
					|| args[0].equals("addSpawner") || args[0].equals("removeSpawner") || args[0].equals("editShop")
					|| args[0].equals("addEnchantedItem") || args[0].equals("addPotion")
					|| args[0].equals("changeOwner")) {
				if (args.length == 2) {
					if (command.getName().equals("adminshop")) {
						list = getAdminShopList(config, args[1]);
					} else {
						list = getPlayerShopList(config, args[1], sender.getName());
					}
				} else if (args.length == 3) {
					if (args[0].equals("addItem") || args[0].equals("addEnchantedItem")) {
						list = getMaterialList(args[2]);
					} else if (args[0].equals("addPotion")) {
						if (args[2].equals("")) {
							for (PotionType pType : PotionType.values()) {
								list.add(pType.name().toLowerCase());
							}
						} else {
							for (PotionType pType : PotionType.values()) {
								if (pType.name().toLowerCase().contains(args[2])) {
									list.add(pType.name().toLowerCase());
								}
							}
						}
					} else if (args[0].equals("addSpawner")) {
						list = getEntityList(args[2]);
					}
				} else if (args.length == 4 && args[0].equals("addPotion")) {
					if (args[3].equals("")) {
						for (PotionEffectType peType : PotionEffectType.values()) {
							if (peType != null) {
								list.add(peType.getName().toLowerCase());
							}
						}
					} else {
						for (PotionEffectType peType : PotionEffectType.values()) {
							if (peType != null && peType.getName().toLowerCase().contains(args[3])) {
								list.add(peType.getName().toLowerCase());
							}
						}
					}
				} else if (args.length == 5 && args[0].equals("addPotion")) {
					if (args[4].equals("")) {
						list.add("extended");
						list.add("upgraded");
						list.add("none");
					} else {
						if ("extended".contains(args[4])) {
							list.add("extended");
						}
						if ("upgraded".contains(args[4])) {
							list.add("upgraded");
						}
						if ("none".contains(args[4])) {
							list.add("none");
						}
					}
				} else if (args[0].equals("addEnchantedItem") && args.length >= 7 && (args.length % 2) == 0) {
					if (args[args.length - 1].equals("")) {
						for (Enchantment enchantment : Enchantment.values()) {
							if (enchantment != null) {
								list.add(enchantment.getKey().getKey());
							}
						}
					} else {
						for (Enchantment enchantment : Enchantment.values()) {
							if (enchantment != null && enchantment.getKey().getKey().contains(args[args.length - 1])) {
								list.add(enchantment.getKey().getKey().toLowerCase());
							}
						}
					}
				}
			} else if (args.length == 1) {
				if ("create".contains(args[0])) {
					list.add("create");
				}
				if ("delete".contains(args[0])) {
					list.add("delete");
				}
				if ("move".contains(args[0])) {
					list.add("move");
				}
				if ("addItem".contains(args[0])) {
					list.add("addItem");
				}
				if ("removeItem".contains(args[0])) {
					list.add("removeItem");
				}
				if ("editItem".contains(args[0])) {
					list.add("editItem");
				}
				if ("editShop".contains(args[0])) {
					list.add("editShop");
				}
				if ("addEnchantedItem".contains(args[0])) {
					list.add("addEnchantedItem");
				}
				if ("addPotion".contains(args[0])) {
					list.add("addPotion");
				}
				if (command.getName().equals("adminshop")) {
					if ("addSpawner".contains(args[0])) {
						list.add("addSpawner");
					}
					if ("removeSpawner".contains(args[0])) {
						list.add("removeSpawner");
					}
				} else {
					if ("changeOwner".contains(args[0])) {
						list.add("changeOwner");
					}
				}
			}
		}
		return list;
	}
	
	private List<String> getPlayerShopList(FileConfiguration config, String arg, String playerName) {
		List<String> temp = config.getStringList("PlayerShopNames");
		List<String> list = new ArrayList<>();
		if (arg.equals("")) {
			for (String shopName : temp) {
				if (shopName.substring(shopName.indexOf("_") + 1).equals(playerName)) {
					list.add(shopName.substring(0, shopName.indexOf("_")));
				}
			}
		} else {
			for (String shopName : temp) {
				if (shopName.substring(0, shopName.indexOf("_")).contains(arg)
						&& shopName.substring(shopName.indexOf("_") + 1).equals(playerName)) {
					list.add(shopName.substring(0, shopName.indexOf("_")));
				}
			}
		}
		return list;
	}

	private List<String> getAdminShopList(FileConfiguration config,  String arg) {
		List<String> temp = config.getStringList("ShopNames");
		List<String> list = new ArrayList<>();
		if (arg.equals("")) {
			list = temp;
		} else {
			for (String shopName : temp) {
				if (shopName.contains(arg)) {
					list.add(shopName);
				}
			}
		}
		return list;
	}
	
	public static List<String> getMaterialList(String arg) {
		Material[] materials = Material.values();
		List<String> list = new ArrayList<>();
		if (arg.equals("")) {
			for (Material material : materials) {
				list.add(material.name().toLowerCase());
			}
		} else {
			for (Material material : materials) {
				if (material.name().toLowerCase().contains(arg)) {
					list.add(material.name().toLowerCase());
				}
			}
		}
		return list;
	}

	public static List<String> getEntityList(String arg) {
		List<String> list = new ArrayList<>();
		EntityType[] entityTypes = EntityType.values();
		if (arg.equals("")) {
			for (EntityType entityname : entityTypes) {
				list.add(entityname.name().toLowerCase());
			}
		} else {
			for (EntityType entityname : entityTypes) {
				if (entityname.name().toLowerCase().contains(arg)) {
					list.add(entityname.name().toLowerCase());
				}
			}
		}
		return list;
	}
}