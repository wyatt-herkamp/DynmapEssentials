package me.kingtux.dynmapessentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.InvalidWorldException;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public final class DynmapEssentials extends JavaPlugin implements Runnable, Listener {
    private Warps essentialsWarps;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;

    @Override
    public void onEnable() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.isPluginEnabled("Essentials")) {
            essentialsWarps = ((Essentials) pluginManager.getPlugin("Essentials")).getWarps();
        } else {
            getLogger().severe("Plugin Can not load without Essentials");
            pluginManager.disablePlugin(this);
        }
        if (pluginManager.isPluginEnabled("dynmap")) {
            dynmapAPI = (DynmapAPI) pluginManager.getPlugin("dynmap");
        } else {
            getLogger().severe("Plugin Can not load without Essentials");
            pluginManager.disablePlugin(this);
        }
        markerAPI = dynmapAPI.getMarkerAPI();
        Metrics metrics = new Metrics(this, 9786);
        //Rerun this every minute
        getServer().getScheduler().runTaskTimer(this, this, 0, 1200);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void run() {
        MarkerSet markerSet = markerAPI.getMarkerSet("Essentials_Warps");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("Essentials_Warps", "Warps", null, false);
        }
        for (String s : essentialsWarps.getList()) {
            Location location;
            try {
                location = essentialsWarps.getWarp(s);
            } catch (WarpNotFoundException | InvalidWorldException e) {
                e.printStackTrace();
                continue;
            }
            if (markerSet.findMarkerByLabel(s) != null) {
                //Update the Location
                markerSet.findMarkerByLabel(s).setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
            } else {
                Marker marker = markerSet.createMarker(null, s, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), markerAPI.getMarkerIcon("pin"), false);
                marker.setLabel(s);
                marker.setMarkerIcon(markerAPI.getMarkerIcon("pin"));
                marker.setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
            }
        }
    }
}
