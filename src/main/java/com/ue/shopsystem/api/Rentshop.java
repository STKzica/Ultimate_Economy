package com.ue.shopsystem.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.ue.exceptions.GeneralEconomyException;
import com.ue.exceptions.PlayerException;
import com.ue.exceptions.ShopSystemException;

public interface Rentshop extends Playershop {

    /**
     * --Get Method--
     * <p>
     * Returns the rental until time in milliseconds.
     * 
     * @return rentUntil
     */
    public double getRentUntil();

    /**
     * --Get Method--
     * <p>
     * Returns true, if this shop is not rented by a other player.
     * 
     * @return rentable
     */
    public boolean isRentable();

    /**
     * --RentShop Method--
     * <p>
     * Resets the entire shop. Sets the shop back to the "rentable" state. Removes
     * all items from the shop.
     * 
     */
    public void resetShop();

    /**
     * --RentShop Method--
     * <p>
     * Opens the rentshop GUI, if the shop is not rented.
     * 
     * @param player
     */
    public void openRentGUI(Player player);

    /**
     * --RentShop Method--
     * <p>
     * Handles the clickevent for the rentGUI.
     * 
     * @param event
     *            InventoryClickEvent
     * @throws PlayerException
     * @throws ShopSystemException
     * @throws GeneralEconomyException
     */
    public void handleRentShopGUIClick(InventoryClickEvent event)
	    throws ShopSystemException, PlayerException, GeneralEconomyException;
}
