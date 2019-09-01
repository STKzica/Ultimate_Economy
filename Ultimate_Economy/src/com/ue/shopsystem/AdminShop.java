package com.ue.shopsystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ue.exceptions.ShopSystemException;

public class AdminShop extends Shop {

	public static List<AdminShop> adminShopList = new ArrayList<>();

	/**
	 * Constructor for creating a new adminShop.
	 * 
	 * @param dataFolder
	 * @param name
	 * @param spawnLocation
	 * @param size
	 */
	private AdminShop(File dataFolder, String name, Location spawnLocation, int size) {
		super(dataFolder, name, spawnLocation, size, false);
		for (String item : itemNames) {
			try {
				loadItem(item);
			} catch (ShopSystemException e) {
				Bukkit.getLogger().log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	/**
	 * Constructor for loading an existing adminShop.
	 * 
	 * @param dataFolder
	 * @param server
	 * @param name
	 */
	private AdminShop(File dataFolder, Server server, String name) {
		super(dataFolder, server, name, false);
		ArrayList<String> tempList = new ArrayList<>(itemNames);
		for (String item : tempList) {
			try {
				loadItem(item);
			} catch (ShopSystemException e) {
				Bukkit.getLogger().log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	@Override
	public void loadItem(String name1) throws ShopSystemException {
		super.loadItem(name1);
		if (config.getString("ShopItems." + name1 + ".Name") != null) {
			String string = config.getString("ShopItems." + name1 + ".Name");
			if (string.contains("SPAWNER_")) {
				String entityname = string.substring(8);
				ItemStack itemStack = new ItemStack(Material.SPAWNER);
				ItemMeta meta = itemStack.getItemMeta();
				meta.setDisplayName(entityname);
				itemStack.setItemMeta(meta);
				addShopItemToInv(itemStack, config.getInt("ShopItems." + name1 + ".Amount"),
						config.getInt("ShopItems." + name1 + ".Slot"),
						config.getDouble("ShopItems." + name1 + ".sellPrice"),
						config.getDouble("ShopItems." + name1 + ".buyPrice"));
			}
		}
	}
	
	@Override
	public void renameShop(File dataFolder, String newName, String player) throws ShopSystemException {
		String nameTemp = null;
		if(!AdminShop.getAdminShopNameList().contains(newName)) {
			nameTemp = newName;
		} else {
			throw new ShopSystemException(ShopSystemException.SHOP_ALREADY_EXISTS);
		}
		File newFile = new File(dataFolder, nameTemp + ".yml");
		if(!newFile.exists()) {
			config = YamlConfiguration.loadConfiguration(file);
			name = nameTemp;
			Bukkit.getLogger().info(config.getInt("ShopSize") + "");
			config.set("ShopName", name);
			try {
				config.save(newFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			file.delete();
			file = newFile;
			villager.setCustomName(name);
			Inventory inventoryNew = Bukkit.createInventory(null, size, name);
			inventoryNew.setContents(inventory.getContents());
			inventory = inventoryNew;
			Inventory editorNew = Bukkit.createInventory(null, this.size, name + "-Editor");
			editorNew.setContents(editor.getContents());
			editor = editorNew;
			Inventory slotEditorNew = Bukkit.createInventory(null, 27, name + "-SlotEditor");
			slotEditorNew.setContents(slotEditor.getContents());
			slotEditor = slotEditorNew;
		} else {
			throw new ShopSystemException(ShopSystemException.ERROR_ON_RENAMING);
		}
	}

	/**
	 * This method returns a list of adminshop names.
	 * 
	 * @return List of Strings
	 */
	public static List<String> getAdminShopNameList() {
		List<String> list = new ArrayList<>();
		for (AdminShop shop : adminShopList) {
			list.add(shop.getName());
		}
		return list;
	}

	/**
	 * This method returns a AdminShop by it's name.
	 * 
	 * @param name
	 * @return
	 * @throws ShopSystemException
	 */
	public static AdminShop getAdminShopByName(String name) throws ShopSystemException {
		for (AdminShop shop : adminShopList) {
			if (shop.getName().equals(name)) {
				return shop;
			}
		}
		throw new ShopSystemException(ShopSystemException.SHOP_DOES_NOT_EXIST);
	}

	/**
	 * This method should be used to create a new adminshop.
	 * 
	 * @param dataFolder
	 * @param server
	 * @param name
	 * @param spawnLocation
	 * @param size
	 * @throws ShopSystemException
	 */
	public static void createAdminShop(File dataFolder, String name, Location spawnLocation, int size)
			throws ShopSystemException {
		if (name.equals("Spawner") || name.contains("-") || name.contains("_")) {
			throw new ShopSystemException(ShopSystemException.INVALID_SHOP_NAME);
		} else if (getAdminShopNameList().contains(name)) {
			throw new ShopSystemException(ShopSystemException.SHOP_ALREADY_EXISTS);
		} else if (size % 9 != 0) {
			throw new ShopSystemException(ShopSystemException.INVALID_INVENTORY_SIZE);
		} else {
			adminShopList.add(new AdminShop(dataFolder, name, spawnLocation, size));
		}
	}

	/**
	 * This method should be used to delete a adminshop.
	 * 
	 * @param name
	 * @throws ShopSystemException
	 */
	public static void deleteAdminShop(String name) throws ShopSystemException {
		AdminShop shop = getAdminShopByName(name);
		adminShopList.remove(shop);
		shop.deleteShop();
	}

	/**
	 * This method despawns all adminshop villager.
	 */
	public static void despawnAllVillagers() {
		for (AdminShop shop : adminShopList) {
			shop.despawnVillager();
		}
	}

	/**
	 * This method loads all adminShops.
	 * 
	 * @param fileConfig
	 * @param dataFolder
	 * @param server
	 */
	public static void loadAllAdminShops(FileConfiguration fileConfig, File dataFolder, Server server) {
		for (String shopName : fileConfig.getStringList("ShopNames")) {
			File file = new File(dataFolder, shopName + ".yml");
			if (file.exists()) {
				adminShopList.add(new AdminShop(dataFolder, server, shopName));
			} else {
				Bukkit.getLogger().log(Level.WARNING, ShopSystemException.CANNOT_LOAD_SHOP,
						new ShopSystemException(ShopSystemException.CANNOT_LOAD_SHOP));
			}
		}
	}
}