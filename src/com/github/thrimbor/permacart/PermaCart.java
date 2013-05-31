package com.github.thrimbor.permacart;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PermaCart extends JavaPlugin implements Listener {
	private int radius;
	private boolean keepStationaryCartsLoaded;
	
	public void onEnable () {
		this.radius = this.getConfig().getInt("radius");
		this.keepStationaryCartsLoaded = this.getConfig().getBoolean("keep_stationary_carts_loaded");
		
		getServer().getPluginManager().registerEvents(this, this);		
		
		getLogger().info(this.getDescription().getName() + " is now enabled");
	}
	
	public void onDisable () {
		getLogger().info(this.getDescription().getName() + " is now disabled");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove (VehicleMoveEvent event) {
		if (event.getVehicle() instanceof Minecart) {
			Chunk chunk = event.getVehicle().getLocation().getBlock().getChunk();
			World world = chunk.getWorld();
			int x = chunk.getX();
			int z = chunk.getZ();
			
			for (int xr = x-radius; xr <= x+radius; xr++)
				for (int zr = z-radius; zr <= z+radius; zr++)
					if (!world.isChunkLoaded(xr, zr))
						world.loadChunk(xr, zr);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUnload (ChunkUnloadEvent event) {
		int x = event.getChunk().getX();
		int z = event.getChunk().getZ();
		World world = event.getWorld();
		
		for (int xr = x-radius; xr <= x+radius; xr++)
			for (int zr = z-radius; zr <= z+radius; zr++)
				if (world.isChunkLoaded(xr, zr))
					for (Entity entity : world.getChunkAt(xr, zr).getEntities())
						if (entity instanceof Minecart)
							if ((((Minecart)entity).getVelocity().length() > 0) || this.keepStationaryCartsLoaded) {
								event.setCancelled(true);
								return;
							}
	}

}
