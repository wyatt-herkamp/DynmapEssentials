package me.kingtux.dynmapessentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Warps;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.InvalidWorldException;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.List;

public final class DynmapEssentials extends JavaPlugin implements Runnable, Listener {
    private Warps essentialsWarps;
    private Essentials essentials;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;

    @Override
    public void onEnable() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.isPluginEnabled("Essentials")) {
            essentials = ((Essentials) pluginManager.getPlugin("Essentials"));
            essentialsWarps = essentials.getWarps();
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
        saveDefaultConfig();
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
        if (getConfig().getBoolean("show-warps")) {
            runWarps();
        }
        if (getConfig().getBoolean("show-homes")) {
            runHomes();
        }
    }

    private void runWarps() {
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

    private void runHomes() {
        MarkerSet markerSet = markerAPI.getMarkerSet("Essentials_Homes");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("Essentials_Homes", "Homes", null, false);
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            User user = essentials.getUser(onlinePlayer);
            List<String> homes = user.getHomes();
            for (String home : homes) {
                String homeName = new StringBuilder(onlinePlayer.getName()).append(": ").append(home).toString();
                try {
                    Location location = user.getHome(home);

                    if (markerSet.findMarkerByLabel(homeName) != null) {
                        //Update the Location
                        markerSet.findMarkerByLabel(homeName).setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
                    } else {
                        Marker marker = markerSet.createMarker(null, homeName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), markerAPI.getMarkerIcon("pin"), false);
                        marker.setLabel(homeName);
                        marker.setMarkerIcon(markerAPI.getMarkerIcon("bed"));
                        marker.setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
