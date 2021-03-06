package com.ue.exceptions;

public enum PlayerExceptionMessageEnum {

    NO_PERMISSION ("player_has_no_permission"),
    NO_PERMISSION_SET_SPAWNER ("no_permission_set_spawner"),
    NO_PERMISSION_BREAK_SPAWNER ("no_permission_break_spawner"),
    WILDERNESS ("wilderness"),
    TOWN_NOT_TOWN_OWNER ("town_not_town_owner"),
    NO_PERMISSION_ON_PLOT ("no_permission_on_plot"),
    JOB_ALREADY_JOINED ("job_already_joined"),
    JOB_NOT_JOINED ("job_not_joined"),
    TOWN_ALREADY_JOINED ("town_already_joined"),
    TOWN_NOT_JOINED ("town_not_joined"),
    NOT_ENOUGH_MONEY_PERSONAL ("not_enough_money_personal"),
    YOU_ARE_NOT_OWNER ("you_are_not_owner"),
    YOU_ARE_THE_OWNER ("you_are_the_owner"),
    OUTSIDE_OF_THE_PLOT ("outside_of_the_plot"),
    YOU_ARE_ALREADY_CITIZEN ("you_are_already_citizen"),
    YOU_ARE_NO_CITIZEN ("you_are_no_citizen"),
    SHOPOWNER_NOT_ENOUGH_MONEY ("shopowner_not_enough_money"),
    ENCHANTMENTLIST_INCOMPLETE ("enchantmentlist_incomplete"),
    PLAYER_DOES_NOT_EXIST ("player_does_not_exist"),
    PLAYER_ALREADY_EXIST ("player_already_exist"),
    HOME_DOES_NOT_EXIST ("home_does_not_exist"),
    HOME_ALREADY_EXIST ("home_already_exist"),
    INVENTORY_SLOT_OCCUPIED ("inventory_slot_occupied"),
    NOT_ENOUGH_MONEY_NON_PERSONAL ("not_enough_money_non_personal"),
    MAX_REACHED ("max_reached");
	
    private String value; 
    
    private PlayerExceptionMessageEnum(String value) { 
        this.value = value; 
    } 
  
    /**
     * Returns the value of this enum. The value is the name of the message in the language file.
     * @return string
     */
    public String getValue() { 
        return this.value; 
    } 
}
