package com.github.thebiologist13.breadcrumb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Breadcrumb extends JavaPlugin {
	
	//Map of how many breadcrumbs a player has. Add to this to generate bread, remove to drop.
	public static ConcurrentHashMap<Player, Short> crumbs = new ConcurrentHashMap<Player, Short>();
	
	//Map of the location of the last last crumb per player
	public static ConcurrentHashMap<Player, Location> lastCrumb = new ConcurrentHashMap<Player, Location>();
	
	//Map of players that the plugin is on for.
	public static ConcurrentHashMap<Player, Boolean> mode = new ConcurrentHashMap<Player, Boolean>();
	
	public FileConfiguration config;
	public Logger log = Logger.getLogger("Minecraft");
	public boolean on = true;
	public String prefix = "";
	
	private File configFile;
	
	@Override
	public void onDisable() {
		log.info("RealisticExplosions v" + getDescription().getVersion() + " has been disabled!");
	}
	
	@Override
	public void onEnable() {
		log.info("RealisticExplosions v" + getDescription().getVersion() + " has been enabled!");
	}
	
	@Override
	public FileConfiguration getConfig() {
		if (config == null) {
			reloadConfig();
		}
		return config;
	}

	public void reloadConfig() {

		if (configFile == null) {
			configFile = new File(getDataFolder(), "config.yml");

			if (!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				copy(getResource("config.yml"), configFile);
			}

		}

		config = YamlConfiguration.loadConfiguration(configFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource("config.yml");
		if (defConfigStream != null) {
			FileConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.options().copyDefaults(true);
			config.setDefaults(defConfig);
		}

	}

	private void copy(InputStream in, File file) {

		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			log.severe("Could not copy config from jar!");
			e.printStackTrace();
		}

	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("breadcrumb")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(mode.containsKey(p))
					mode.replace(p, !mode.get(p));
				else
					mode.put(p, false);
				String message = (mode.get(p)) 
						? config.getString("onMessage", "Leaving a trail of breadcrumbs!")
						: config.getString("offMessage", "No longer leaving behind breadcrumbs!");
				p.sendMessage(ChatColor.GREEN + message);
			} else {
				log.info(prefix + "This command cannot be issued from console.");
			}
			return true;
		}
		return false;
	}
	
}
