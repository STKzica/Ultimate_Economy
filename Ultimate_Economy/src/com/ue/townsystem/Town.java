package com.ue.townsystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ue.exceptions.banksystem.PlayerDoesNotExistException;
import com.ue.exceptions.banksystem.PlayerHasNotEnoughtMoneyException;
import com.ue.exceptions.townsystem.ChunkAlreadyClaimedException;
import com.ue.exceptions.townsystem.ChunkNotClaimedByThisTownException;
import com.ue.exceptions.townsystem.PlayerIsAlreadyCitizenException;
import com.ue.exceptions.townsystem.PlayerIsAlreadyCoOwnerException;
import com.ue.exceptions.townsystem.PlayerIsNotCitizenException;
import com.ue.exceptions.townsystem.PlotIsAlreadyForSaleException;
import com.ue.exceptions.townsystem.PlotIsNotForSaleException;
import com.ue.exceptions.townsystem.TownDoesNotExistException;
import com.ue.utils.PaymentUtils;

public class Town {

	private String townName;
	private String owner;
	private ArrayList<String> citizens,coOwners;
	private ArrayList<String> chunkCoords;
	private Location townSpawn;
	private ArrayList<Plot> plots;
	private double tax; //TODO integrate tax system
	
	/**
	 * <p>
	 * Creates a town object.
	 * Only if you load a existing town, the parameter startChunk should be 'null'
	 * <p>
	 * @param file
	 * @param owner
	 * @param townName
	 * @param startChunk
	 */
	public Town(File file,String owner,String townName,Chunk startChunk) {
		this.townName = townName;
		this.owner = owner;
		citizens = new ArrayList<>();
		coOwners = new ArrayList<>();
		chunkCoords = new ArrayList<>();
		plots = new ArrayList<>();
		citizens.add(owner);
		chunkCoords.add(startChunk.getX() + "/" + startChunk.getZ());
		if(startChunk != null) {
			setOwner(file, owner);
			setCoOwners(coOwners);
			setCiticens(citizens);
			setChunkList(chunkCoords);
			Location spawn = new Location(startChunk.getWorld(), (startChunk.getX() << 4) + 7, 0, (startChunk.getZ() << 4) + 7);
			spawn.setY(spawn.getWorld().getHighestBlockYAt(spawn));
			setTownSpawn(file, spawn);
		}
	}
	
	public void createTownManagerVillager() {
		//TODO
	}
	
	public void moveTownManagerVillager() {
		//TODO
	}
	
	public void despawnTownManagerVillager() {
		//TODO
	}
	
	public void removeTownManagerVillager() {
		//TODO
	}
	
	/**
	 * <p>
	 * 	Set a plot for sale.
	 * <p>
	 * @param chunk	(format "X/Z")
	 * @throws ChunkNotClaimedByThisTownException 
	 * @throws PlotIsNotForSaleException 
	 * @throws PlotIsAlreadyForSaleException 
	 */
	public void setSlotForSale(File file,double salePrice,int chunkX,int chunkZ) throws ChunkNotClaimedByThisTownException, PlotIsAlreadyForSaleException, PlotIsNotForSaleException {
		String coords = chunkX + "/" + chunkZ;
		if(chunkCoords.contains(coords)) {
			getPlotByChunkCoords(coords).setForSale(file, true, salePrice);
		}
		else {
			throw new ChunkNotClaimedByThisTownException(coords);
		}
	}
	
	/**
	 * <p>
	 * Remove a slot from sale.
	 * <p>
	 * @param chunk	(format "X/Z")
	 * @throws ChunkNotClaimedByThisTownException 
	 * @throws PlotIsNotForSaleException 
	 * @throws PlotIsAlreadyForSaleException 
	 */
	public void removeSlotFromSale(File file,double salePrice,int chunkX,int chunkZ) throws ChunkNotClaimedByThisTownException, PlotIsAlreadyForSaleException, PlotIsNotForSaleException {
		String coords = chunkX + "/" + chunkZ;
		if(chunkCoords.contains(coords)) {
			getPlotByChunkCoords(coords).setForSale(file, false, 0);
		}
		else {
			throw new ChunkNotClaimedByThisTownException(coords);
		}
	}
	
