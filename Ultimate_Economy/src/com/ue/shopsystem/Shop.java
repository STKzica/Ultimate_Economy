package com.ue.shopsystem;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.ue.exceptions.ShopSystemException;
import com.ue.exceptions.TownSystemException;
import com.ue.townsystem.Town;
import com.ue.townsystem.TownWorld;

public abstract class Shop {

	// minecraft skull texture links
	private static final String PLUS = "http://textures.minecraft.net/texture/9a2d891c6ae9f6baa040d736ab84d48344bb6b70d7f1a280dd12cbac4d777";
	private static final String MINUS = "http://textures.minecraft.net/texture/935e4e26eafc11b52c11668e1d6634e7d1d0d21c411cb085f9394268eb4cdfba";
	private static final String ONE = "http://textures.minecraft.net/texture/d2a6f0e84daefc8b21aa99415b16ed5fdaa6d8dc0c3cd591f49ca832b575";
	private static final String TEN = "http://textures.minecraft.net/texture/b0cf9794fbc089dab037141f67875ab37fadd12f3b92dba7dd2288f1e98836";
	private static final String SLOTFILLED = "http://textures.minecraft.net/texture/9e42f682e430b55b61204a6f8b76d5227d278ed9ec4d98bda4a7a4830a4b6";
	private static final String SLOTEMPTY = "http://textures.minecraft.net/texture/b55d5019c8d55bcb9dc3494ccc3419757f89c3384cf3c9abec3f18831f35b0";
	private static final String TWENTY = "http://textures.minecraft.net/texture/f7b29a1bb25b2ad8ff3a7a38228189c9461f457a4da98dae29384c5c25d85";
	private static final String BUY = "http://textures.minecraft.net/texture/e5da4847272582265bdaca367237c96122b139f4e597fbc6667d3fb75fea7cf6";
	private static final String SELL = "http://textures.minecraft.net/texture/abae89e92ac362635ba3e9fb7c12b7ddd9b38adb11df8aa1aff3e51ac428a4";
	
	private static final String K_ON = "http://textures.minecraft.net/texture/d42a4802b6b2deb49cfbb4b7e267e2f9ad45da24c73286f97bef91d21616496";
	private static final String K_OFF = "http://textures.minecraft.net/texture/e883b5beb4e601c3cbf50505c8bd552e81b996076312cffe27b3cc1a29e3";

	public Villager villager;
	public FileConfiguration config;
	public File file;
	public String name;
	public Location location;
	public Inventory inventory, editor, slotEditor;
	public int size;
	public List<String> itemNames;
	public boolean isPlayershop;
	public int slotEditorSlot;

	/**
	 * Constructor for creating a new shop.
	 * 
	 * @param dataFolder
	 * @param name
	 * @param spawnLocation
	 * @param size
	 * @param isPlayershop
	 */
	public Shop(File dataFolder, String name, Location spawnLocation, int size, boolean isPlayershop) {
		this.isPlayershop = isPlayershop;
		itemNames = new ArrayList<>();
		file = new File(dataFolder, name + ".yml");
		inventory = Bukkit.createInventory(null, size, name);
		slotEditor = Bukkit.createInventory(null, 27, name + "-SlotEditor");
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		config = YamlConfiguration.loadConfiguration(file);
		this.name = name;
		location = spawnLocation;
		this.size = size;
		config.set("ShopName", name);
		config.set("ShopSize", size);
		config.set("ShopLocation.x", location.getX());
		config.set("ShopLocation.y", location.getY());
		config.set("ShopLocation.z", location.getZ());
		config.set("ShopLocation.World", location.getWorld().getName());
		config.set("ShopItemList", itemNames);
		save();
		setupShopVillager();
	}

	/**
	 * Constructor for loading an existing shop.
	 * 
	 * @param dataFolder
	 * @param server
	 * @param name
	 * @param isPlayershop
	 */
	public Shop(File dataFolder, Server server, String name, boolean isPlayershop) {

		this.isPlayershop = isPlayershop;
		itemNames = new ArrayList<>();
		file = new File(dataFolder, name + ".yml");
		config = YamlConfiguration.loadConfiguration(file);
		size = config.getInt("ShopSize");
		inventory = Bukkit.createInventory(null, this.size, name);
		slotEditor = Bukkit.createInventory(null, 27, name + "-SlotEditor");
		this.name = name;
		itemNames = config.getStringList("ShopItemList");
		inventory = Bukkit.createInventory(villager, size, name);
		location = new Location(server.getWorld(config.getString("ShopLocation.World")),
				config.getDouble("ShopLocation.x"), config.getDouble("ShopLocation.y"),
				config.getDouble("ShopLocation.z"));
		setupShopVillager();
	}

