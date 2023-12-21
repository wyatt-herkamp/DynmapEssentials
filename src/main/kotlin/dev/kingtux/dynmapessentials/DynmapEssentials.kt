package dev.kingtux.dynmapessentials

import com.earth2me.essentials.Essentials
import com.earth2me.essentials.Warps
import com.earth2me.essentials.commands.WarpNotFoundException
import net.ess3.api.InvalidWorldException
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.dynmap.DynmapAPI
import org.dynmap.markers.MarkerAPI

class DynmapEssentials : JavaPlugin() {


    override fun onEnable() {
        val essentials: Essentials
        val dynmapAPI: DynmapAPI
        val pluginManager = Bukkit.getPluginManager()
        if (pluginManager.isPluginEnabled("Essentials")) {
            val essentialsPlugin = pluginManager.getPlugin("Essentials");
            if (essentialsPlugin == null){
                pluginManager.disablePlugin(this)
                return;
            }
            essentials = essentialsPlugin as Essentials
        } else {
            logger.severe("Plugin Can not load without Essentials")
            pluginManager.disablePlugin(this)
            return;
        }
        if (pluginManager.isPluginEnabled("dynmap")) {
            val dynmapPlugin = pluginManager.getPlugin("dynmap");
            if (dynmapPlugin == null){
                pluginManager.disablePlugin(this)
                return;
            }
            dynmapAPI = dynmapPlugin as DynmapAPI;
        } else {
            logger.severe("Plugin Can not load without Essentials")
            pluginManager.disablePlugin(this)
            return;
        }

        saveDefaultConfig()
        config.addDefault("show-homes", false)
        config.addDefault("show-warps", true)
        config.addDefault("home.show-offline", false)
        config.addDefault("home.format", "{name}: {home}")
        saveConfig()
        // Update From 1.2 to 1.3
        if (dynmapAPI.markerAPI.getMarkerSet("Essentials_Homes") != null) {
            logger.info("Old Marker Set Found. Deleting. New V1.3 uses has Ids")
            dynmapAPI.markerAPI.getMarkerSet("Essentials_Homes").deleteMarkerSet()
        }
        if (dynmapAPI.markerAPI.getMarkerSet("Essentials_Warps") != null) {
            logger.info("Old Marker Set Found. Deleting. New V1.3 uses has Ids")
            dynmapAPI.markerAPI.getMarkerSet("Essentials_Warps").deleteMarkerSet()
        }
        val metrics = Metrics(this, 9786)
        //Rerun this every minute
        server.scheduler.runTaskTimer(this, MarkerUpdater(this, essentials, dynmapAPI), 0, config.getInt("refresh-rate", 1200).toLong())
    }

    override fun onDisable() {
    }



}
