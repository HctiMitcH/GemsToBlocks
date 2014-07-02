/*
 * GemsToBlocks Created by HctiMitcH
 * Sunday, February 2nd 2014
 * Description: Crafts Gems specified in the config into blocks when a player does '/block' and has the correct permission
 */

package io.github.HctiMitcH;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GemsToBlocks extends JavaPlugin {
	
	File configFile;
	FileConfiguration config;
	List<String> itemsList;
	Map<String, Long> times = new HashMap<String, Long>();
	
	@Override
    public void onEnable(){
		configFile = new File(getDataFolder(), "config.yml");
		try{
			firstRun();
		} catch (Exception e){
			e.printStackTrace();
		}
		config = new YamlConfiguration();
	    loadYamls();
	    itemsList = config.getStringList("config.items");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "|GemsToBlocks| Version 1.2 CraftBukkit 1.7.2 R0.3 by" + ChatColor.UNDERLINE + "HctiMitcH");
    }
	
	private void firstRun() throws Exception {
	    if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadYamls() {
	    try {
	        config.load(configFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@Override
    public void onDisable() {
		
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("block")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
			} else {
				Player player = (Player) sender;
				if(player.hasPermission("gemstoblocks.block")){
					if(!times.containsKey(player.getName())){
						times.put(player.getName(), System.currentTimeMillis() / 1000L - 60);
					}
					if(times.get(player.getName()) + 5 < System.currentTimeMillis() / 1000L){
						Inventory inv = player.getInventory();
						ItemStack items[] = inv.getContents();
						boolean itemChanged = false;
						int itemCount = 0;
						int totalBlock = 0;
						int totalGem = 0;
						for(String item : itemsList){
							itemCount = 0;
							for(ItemStack stack : items){
								if(stack != null){
									if(item.equalsIgnoreCase("gold") || item.equalsIgnoreCase("iron")){
										if(stack.getType() == Material.getMaterial(item + "_INGOT")){
											itemCount += stack.getAmount();
											inv.remove(stack);
										}
									}else{
										if(stack.getType() == Material.getMaterial(item)){
											itemCount += stack.getAmount();
											inv.remove(stack);
										}
									}
								}
							}
							if(inv.firstEmpty() != -1){
								if(itemCount / 9 > 0){
									itemChanged = true;
									totalBlock += itemCount / 9;
									inv.addItem(new ItemStack(Material.getMaterial(item + "_BLOCK"), itemCount / 9));
								}
								if(inv.firstEmpty() != -1){
									if(itemCount % 9 > 0){
										if(item.equalsIgnoreCase("gold") || item.equalsIgnoreCase("iron")){
											inv.addItem(new ItemStack(Material.getMaterial(item + "_INGOT"), itemCount % 9));
										}else{
											inv.addItem(new ItemStack(Material.getMaterial(item), itemCount % 9));
										}
									}
								}
							}else{
								if(itemCount / 9 > 0){
									player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.getMaterial(item + "_BLOCK"), itemCount / 9));
									itemChanged = true;
								}
								if(itemCount % 9 > 0){
									player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.getMaterial(item), itemCount % 9));
								}
							}
							totalGem += itemCount;
						}
						if(!itemChanged){
							player.sendMessage(ChatColor.RED + "You do not have enought items!");
						}else{
							sender.sendMessage(ChatColor.GREEN + "You crafted: " + totalGem + " items into " + totalBlock + " blocks.");
							player.playSound(player.getLocation(), Sound.FIZZ, 10, 1);
						}
						times.put(player.getName(), System.currentTimeMillis() / 1000L);
					}else{
						player.sendMessage(ChatColor.RED + "You must wait: " + (5 - ((System.currentTimeMillis() / 1000L) - times.get(player.getName()))) + " seconds!");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You Do Not Have Permission!");
				}
			}
			return true;
		}
		return false;
	}
}