	private void setupShopVillager() {
		location.getChunk().load();
		Collection<Entity> entitys = location.getWorld().getNearbyEntities(location, 10, 10, 10);
		for (Entity entity : entitys) {
			if (entity.getName().equals(name)) {
				entity.remove();
			}
		}
		villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		villager.setCustomName(name);
		villager.setCustomNameVisible(true);
		villager.setSilent(true);
		villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30000000, 30000000));
		villager.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30000000, 30000000));
		villager.setVillagerLevel(2);
		config = YamlConfiguration.loadConfiguration(file);
		if(config.isSet("Profession")) {
			villager.setProfession(Profession.valueOf(config.getString("Profession")));
		} else {
			villager.setProfession(Profession.NITWIT);
		}
		editor = Bukkit.createInventory(null, this.size, name + "-Editor");
		setupShopItems();
		setupEditor();
	}

	/**
	 * Only public for child PlayerShop. Not for commercial use.
	 */
	public void setupShopItems() {
		int slot = size - 1;
		ItemStack anvil = new ItemStack(Material.ANVIL);
		ItemMeta meta = anvil.getItemMeta();
		meta.setDisplayName("Info");
		anvil.setItemMeta(meta);
		addShopItemToInv(anvil, 1, slot, 0.0, 0.0);
		itemNames.add("ANVIL_0");
	}

	private void setupSlotEditor(int slot) {
		double buyPrice = 0;
		double sellPrice = 0;
		try {
			if(!slotIsEmpty(slot)) {
				ItemStack itemStack = new ItemStack(inventory.getItem(slot - 1));
				itemStack.setAmount(1);
				ItemMeta itemMeta = itemStack.getItemMeta();
				List<String> loreList = itemMeta.getLore();
				Iterator<String> loreIter = loreList.iterator();
				while(loreIter.hasNext()) {
					String lore = loreIter.next();
					if(lore.contains(" buy for ") || lore.contains(" sell for ")) {
						loreIter.remove();
					}
				}
				itemMeta.setLore(loreList);
				itemStack.setItemMeta(itemMeta);
				String itemString =  itemStack.toString();
				buyPrice = getItemBuyPrice(itemString);
				sellPrice = getItemSellPrice(itemString);
			}
		} catch (ShopSystemException e) {}
		
		List<String> listBuy = new ArrayList<String>();
		List<String> listSell = new ArrayList<String>();
		listBuy.add(ChatColor.GOLD + "Price: " + buyPrice);
		listSell.add(ChatColor.GOLD + "Price: " + sellPrice);
		ItemStack item = getSkull(PLUS, "plus");
		slotEditor.setItem(2, item);
		ItemMeta meta = item.getItemMeta();
		meta.setLore(listBuy);
		item.setItemMeta(meta);
		slotEditor.setItem(11, item);
		meta = item.getItemMeta();
		meta.setLore(listSell);
		item.setItemMeta(meta);
		slotEditor.setItem(20, item);
		
		item = getSkull(K_OFF, "factor off");
		slotEditor.setItem(12, item);
		slotEditor.setItem(21, item);
		
		item = getSkull(ONE, "one");
		slotEditor.setItem(4, item);
		meta = item.getItemMeta();
		meta.setLore(listBuy);
		item.setItemMeta(meta);
		slotEditor.setItem(13, item);
		meta = item.getItemMeta();
		meta.setLore(listSell);
		item.setItemMeta(meta);
		slotEditor.setItem(22, item);
		item = getSkull(TEN, "ten");
		slotEditor.setItem(5, item);
		meta = item.getItemMeta();
		meta.setLore(listBuy);
		item.setItemMeta(meta);
		slotEditor.setItem(14, item);
		meta = item.getItemMeta();
		meta.setLore(listSell);
		item.setItemMeta(meta);
		slotEditor.setItem(23, item);
		item = getSkull(TWENTY, "twenty");
		slotEditor.setItem(6, item);
		meta = item.getItemMeta();
		meta.setLore(listBuy);
		item.setItemMeta(meta);
		slotEditor.setItem(15, item);
		meta = item.getItemMeta();
		meta.setLore(listSell);
		item.setItemMeta(meta);
		slotEditor.setItem(24, item);
		item = new ItemStack(Material.GREEN_WOOL);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "save changes");
		item.setItemMeta(meta);
		slotEditor.setItem(8, item);
		item = new ItemStack(Material.RED_WOOL);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "exit without save");
		item.setItemMeta(meta);
		slotEditor.setItem(7, item);
		item = new ItemStack(Material.BARRIER);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "remove item");
		item.setItemMeta(meta);
		slotEditor.setItem(26, item);
		item = getSkull(BUY, "buyprice");
		meta = item.getItemMeta();
		meta.setLore(listBuy);
		item.setItemMeta(meta);
		slotEditor.setItem(9, item);
		item = getSkull(SELL, "sellprice");
		meta = item.getItemMeta();
		meta.setLore(listSell);
		item.setItemMeta(meta);
		slotEditor.setItem(18, item);
	}
	
	/**
	 * Change the profession of a shopvillager
	 * 
	 * @param profession
	 */
	public void changeProfession(Profession profession) {
		villager.setProfession(profession);
		config = YamlConfiguration.loadConfiguration(file);
		config.set("Profession", profession.name());
		save();
	}
	
	/**
	 * This method renames a shop.
	 * 
	 * @param newName
	 * @param player
	 * @throws ShopSystemException thrown, when a shop with this name already exists
	 */
	public abstract void renameShop(File dataFolder, String newName, String player) throws ShopSystemException;

	/**
	 * This method allows you to resize a shop.
	 * 
	 * @param newSize - size in slots
	 * @throws ShopSystemException 
	 */
	public void resize(int newSize) throws ShopSystemException {
		if(newSize % 9 != 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_INVENTORY_SIZE);
		} else {
			boolean possible = true;
			int diff = size - newSize;
			int temp = 1;
			if(isPlayershop) {
				temp = 2;
			}
			if(inventory.getSize() > newSize) {
				for(int i = 1;i<=diff;i++) {
					ItemStack stack = inventory.getItem(size-i-temp);
					if(stack != null) {
						possible = false;
					}
				}
			}
			if(possible) {
				config = YamlConfiguration.loadConfiguration(file);
				size = newSize;
				config.set("ShopSize", size);
				save();
				inventory = Bukkit.createInventory(null, size, name);
				reload();
				setupShopItems();
			} else {
				Bukkit.getLogger().info("test");
				throw new ShopSystemException(ShopSystemException.RESIZING_FAILED);
			}
		}
	}

	/**
	 * This method adds a item to this shop.
	 * 
	 * @param slot
	 * @param sellPrice
	 * @param buyPrice
	 * @param itemStack
	 * @throws ShopSystemException
	 */
	public void addItem(int slot, double sellPrice, double buyPrice, ItemStack itemStack) throws ShopSystemException {
		int amount = itemStack.getAmount();
		itemStack.setAmount(1);
		String itemString = itemStack.toString();
		if (itemStack.getType() == Material.SPAWNER && !isPlayershop) {
			ItemMeta meta = itemStack.getItemMeta();
			String entity = meta.getDisplayName();
			itemString = "SPAWNER_" + entity;
		}
		if (!slotIsEmpty(slot + 1)) {
			throw new ShopSystemException(ShopSystemException.INVENTORY_SLOT_OCCUPIED);
		} else if (sellPrice < 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_SELL_PRICE);
		} else if (buyPrice < 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_BUY_PRICE);
		} else if (buyPrice == 0 && sellPrice == 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_PRICES);
		} else if (itemNames.contains(itemString)) {
			throw new ShopSystemException(ShopSystemException.ITEM_ALREADY_EXISTS);
		} else {
			config = YamlConfiguration.loadConfiguration(file);
			//new savesystem
			itemNames.add(itemString);
			config.set("ShopItems." + itemString + ".Name", itemStack);
			config.set("ShopItems." + itemString + ".Amount", amount);
			config.set("ShopItems." + itemString + ".Slot", slot);
			config.set("ShopItems." + itemString + ".sellPrice", sellPrice);
			config.set("ShopItems." + itemString + ".buyPrice", buyPrice);
			config.set("ShopItems." + itemString + ".newSaveMethod", "true");
			if (isPlayershop) {
				config.set("ShopItems." + itemString + ".stock", 0);
			}
			config.set("ShopItemList", itemNames);
			save();
			addShopItemToInv(new ItemStack(itemStack), amount, slot, sellPrice, buyPrice);
		}
	}

	/**
	 * This method edits an existing item in this shop.
	 * 
	 * @param slot
	 * @param amount
	 * @param sellPrice
	 * @param buyPrice
	 * @return String
	 * @throws ShopSystemException
	 */
	public String editItem(int slot, String amount, String sellPrice, String buyPrice) throws ShopSystemException {
		if (slotIsEmpty(slot)) {
			throw new ShopSystemException(ShopSystemException.INVENTORY_SLOT_EMPTY);
		} else if (!amount.equals("none") && (Integer.valueOf(amount) <= 0 || Integer.valueOf(amount) > 64)) {
			throw new ShopSystemException(ShopSystemException.INVALID_AMOUNT);
		}
		if (!sellPrice.equals("none") && Double.valueOf(sellPrice) < 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_SELL_PRICE);
		} 
		if (!buyPrice.equals("none") && Double.valueOf(buyPrice) < 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_BUY_PRICE);
		} else if (!sellPrice.equals("none") && !buyPrice.equals("none") && Double.valueOf(sellPrice) == 0
				&& Double.valueOf(buyPrice) == 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_PRICES);
		} else {
			ItemStack itemStack = inventory.getItem(slot - 1);
			itemStack.setAmount(1);
			ItemMeta itemMeta = itemStack.getItemMeta();
			List<String> loreList = itemMeta.getLore();
			Iterator<String> loreIter = loreList.iterator();
			while(loreIter.hasNext()) {
				String lore = loreIter.next();
				if(lore.contains(" buy for ") || lore.contains(" sell for ")) {
					loreIter.remove();
				}
			}
			itemMeta.setLore(loreList);
			itemStack.setItemMeta(itemMeta);
			String itemString =  itemStack.toString();
			
			String message = ChatColor.GOLD + "Updated ";
			config = YamlConfiguration.loadConfiguration(file);
			if (!amount.equals("none")) {
				config.set("ShopItems." + itemString + ".Amount", Integer.valueOf(amount));
				message = message + ChatColor.GREEN + "amount ";
			}
			if (!sellPrice.equals("none")) {
				config.set("ShopItems." + itemString + ".sellPrice", Double.valueOf(sellPrice));
				message = message + ChatColor.GREEN + "sellPrice ";
			}
			if (!buyPrice.equals("none")) {
				config.set("ShopItems." + itemString + ".buyPrice", Double.valueOf(buyPrice));
				message = message + ChatColor.GREEN + "buyPrice ";
			}
			save();
			loadItem(itemString);
			message = message + ChatColor.GOLD + "for item " + ChatColor.GREEN + itemStack.getType().name().toLowerCase();
			return message;
		}
	}

	/**
	 * Loads a item by it's name. Not for commercial use.
	 * 
	 * @param itemString
	 * @throws ShopSystemException
	 */
	public void loadItem(String itemString) throws ShopSystemException {
		config = YamlConfiguration.loadConfiguration(file);
		//new loading method for new save method
		if(!itemString.equals("ANVIL_0") && !itemString.equals("CRAFTING_TABLE_0") && config.getString("ShopItems." + itemString + ".newSaveMethod") != null) {
			ItemStack itemStack = config.getItemStack("ShopItems." + itemString + ".Name");
			addShopItemToInv(itemStack, config.getInt("ShopItems." + itemString + ".Amount"),
					config.getInt("ShopItems." + itemString + ".Slot"),
					config.getDouble("ShopItems." + itemString + ".sellPrice"),
					config.getDouble("ShopItems." + itemString + ".buyPrice"));
		}
		//old loading method, only for old saved items, converts to the new save method
		else if(!itemString.equals("ANVIL_0") && !itemString.equals("CRAFTING_TABLE_0")){
			if (config.getString("ShopItems." + itemString + ".Name") != null) {
				String string = config.getString("ShopItems." + itemString + ".Name");
				List<String> lore = config.getStringList("ShopItems." + itemString + ".lore");
				int damage = config.getInt("ShopItems." + itemString + ".damage");
				String displayName = "default";
				if (string.contains("|")) {
					displayName = string.substring(0, string.indexOf("|"));
					string = string.substring(string.indexOf("|") + 1);
				}
				ItemStack itemStack = null;
				// enchanted
				if (string.contains("#Enchanted_")) {
					itemStack = new ItemStack(Material.valueOf(string.substring(0, string.indexOf("#")).toUpperCase()),
							config.getInt("ShopItems." + itemString + ".Amount"));
					addEnchantments(itemStack,
							new ArrayList<String>(config.getStringList("ShopItems." + itemString + ".enchantments")));
				}
				// potion
				else if (string.contains("potion:")) {
					String name = config.getString("ShopItems." + itemString + ".Name");
					itemStack = new ItemStack(Material.valueOf(string.substring(0, string.indexOf(":")).toUpperCase()),
							config.getInt("ShopItems." + itemString + ".Amount"));
					PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
					boolean extended = false;
					boolean upgraded = false;
					String property = name.substring(name.indexOf("#") + 1);
					if (property.equalsIgnoreCase("extended")) {
						extended = true;
					} else if (property.equalsIgnoreCase("upgraded")) {
						upgraded = true;
					}
					meta.setBasePotionData(new PotionData(
							PotionType.valueOf(name.substring(name.indexOf(":") + 1, name.indexOf("#")).toUpperCase()),
							extended, upgraded));
					itemStack.setItemMeta(meta);
				}
				// all other
				else if (!string.contains("SPAWNER")) {
					itemStack = new ItemStack(Material.getMaterial(string),
							config.getInt("ShopItems." + itemString + ".Amount"));
				}
				if (itemStack != null) {
					ItemMeta meta2 = itemStack.getItemMeta();
					if (lore != null && !lore.isEmpty()) {
						meta2.setLore(lore);

					}
					if (!displayName.equals("default")) {
						meta2.setDisplayName(displayName);
					}
					if (damage > 0) {
						Damageable damageMeta = (Damageable) meta2;
						damageMeta.setDamage(damage);
						meta2 = (ItemMeta) damageMeta;
					}
					itemStack.setItemMeta(meta2);
					
					//convert to new save method 
					itemStack.setAmount(config.getInt("ShopItems." + itemString + ".Amount"));
					double sell = config.getDouble("ShopItems." + itemString + ".sellPrice");
					double buy = config.getDouble("ShopItems." + itemString + ".buyPrice");
					int slot = config.getInt("ShopItems." + itemString + ".Slot");
					//remove old item
					config = YamlConfiguration.loadConfiguration(file);
					itemNames.remove(itemString);
					config.set("ShopItemList", itemNames);
					config.set("ShopItems." + itemString, null);
					save();
					//add new item
					addItem(slot, sell , buy , itemStack);
				}
			} else if (!itemString.equals("ANVIL_0") && !itemString.equals("CRAFTING_TABLE_0")) {
				throw new ShopSystemException(ShopSystemException.CANNOT_LOAD_SHOPITEM + " |" + itemString + "|");
			}
		}
	}
	
	public ItemStack getItemStack(String itemString) throws ShopSystemException {
		if(!itemNames.contains(itemString)) {
			throw new ShopSystemException(ShopSystemException.ITEM_DOES_NOT_EXIST);
		}
		else {
			config = YamlConfiguration.loadConfiguration(file);
			return config.getItemStack("ShopItems." + itemString + ".Name");
		}
	}

	private boolean hasCustomName(ItemStack itemStack) {
		boolean has = true;
		ItemStack testStack = new ItemStack(itemStack.getType());
		ItemMeta testMeta = testStack.getItemMeta();
		if (itemStack.getItemMeta().getDisplayName().equals(testMeta.getDisplayName())) {
			has = false;
		}
		return has;
	}

	/**
	 * This method removes a item from this shop.
	 * 
	 * @param slot
	 * @throws ShopSystemException
	 */
	public void removeItem(int slot) throws ShopSystemException {
		if (slotIsEmpty(slot + 1)) {
			throw new ShopSystemException(ShopSystemException.INVENTORY_SLOT_EMPTY);
		} else if ((slot + 1) != size && (slot + 1) <= size) {
			String itemString = "";
			ItemStack stack = inventory.getItem(slot);
			if(stack.getType().equals(Material.SPAWNER)) {
				itemString = "SPAWNER_" + stack.getItemMeta().getDisplayName();
			}
			else {
				ItemMeta itemMeta = stack.getItemMeta();
				List<String> loreList = itemMeta.getLore();
				Iterator<String> loreIter = loreList.iterator();
				while (loreIter.hasNext()) {
					String lore = loreIter.next();
					if (lore.contains(" buy for ") || lore.contains(" sell for ")) {
						loreIter.remove();
					}
				}
				itemMeta.setLore(loreList);
				stack.setItemMeta(itemMeta);
				stack.setAmount(1);
				itemString = stack.toString();
			}
			
			config = YamlConfiguration.loadConfiguration(file);
			inventory.clear(slot);
			if(itemNames.contains(itemString)) {
				itemNames.remove(itemString);
				config.set("ShopItemList", itemNames);
				config.set("ShopItems." + itemString, null);
				save();
			}
		} else if ((slot + 1) == size) {
			throw new ShopSystemException(ShopSystemException.ITEM_CANNOT_BE_DELETED);
		}
	}

	/**
	 * This method returns the sellprice of a item.
	 * 
	 * @param itemName
	 * @return double
	 * @throws ShopSystemException
	 */
	public double getItemSellPrice(String itemName) throws ShopSystemException {
		config = YamlConfiguration.loadConfiguration(file);
		if (itemNames.contains(itemName)) {
			return config.getDouble("ShopItems." + itemName + ".sellPrice");
		} else {
			throw new ShopSystemException(ShopSystemException.ITEM_DOES_NOT_EXIST);
		}
	}

	/**
	 * This method returns the amount of a item.
	 * 
	 * @param itemName
	 * @return int
	 * @throws ShopSystemException
	 */
	public int getItemAmount(String itemName) throws ShopSystemException {
		config = YamlConfiguration.loadConfiguration(file);
		if (itemNames.contains(itemName)) {
			return config.getInt("ShopItems." + itemName + ".Amount");
		} else {
			throw new ShopSystemException(ShopSystemException.ITEM_DOES_NOT_EXIST);
		}
	}

	/**
	 * This method returns the buyprice of a item.
	 * 
	 * @param itemName
	 * @return double
	 * @throws ShopSystemException
	 */
	public double getItemBuyPrice(String itemName) throws ShopSystemException {
		config = YamlConfiguration.loadConfiguration(file);
		if (itemNames.contains(itemName)) {
			return config.getDouble("ShopItems." + itemName + ".buyPrice");
		} else {
			throw new ShopSystemException(ShopSystemException.ITEM_DOES_NOT_EXIST);
		}
	}

	/**
	 * This method returns the lore of a item.
	 * 
	 * @param itemName
	 * @return List of Strings
	 * @throws ShopSystemException
	 */
	public List<String> getItemLore(String itemName) throws ShopSystemException {
		config = YamlConfiguration.loadConfiguration(file);
		if (itemNames.contains(itemName)) {
			return config.getStringList("ShopItems." + itemName + ".lore");
		} else {
			throw new ShopSystemException(ShopSystemException.ITEM_DOES_NOT_EXIST);
		}
	}

	/**
	 * This method returns the damage of a item.
	 * 
	 * @param itemName
	 * @return int
	 * @throws ShopSystemException
	 */
	public int getItemDamage(String itemName) throws ShopSystemException {
		config = YamlConfiguration.loadConfiguration(file);
		if (itemNames.contains(itemName)) {
			return config.getInt("ShopItems." + itemName + ".damage");
		} else {
			throw new ShopSystemException(ShopSystemException.ITEM_DOES_NOT_EXIST);
		}
	}

	public static ArrayList<String> addEnchantments(ItemStack itemStack, ArrayList<String> enchantmentList) {
		Enchantment e = null;
		int lvl = 0;
		ArrayList<String> newList = new ArrayList<>();
		for (String enchantment : enchantmentList) {
			e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment.substring(0, enchantment.indexOf("-"))));
			lvl = Integer.valueOf(enchantment.substring(enchantment.indexOf("-") + 1));
			if (e.getMaxLevel() < lvl) {
				lvl = e.getMaxLevel();
				enchantment = enchantment.substring(0, enchantment.indexOf("-") + 1) + String.valueOf(lvl);
			}
			if (itemStack.getType().toString().equals("ENCHANTED_BOOK")) {
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
				meta.addStoredEnchant(e, lvl, true);
				itemStack.setItemMeta(meta);
			} else if (e.canEnchantItem(itemStack)) {
				itemStack.addEnchantment(e, lvl);
			}
		}
		if (itemStack.getType().toString().equals("ENCHANTED_BOOK")) {
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
			for (Entry<Enchantment, Integer> map : meta.getStoredEnchants().entrySet()) {
				newList.add(
						map.getKey().getKey().toString().substring(map.getKey().getKey().toString().indexOf(":") + 1)
								+ "-" + map.getValue().intValue());
				newList.sort(String.CASE_INSENSITIVE_ORDER);
			}
		} else {
			for (Entry<Enchantment, Integer> map : itemStack.getEnchantments().entrySet()) {
				newList.add(
						map.getKey().getKey().toString().substring(map.getKey().getKey().toString().indexOf(":") + 1)
								+ "-" + map.getValue().intValue());
				newList.sort(String.CASE_INSENSITIVE_ORDER);
			}
		}
		return newList;
	}

	public ItemStack getSkull(String url, String name) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
		if (url.isEmpty())
			return head;
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		byte[] encodedData = Base64.getEncoder()
				.encode((String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes()));
		profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
		Field profileField = null;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		headMeta.setDisplayName(name);
		head.setItemMeta(headMeta);
		return head;
	}

	/**
	 * This method returns true if the slot is empty.
	 * 
	 * @param slot
	 * @return boolean
	 * @throws ShopSystemException
	 */
	public boolean slotIsEmpty(int slot) throws ShopSystemException {
		if (slot <= inventory.getSize() && slot > 0) {
			slot--;
			boolean isEmpty = false;
			if (inventory.getItem(slot) == null) {
				isEmpty = true;
			}
			return isEmpty;
		} else {
			throw new ShopSystemException(ShopSystemException.INVENTORY_SLOT_INVALID);
		}
	}

	public List<String> getItemList() {
		return itemNames;
	}

	public List<String> removedoubleObjects(List<String> list) {
		Set<String> set = new LinkedHashSet<String>(list);
		list = new ArrayList<String>(set);
		return list;
	}

	public void save() {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void openInv(Player p) {
		p.openInventory(inventory);
	}

	public String getName() {
		return name;
	}

	public void despawnVillager() {
		villager.remove();
	}

	public void deleteShop() {
		file.delete();
		World world = villager.getLocation().getWorld();
		villager.remove();
		world.save();
	}

	public int getSize() {
		return size;
	}

	public ItemStack getItem(int slot) {
		slot--;
		return inventory.getItem(slot);
	}

	public int getSlotEditorSlot() {
		return slotEditorSlot;
	}

	private void setupEditor() {
		int value = 1;
		if (isPlayershop) {
			value++;
		}
		for (int i = 0; i < (size - value); i++) {
			try {
				if (slotIsEmpty(i + 1)) {
					editor.setItem(i, getSkull(SLOTEMPTY, "Slot " + (i + 1)));
				} else {
					editor.setItem(i, getSkull(SLOTFILLED, "Slot " + (i + 1)));
				}
			} catch (ShopSystemException e) {
			}
		}
	}

	public void openEditor(Player p) {
		setupEditor();
		p.openInventory(editor);
	}

	private void switchPlusMinus(int slot, String state) {
		slot--;
		if (state.equals("plus")) {
			ItemStack item = getSkull(MINUS, "minus");
			slotEditor.setItem(slot, item);
		} else {
			ItemStack item = getSkull(PLUS, "plus");
			slotEditor.setItem(slot, item);
		}
	}
	
	private void switchFactor(int slot, String state) {
		slot--;
		if (state.equals("factor off")) {
			ItemStack item = getSkull(K_OFF, "factor off");
			slotEditor.setItem(slot, item);
		} else {
			ItemStack item = getSkull(K_ON, "factor on");
			slotEditor.setItem(slot, item);
		}
	}

	public void openSlotEditor(Player player, int slot) throws IllegalArgumentException, ShopSystemException {
		setupSlotEditor(slot);
		ItemStack item;
		slotEditorSlot = slot;
		if (slotIsEmpty(slot)) {
			item = new ItemStack(Material.BARRIER);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + "select item");
			item.setItemMeta(meta);
			slotEditor.setItem(0, item);
		} else {
			slot--;
			item = new ItemStack(inventory.getItem(slot));
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			if (lore != null) {
				Iterator<String> iterator = lore.iterator();
				while (iterator.hasNext()) {
					String string = iterator.next();
					if (string.contains("buy for") || string.contains("sell for")) {
						iterator.remove();
					}
				}
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			slotEditor.setItem(0, item);
		}
		player.openInventory(slotEditor);
	}

	private void updateEditorPrice(int a, int b, int c, int d, int e, Double price) {
		List<String> list = new ArrayList<>();
		list.add(ChatColor.GOLD + "Price: " + price);
		ItemMeta meta = slotEditor.getItem(a).getItemMeta();
		meta.setLore(list);
		slotEditor.getItem(a).setItemMeta(meta);
		meta = slotEditor.getItem(b).getItemMeta();
		meta.setLore(list);
		slotEditor.getItem(b).setItemMeta(meta);
		meta = slotEditor.getItem(c).getItemMeta();
		meta.setLore(list);
		slotEditor.getItem(c).setItemMeta(meta);
		meta = slotEditor.getItem(d).getItemMeta();
		meta.setLore(list);
		slotEditor.getItem(d).setItemMeta(meta);
		meta = slotEditor.getItem(e).getItemMeta();
		meta.setLore(list);
		slotEditor.getItem(e).setItemMeta(meta);
	}

	private String getShopItemName(ItemStack itemStack) {
		String name = "";
		ItemMeta meta = itemStack.getItemMeta();
		String material = itemStack.getType().name();
		ArrayList<String> enchantmentList = new ArrayList<>();
		if (hasCustomName(itemStack) && !material.equals("SPAWNER")) {
			name = meta.getDisplayName() + "|";
		}
		switch (getItemType(itemStack)) {
			case "potion":
				String property = "none";
				PotionMeta potionMeta = (PotionMeta) meta;
				if (potionMeta.getBasePotionData().isExtended()) {
					property = "extended";
				} else if (potionMeta.getBasePotionData().isUpgraded()) {
					property = "upgraded";
				} else {
					property = "none";
				}
				name = name + itemStack.getType().toString().toLowerCase() + ":"
						+ potionMeta.getBasePotionData().getType().toString().toLowerCase() + "#" + property;
				break;
			case "enchanted":
				Map<Enchantment, Integer> enchants = null;
				if (itemStack.getType().toString().equals("ENCHANTED_BOOK")) {
					EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) meta;
					enchants = enchantMeta.getStoredEnchants();
				} else {
					enchants = meta.getEnchants();
				}
				for (Entry<Enchantment, Integer> e : enchants.entrySet()) {
					enchantmentList.add(e.getKey().getKey().getKey().toLowerCase() + "-" + e.getValue());
				}
				enchantmentList.sort(String.CASE_INSENSITIVE_ORDER);
				name = name + itemStack.getType().toString().toLowerCase() + "#Enchanted_" + enchantmentList.toString();
				break;
			case "default":
				name = name + material;
				break;
		}
		return name;
	}

	private String getItemType(ItemStack itemStack) {
		String type = null;
		EnchantmentStorageMeta meta = null;
		if (itemStack.getType() == Material.ENCHANTED_BOOK) {
			meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
		}
		if (itemStack.getType().toString().toLowerCase().contains("potion")) {
			type = "potion";
		} else if (itemStack.hasItemMeta() && !itemStack.getEnchantments().isEmpty()
				|| meta != null && !meta.getStoredEnchants().isEmpty()) {
			type = "enchanted";
		} else {
			type = "default";
		}
		return type;
	}

	/**
	 * This method moves a shop to a new location.
	 * 
	 * @param location
	 * @throws TownSystemException 
	 * @Param player (only playershop)
	 */
	public void moveShop(Location location, String player) throws TownSystemException {
		if(isPlayershop && TownWorld.isTownWorld(location.getWorld().getName())) {
			TownWorld townWorld = null;
			try {
				townWorld = TownWorld.getTownWorldByName(location.getWorld().getName());
			} catch (TownSystemException e) {
				//should never happen
			}
				if(townWorld.chunkIsFree(location.getChunk())) {
					throw new TownSystemException(TownSystemException.PLAYER_HAS_NO_PERMISSION);
				} else {
					Town town = townWorld.getTownByChunk(location.getChunk());
					if(!town.hasBuildPermissions(player, location.getChunk().getX() + "/" + location.getChunk().getZ())) {
						throw new TownSystemException(TownSystemException.PLAYER_HAS_NO_PERMISSION);
					}
				}
			
		}
		config = YamlConfiguration.loadConfiguration(file);
		config.set("ShopLocation.x", location.getBlockX());
		config.set("ShopLocation.y", location.getBlockY());
		config.set("ShopLocation.z", location.getBlockZ());
		config.set("ShopLocation.World", location.getWorld().getName());
		villager.teleport(location);
		save();
	}

	/**
	 * This method handles the SlotEditor in the InventoryClickEvent.
	 * 
	 * @param event
	 * @throws ShopSystemException
	 */
	public void handleSlotEditor(InventoryClickEvent event) throws ShopSystemException {
		if (event.getCurrentItem().getItemMeta() != null) {
			Player player = (Player) event.getWhoClicked();
			ItemStack originStack = null;
			if (inventory.getItem(slotEditorSlot - 1) != null) {
				originStack = new ItemStack(inventory.getItem(slotEditorSlot - 1));
			}
			int slot = event.getSlot() + 1;
			String command = event.getCurrentItem().getItemMeta().getDisplayName();
			String operator = null;
			int factor = 1;
			if(event.getInventory().getItem(12).getItemMeta().getDisplayName().equals("factor on")) {
				factor = 1000;
			}
			double price = 0.0;
			switch (slot) {
				case 5:
				case 6:
				case 7:
					operator = event.getInventory().getItem(2).getItemMeta().getDisplayName();
					break;
				case 14:
				case 15:
				case 16:
					operator = event.getInventory().getItem(11).getItemMeta().getDisplayName();
					price = Double
							.valueOf(event.getInventory().getItem(9).getItemMeta().getLore().get(0).substring(9));
					break;
				case 23:
				case 24:
				case 25:
					operator = event.getInventory().getItem(20).getItemMeta().getDisplayName();
					price = Double
							.valueOf(event.getInventory().getItem(18).getItemMeta().getLore().get(0).substring(9));
					break;
			}
			ItemStack editorItemStack = slotEditor.getItem(0);
			if (command.equals("plus")) {
				switchPlusMinus(slot, "plus");
			} else if (command.equals("minus")) {
				switchPlusMinus(slot, "minus");
			} else if(command.equals("factor off")) {
				switchFactor(slot, "factor on");
			} else if(command.equals("factor on")) {
				switchFactor(slot, "factor off");
			} else if (command.equals("one")) {
				switch (slot) {
					case 5:
						if (editorItemStack != null && operator.equals("plus")
								&& (editorItemStack.getAmount() + 1 <= 64)) {
							editorItemStack.setAmount(editorItemStack.getAmount() + 1);
						} else if (editorItemStack != null && operator.equals("plus")
								&& (editorItemStack.getAmount() + 1 > 64)) {
							editorItemStack.setAmount(64);
						} else if (editorItemStack != null && editorItemStack.getAmount() > 1) {
							editorItemStack.setAmount(editorItemStack.getAmount() - 1);
						}
						break;
					case 14:
						if (price >= 1 && operator.equals("minus")) {
							updateEditorPrice(9, 11, 13, 14, 15, price - 1*factor);
						} else if (operator.equals("plus")) {
							updateEditorPrice(9, 11, 13, 14, 15, price + 1*factor);
						}
						break;
					case 23:
						if (price >= 1 && operator.equals("minus")) {
							updateEditorPrice(18, 20, 22, 23, 24, price - 1*factor);
						} else if (operator.equals("plus")) {
							updateEditorPrice(18, 20, 22, 23, 24, price + 1*factor);
						}
						break;
				}
			} else if (command.equals("ten")) {
				switch (slot) {
					case 6:
						if (editorItemStack != null && operator.equals("plus")
								&& (editorItemStack.getAmount() + 10 <= 64)) {
							editorItemStack.setAmount(editorItemStack.getAmount() + 10);
						} else if (editorItemStack != null && operator.equals("plus")
								&& (editorItemStack.getAmount() + 10 > 64)) {
							editorItemStack.setAmount(64);
						} else if (editorItemStack != null && editorItemStack.getAmount() > 10) {
							editorItemStack.setAmount(editorItemStack.getAmount() - 10);
						}
						break;
					case 15:
						if (price >= 10 && operator.equals("minus")) {
							updateEditorPrice(9, 11, 13, 14, 15, price - 10*factor);
						} else if (operator.equals("plus")) {
							updateEditorPrice(9, 11, 13, 14, 15, price + 10*factor);
						}
						break;
					case 24:
						if (price >= 10 && operator.equals("minus")) {
							updateEditorPrice(18, 20, 22, 23, 24, price - 10*factor);
						} else if (operator.equals("plus")) {
							updateEditorPrice(18, 20, 22, 23, 24, price + 10*factor);
						}
						break;
				}
			} else if (command.equals("twenty")) {
				switch (slot) {
					case 7:
						if (editorItemStack != null && operator.equals("plus")
								&& (editorItemStack.getAmount() + 20 <= 64)) {
							editorItemStack.setAmount(editorItemStack.getAmount() + 20);
						} else if (editorItemStack != null && operator.equals("plus")
								&& (editorItemStack.getAmount() + 20 > 64)) {
							editorItemStack.setAmount(64);
						} else if (editorItemStack != null && editorItemStack.getAmount() > 20) {
							editorItemStack.setAmount(editorItemStack.getAmount() - 20);
						}
						break;
					case 16:
						if (price >= 20 && operator.equals("minus")) {
							updateEditorPrice(9, 11, 13, 14, 15, price - 20*factor);
						} else if (operator.equals("plus")) {
							updateEditorPrice(9, 11, 13, 14, 15, price + 20*factor);
						}
						break;
					case 25:
						if (price >= 20 && operator.equals("minus")) {
							updateEditorPrice(18, 20, 22, 23, 24, price - 20*factor);
						} else if (operator.equals("plus")) {
							updateEditorPrice(18, 20, 22, 23, 24, price + 20*factor);
						}
						break;
				}
			} else if (command.equals(ChatColor.YELLOW + "save changes")) {
				double buyPrice = Double
						.valueOf(event.getInventory().getItem(9).getItemMeta().getLore().get(0).substring(9));
				double sellPrice = Double
						.valueOf(event.getInventory().getItem(18).getItemMeta().getLore().get(0).substring(9));
				if (buyPrice != 0 || sellPrice != 0) {
					ItemStack itemStack = event.getInventory().getItem(0);
					Boolean isNew = true;
					Boolean existsInOtherSlot = false;
					if (originStack != null) {
						if (originStack.getItemMeta().getLore() != null) {
							List<String> loreList = originStack.getItemMeta().getLore();
							Iterator<String> iterator = loreList.iterator();
							while (iterator.hasNext()) {
								String lore = iterator.next();
								if (lore.contains("buy for") || lore.contains("sell for")) {
									iterator.remove();
								}
							}
							ItemMeta meta2 = originStack.getItemMeta();
							meta2.setLore(loreList);
							originStack.setItemMeta(meta2);
						}
						Integer amount = itemStack.getAmount();
						itemStack.setAmount(originStack.getAmount());
						if (!itemStack.toString().equals(originStack.toString())) {
							for (String itemName : itemNames) {
								if (itemName.equals(getShopItemName(itemStack))) {
									existsInOtherSlot = true;
									player.sendMessage(ChatColor.RED + "This item exists in a other slot!");
								}
							}
							if (!existsInOtherSlot) {
								removeItem(slotEditorSlot - 1);
								player.sendMessage(ChatColor.GOLD + "The item " + ChatColor.GREEN
										+ originStack.getType().toString().toLowerCase() + ChatColor.GOLD
										+ " was removed from shop.");
							}
						} else {
							isNew = false;
							editItem(slotEditorSlot, String.valueOf(amount), String.valueOf(sellPrice),
									String.valueOf(buyPrice));
						}
					}
					if (isNew && !existsInOtherSlot) {
						addItem(slotEditorSlot - 1, sellPrice, buyPrice, itemStack);
						player.sendMessage(ChatColor.GOLD + "The item " + ChatColor.GREEN
								+ itemStack.getType().toString().toLowerCase() + ChatColor.GOLD
								+ " was added to the shop.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "The sellprice and the buyprice are both 0!");
				}
			} else if (command.equals(ChatColor.RED + "remove item")) {
				removeItem(slotEditorSlot - 1);
				player.sendMessage(ChatColor.GOLD + "The item " + ChatColor.GREEN
						+ originStack.getType().toString().toLowerCase() + ChatColor.GOLD + " was removed from shop.");
			} else if (!command.equals("buyprice") && !command.equals("sellprice")) {
				ItemStack editorItemStack2 = new ItemStack(event.getCurrentItem());
				editorItemStack2.setAmount(1);
				slotEditor.setItem(0, editorItemStack2);
			}
		}
	}

	/**
	 * This method adds a shopitem to the shop inventory. Not for commercial use.
	 * 
	 * @param itemStack
	 * @param amount
	 * @param slot
	 * @param sellPrice
	 * @param buyPrice
	 */
	public void addShopItemToInv(ItemStack itemStack, int amount, int slot, double sellPrice, double buyPrice) {
		String displayName = itemStack.getItemMeta().getDisplayName();
		List<String> list = null;
		if (itemStack.getItemMeta().getLore() != null) {
			list = itemStack.getItemMeta().getLore();
		} else {
			list = new ArrayList<>();
		}
		ItemMeta meta = itemStack.getItemMeta();
		if (displayName.equals("Info")) {
			meta.setDisplayName("Info");
			list.add(ChatColor.GOLD + "Rightclick: " + ChatColor.GREEN + "sell specified amount");
			list.add(ChatColor.GOLD + "Shift-Rightclick: " + ChatColor.GREEN + "sell all");
			list.add(ChatColor.GOLD + "Leftclick: " + ChatColor.GREEN + "buy");
		} else if (displayName.equals("Stock")) {
			list.add(ChatColor.RED + "Only for Shopowner");
			list.add(ChatColor.GOLD + "Middle Mouse: " + ChatColor.GREEN + "open/close stockpile");
		} else if (sellPrice == 0.0) {
			list.add(ChatColor.GOLD + String.valueOf(amount) + " buy for " + ChatColor.GREEN + buyPrice + "$");
		} else if (buyPrice == 0.0) {
			list.add(ChatColor.GOLD + String.valueOf(amount) + " sell for " + ChatColor.GREEN + sellPrice + "$");
		} else {
			list.add(ChatColor.GOLD + String.valueOf(amount) + " buy for " + ChatColor.GREEN + buyPrice + "$");
			list.add(ChatColor.GOLD + String.valueOf(amount) + " sell for " + ChatColor.GREEN + sellPrice + "$");
		}
		meta.setLore(list);
		itemStack.setItemMeta(meta);
		itemStack.setAmount(amount);
		inventory.setItem(slot, itemStack);
	}

	public void reload() throws ShopSystemException {
		for (String item : itemNames) {
			loadItem(item);
		}
	}
}