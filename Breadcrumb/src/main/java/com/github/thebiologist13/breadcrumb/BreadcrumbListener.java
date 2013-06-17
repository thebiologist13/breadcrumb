package com.github.thebiologist13.breadcrumb;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class BreadcrumbListener implements Listener {

	private final FileConfiguration CONFIG;
	private final String DISPLAY_NAME = "Bread Crumb";
	private final String LORE = "A piece of bread.";
	
	public BreadcrumbListener(Breadcrumb plugin) {
		this.CONFIG = plugin.getConfig();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent ev) {
		Player p = ev.getPlayer();
		boolean on = Breadcrumb.mode.containsKey(p) ? Breadcrumb.mode.get(p) : false;
		
		if(!on)
			return;
		
		Location last = Breadcrumb.lastCrumb.get(p);
		
		if(last == null) {
			dropCrumb(p);
		} else {
			double distance = CONFIG.getDouble("distance", 5.0D);
			
			if(last.distanceSquared(p.getLocation()) > Math.pow(distance, 2)) {
				dropCrumb(p);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDespawn(ItemDespawnEvent ev) {
		Item i = ev.getEntity();
		int ticksToLive = CONFIG.getInt("despawn", 10) * 1200;
		if(isCrumb(i.getItemStack()) && i.getTicksLived() < ticksToLive) {
			ev.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onItemPickup(PlayerPickupItemEvent ev) {
		Player p = ev.getPlayer();
		ItemStack stack = ev.getItem().getItemStack();
		if(isCrumb(stack)) {
			if(Breadcrumb.crumbs.containsKey(p)) {
				Breadcrumb.crumbs.replace(p, Breadcrumb.crumbs.get(p) + stack.getAmount());
			} else {
				Breadcrumb.crumbs.put(p, stack.getAmount());
			}
			ev.setCancelled(true);
			ev.getItem().remove();
		}
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent ev) {
		Player p = ev.getPlayer();
		Breadcrumb.mode.remove(p);
		Breadcrumb.crumbs.remove(p);
		Breadcrumb.lastCrumb.remove(p);
	}

	private void dropCrumb(Player p) {
		boolean needsBread = CONFIG.getBoolean("bread.required", true);
		boolean hasBread = p.getInventory().contains(Material.BREAD);
		boolean hasCrumbs = Breadcrumb.crumbs.get(p) != null 
				&& (Breadcrumb.crumbs.get(p) > 0);
		int crumbs = CONFIG.getInt("bread.crumbs", 8);
		
		Location loc = p.getLocation();
		
		if(needsBread) {
			if(hasCrumbs) {
				Breadcrumb.crumbs.replace(p, Breadcrumb.crumbs.get(p) - 1);
				spawnCrumb(loc);
			} else if(hasBread) {
				Breadcrumb.crumbs.put(p, crumbs - 1);
				PlayerInventory inv = p.getInventory();
				int slot = inv.first(Material.BREAD);
				ItemStack breadStack = inv.getItem(slot);
				int amount = breadStack.getAmount();
				if(amount == 1) {
					inv.setItem(slot, new ItemStack(Material.AIR));
				} else {
					breadStack.setAmount(amount - 1);
					inv.setItem(slot, breadStack);
				}
				spawnCrumb(loc);
			}
		} else {
			spawnCrumb(loc);
		}
		
		Breadcrumb.lastCrumb.put(p, loc);
	}
	
	private boolean isCrumb(ItemStack i) {
		ItemMeta meta = i.getItemMeta();
		if(meta == null)
			return false;
		
		String name = meta.getDisplayName();
		List<String> lore = meta.getLore();
		
		if(name == null || name.isEmpty() || lore == null || lore.isEmpty())
			return false;
		
		System.out.println("DISPLAY_NAME: " + DISPLAY_NAME);
		System.out.println("LORE: " + LORE);
		System.out.println("Meta name: " + name);
		System.out.println("Meta lore: " + lore.toString());
		
		boolean hasLore = false;
		for(String s : lore) {
			if(s.equals(LORE)) {
				hasLore = true;
				break;
			}
		}
		
		boolean hasName = name.equals(DISPLAY_NAME);
		
		System.out.println("Has Lore: " + hasLore);
		System.out.println("Has Name: " + hasName);
		
		if(hasName && hasLore) {
			return true;
		} else {
			return false;
		}
	}
	
	private Item spawnCrumb(Location l) {
		ItemStack aCrumb = new ItemStack(Material.BAKED_POTATO);
		aCrumb.setAmount(1);
		ItemMeta meta = aCrumb.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add(LORE);
		meta.setDisplayName(DISPLAY_NAME);
		meta.setLore(lore);
		aCrumb.setItemMeta(meta);
		
		Item i = l.getWorld().dropItem(l, aCrumb);
		return i;
	}
	
}