	/**
	 * <p>
	 * Buy a plot in a town if the plot is for sale.
	 * <p>
	 * @param citizen
	 * @param chunk	(format "X/Z")
	 * @throws ChunkNotClaimedByThisTownException 
	 * @throws PlayerDoesNotExistException 
	 * @throws PlotIsNotForSaleException 
	 * @throws PlayerHasNotEnoughtMoneyException (expect that the buyer is the same as the command/ui executor)
	 */
	public void buyPlot(File townworldfile,File playerfile,String citizen,String chunk) throws ChunkNotClaimedByThisTownException, PlayerDoesNotExistException, PlotIsNotForSaleException, PlayerHasNotEnoughtMoneyException {
		Plot plot = getPlotByChunkCoords(chunk);
		if(!plot.isForSale()) {
			throw new PlotIsNotForSaleException(chunk);
		}
		else if(PaymentUtils.playerHasEnoughtMoney(playerfile, citizen, plot.getSalePrice())){
			throw new PlayerHasNotEnoughtMoneyException(citizen,true);
		}
		else {
			plot.setOwner(townworldfile, citizen);
			plot.setForSale(false);
			PaymentUtils.decreasePlayerAmount(playerfile, citizen, plot.getSalePrice());
		}
	}
	
	/**
	 * <p>
	 * Adds a chunk to a town
	 * <p>
	 * @param file
	 * @param chunk	(format "X/Z")
	 * @throws ChunkAlreadyClaimedException 
	 */
	public void addChunk(File file,int chunkX,int chunkZ) throws ChunkAlreadyClaimedException {
		String coords = chunkX + "/" + chunkZ;
		if(!chunkCoords.contains(coords)) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			chunkCoords.add(coords);
			config.set("Towns." + townName + ".chunks", chunkCoords);
			save(file,config);
			plots.add(new Plot(file, owner, coords, townName));
		}
		else {
			throw new ChunkAlreadyClaimedException(coords);
		}
	}
	
	/**
	 * <p>
	 * Removes a chunk from a town
	 * <p>
	 * @param file
	 * @param chunk	(format "X/Z")
	 * @throws ChunkNotClaimedByThisTownException 
	 */
	@SuppressWarnings("deprecation")
	public void removeChunk(File file,int chunkX,int chunkZ,World world) throws ChunkNotClaimedByThisTownException {
		String coords = chunkX + "/" + chunkZ;
		if(chunkCoords.contains(coords)) {
			chunkCoords.remove(coords);
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			config.set("Towns." + townName + ".chunks", chunkCoords);
			save(file,config);
			//TODO not for future, find a better solution
			world.regenerateChunk(chunkX, chunkZ);
			world.save();
			int index = -1;
			for(Plot plot: plots) {
				if(plot.getChunkCoords().equals(coords)) {
					index = plots.indexOf(plot);
					break;
				}
			}
			if(index != -1) {
				plots.remove(index);
			}
		}
		else {
			throw new ChunkNotClaimedByThisTownException(coords);
		}
	}
	
	/**
	 * <p>
	 * Get town owner
	 * <p>
	 * @return String
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * <p>
	 * Set town owner
	 * <p>
	 * @param file
	 * @param owner
	 */
	public void setOwner(File file,String owner) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		this.owner = owner;
		config.set("Towns." + townName + ".owner", owner);
		save(file,config);
	}
	
	/**
	 * <p>
	 * Get list of citizens
	 * <p>
	 * @return ArrayList
	 */
	public ArrayList<String> getCitizens() {
		return citizens;
	}
	/**
	 * <p>
	 * Set all citizens.
	 * <p>
	 * @param citizens
	 */
	public void setCiticens(List<String> citizens) {
		this.citizens.addAll(citizens);
	}
	
	/**
	 * <p>
	 * Add a player as citizen to a town
	 * <p>
	 * @param file
	 * @param newCitizen
	 * @throws PlayerIsAlreadyCitizenException 
	 */
	public void addCitizen(File file,String newCitizen) throws PlayerIsAlreadyCitizenException {
		if(!citizens.contains(newCitizen)) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			citizens.add(newCitizen);
			config.set("Towns." + townName + ".citizens", citizens);
			save(file,config);
		}
		else {
			throw new PlayerIsAlreadyCitizenException(newCitizen);
		}
	}
	
	/**
	 * <p>
	 * Remove a player as citizen from a town
	 * <p>
	 * @param file
	 * @param citizen
	 * @throws PlayerIsNotCitizenException 
	 */
	public void removeCitizen(File file,String citizen) throws PlayerIsNotCitizenException {
		if(citizens.contains(citizen)) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			citizens.remove(citizen);
			config.set("Towns." + townName + ".citizens", citizens);
			save(file,config);
		}
		else {
			throw new PlayerIsNotCitizenException(citizen);
		}
	}
	
	/**
	 * <p>
	 * Get town name
	 * <p>
	 * @return String
	 */
	public String getTownName() {
		return townName;
	}
	
	/**
	 * <p>
	 * Set town name
	 * <p>
	 * @param file
	 * @param townName
	 */
	public void setTownName(File file,String townName) {
		this.townName = townName;
	}
	
	/**
	 * <p>
	 * Get a list of all claimed chunks
	 * <p>
	 * @return ArrayList
	 */
	public ArrayList<String> getChunkList() {
		return chunkCoords;
	}
	/**
	 * <p>
	 * Set the chunklist.
	 * <p>
	 * @param chunkCoords
	 */
	public void setChunkList(List<String> chunkCoords) {
		this.chunkCoords.addAll(chunkCoords);
	}
	
	/**
	 * <p>
	 * Set the plotlist
	 * <p>
	 * @param list
	 */
	public void setPlotList(ArrayList<Plot> list) {
		plots = list;
	}
	
	/**
	 * <p>
	 * Get the town spawn location
	 * <p>
	 * @return Location
	 */
	public Location getTownSpawn() {
		return townSpawn;
	}
	
	/**
	 * <p>
	 * Set the town spawn location
	 * <p>
	 * @param file
	 * @param townSpawn
	 */
	public void setTownSpawn(File file,Location townSpawn) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		this.townSpawn = townSpawn;
		config.set("Towns." + townName + ".townspawn", townSpawn.getX() + "/" + townSpawn.getY() + "/" + townSpawn.getZ());
		save(file,config);
	}
	
	/**
	 * <p>
	 * Get a list of CoOwners of the town
	 * <p>
	 * @return ArrayList
	 */
	public ArrayList<String> getCoOwners() {
		return coOwners;
	}
	/**
	 * <p>
	 * Set all coOwners.
	 * <p>
	 * @param coOwners
	 */
	public void setCoOwners(List<String> coOwners) {
		this.coOwners.addAll(coOwners);
	}
	
	/**
	 * <p>
	 * Set a player as CoOwner of a town
	 * <p>
	 * @param file
	 * @param coOwner
	 * @return
	 * @throws PlayerIsAlreadyCoOwnerException 
	 */
	public void addCoOwner(File file,String coOwner) throws PlayerIsAlreadyCoOwnerException {
		if(!coOwners.contains(coOwner)) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			coOwners.add(coOwner);
			config.set("Towns." + townName + ".coOwners", coOwners);
			save(file,config);
		}
		else {
			throw new PlayerIsAlreadyCoOwnerException(coOwner,"town");
		}
	}
	
	private void save(File file,FileConfiguration config) {
		try {
			config = YamlConfiguration.loadConfiguration(file);
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>
	 * Get savefile of townworld with this town
	 * <p>
	 * @return File
	 */
	/*public File getFilea() {
		return file;
	}*/
	
	/**
	 * <p>
	 * Returns true if the cunk is owned by any town
	 * <p>
	 * @param chunk	(format "X/Z")
	 * @return
	 */
	public boolean isClaimedByTown(Chunk chunk) {
		boolean is = false;
		if(chunkCoords.contains(chunk.getX() + "/" + chunk.getZ())) {
			is = true;
		}
		return is;
	}
	
	/**
	 * <p>
	 * Get a plot with the chunk coords.
	 * <p>
	 * @param coords
	 * @return
	 * @throws ChunkNotClaimedByThisTownException
	 */
	private Plot getPlotByChunkCoords(String coords) throws ChunkNotClaimedByThisTownException {
		Plot p = null;
		for(Plot plot:plots) {
			if(plot.getChunkCoords().equals(coords)) {
				p = plot;
			}
		}
		if(p != null) {
			return p;
		}
		else {
			throw new ChunkNotClaimedByThisTownException(coords);
		}
	}
	
	/**
	 * <p>
	 * Static method for loading a existing town by name.
	 * <p>
	 * @param file
	 * @param townName
	 * @return
	 * @throws TownDoesNotExistException 
	 * @throws ChunkNotClaimedByThisTownException 
	 */
	public static Town loadTown(File file,String townName) throws TownDoesNotExistException, ChunkNotClaimedByThisTownException {
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		if(config.getStringList("TownNames").contains(townName)) {
			Town town = new Town(file, config.getString("Towns." + townName + ".owner"), townName, null);
			town.setCoOwners(config.getStringList("Towns." + townName + ".coOwners"));
			town.setCiticens(config.getStringList("Towns." + townName + ".citizens"));
			town.setChunkList(config.getStringList("Towns." + townName + ".chunks"));
			ArrayList<Plot> plotList = new ArrayList<>();
			for(String coords:town.getChunkList()) {
				plotList.add(Plot.loadPlot(file, townName, coords));
			}
			town.setPlotList(plotList);
			String locationString = config.getString("Towns." + townName + ".townspawn");
			town.setTownSpawn(file, new Location(Bukkit.getWorld(config.getString("World")), Integer.valueOf(locationString.substring(0, locationString.indexOf("/"))), Integer.valueOf(locationString.substring(locationString.indexOf("/")+1,locationString.lastIndexOf("/"))), Integer.valueOf(locationString.substring(locationString.lastIndexOf("/")+1))));
			return town;
		}
		else {
			throw new TownDoesNotExistException(townName);
		}
	}
}
