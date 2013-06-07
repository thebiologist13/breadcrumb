package com.github.thebiologist13.breadcrumb;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class BreadcrumbListener implements Listener {

	private final Breadcrumb PLUGIN;
	private final FileConfiguration CONFIG;
	
	public BreadcrumbListener(Breadcrumb plugin) {
		this.PLUGIN = plugin;
		this.CONFIG = plugin.getConfig();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent ev) {
		Player p = ev.getPlayer();
		boolean on = Breadcrumb.mode.get(p);
		
		if(!on)
			return;
		
		Location last = Breadcrumb.lastCrumb.get(p);
		
		if(last != null) {
			double distance = CONFIG.getDouble("distance", 5.0D);
			
			if(last.distanceSquared(p.getLocation()) > Math.pow(distance, 2)) {
				
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDespawn(ItemDespawnEvent ev) {
		
	}

}
