package me.kingtux.dynmapessentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Warps;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.InvalidWorldException;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.List;
import java.util.UUID;

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
        getConfig().addDefault("show-homes", false);
        getConfig().addDefault("show-warps", true);
        getConfig().addDefault("home.show-offline", false);
        getConfig().addDefault("home.format", "{name}: {home}");
        saveConfig();
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
                MarkerIcon icon = markerAPI.getMarkerIcon(getConfig().getString("warp.marker", "pin"));
                Marker marker = markerSet.createMarker(null, s, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), icon, false);
                marker.setLabel(s);
                marker.setMarkerIcon(icon);
                marker.setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
            }
        }
    }

    private void runHomes() {
        MarkerSet markerSet = markerAPI.getMarkerSet("Essentials_Homes");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("Essentials_Homes", "Homes", null, false);
        }

        for (UUID uuid : essentials.getUserMap().getAllUniqueUsers()) {
            User user = essentials.getUser(uuid);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (!offlinePlayer.isOnline() && !getConfig().getBoolean("home.show-offline")) continue;
            List<String> homes = user.getHomes();
            for (String home : homes) {
                String homeName = getConfig().getString("home.format").replace("{name}", offlinePlayer.getName()).replace("{home}", home);
                try {
                    Location location = user.getHome(home);

                    if (markerSet.findMarkerByLabel(homeName) != null) {
                        //Update the Location
                        markerSet.findMarkerByLabel(homeName).setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
                    } else {
                        MarkerIcon icon = markerAPI.getMarkerIcon(getConfig().getString("home.marker", "pin"));
                        Marker marker = markerSet.createMarker(null, homeName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), icon, false);
                        marker.setLabel(homeName);
                        marker.setMarkerIcon(icon);
                        marker.setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
