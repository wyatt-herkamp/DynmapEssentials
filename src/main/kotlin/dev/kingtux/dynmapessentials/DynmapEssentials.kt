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

class DynmapEssentials : JavaPlugin(), Runnable, Listener {
    private lateinit var essentialsWarps: Warps;
    private lateinit var  essentials: Essentials
    private lateinit var  dynmapAPI: DynmapAPI
    private lateinit var  markerAPI: MarkerAPI

    override fun onEnable() {
        val pluginManager = Bukkit.getPluginManager()
        if (pluginManager.isPluginEnabled("Essentials")) {
            val essentialsPlugin = pluginManager.getPlugin("Essentials");
            if (essentialsPlugin == null){
                pluginManager.disablePlugin(this)
                return;
            }
            essentials = essentialsPlugin as Essentials
            essentialsWarps = essentials.warps;
        } else {
            logger.severe("Plugin Can not load without Essentials")
            pluginManager.disablePlugin(this)
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
        }
        saveDefaultConfig()
        config.addDefault("show-homes", false)
        config.addDefault("show-warps", true)
        config.addDefault("home.show-offline", false)
        config.addDefault("home.format", "{name}: {home}")
        saveConfig()
        markerAPI = dynmapAPI.markerAPI
        val metrics = Metrics(this, 9786)
        //Rerun this every minute
        server.scheduler.runTaskTimer(this, this, 0, config.getInt("refresh-rate", 1200).toLong())
    }

    override fun onDisable() {
    }

    override fun run() {
        if (config.getBoolean("show-warps")) {
            runWarps()
        }
        if (config.getBoolean("show-homes")) {
            runHomes()
        }
    }

    private fun runWarps() {
        var markerSet = markerAPI.getMarkerSet("Essentials_Warps")
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("Essentials_Warps", "Warps", null, false)
        }
        for (s in essentialsWarps.list) {
            var location: Location
            try {
                location = essentialsWarps.getWarp(s)
            } catch (e: WarpNotFoundException) {
                logger.warning("WarpNotFound but is in list. ${e.message}")
                continue
            } catch (e: InvalidWorldException) {
                logger.warning("Invalid World: ${e.message}")
                continue
            }
            if (markerSet.findMarkerByLabel(s) != null) {
                //Update the Location
                markerSet.findMarkerByLabel(s).setLocation(location.world!!.name, location.x, location.y, location.z)
            } else {
                val icon = markerAPI.getMarkerIcon(config.getString("warp.marker", "pin"))
                val marker = markerSet.createMarker(
                    null,
                    s,
                    location.world!!.name,
                    location.x,
                    location.y,
                    location.z,
                    icon,
                    false
                )
                marker.label = s
                marker.setMarkerIcon(icon)
                marker.setLocation(location.world!!.name, location.x, location.y, location.z)
            }
        }
    }

    private fun runHomes() {
        var markerSet = markerAPI.getMarkerSet("Essentials_Homes")
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("Essentials_Homes", "Homes", null, false)
        }

        for (uuid in essentials.userMap.allUniqueUsers) {
            val user = essentials.getUser(uuid)
            val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
            if (!offlinePlayer.isOnline && !config.getBoolean("home.show-offline")) continue
            val homes = user.homes
            for (home in homes) {
                val homeName = config.getString("home.format")!!
                    .replace("{name}", offlinePlayer.name!!).replace("{home}", home)
                try {
                    val location = user.getHome(home)

                    if (markerSet.findMarkerByLabel(homeName) != null) {
                        //Update the Location
                        markerSet.findMarkerByLabel(homeName)
                            .setLocation(location.world!!.name, location.x, location.y, location.z)
                    } else {
                        val icon = markerAPI.getMarkerIcon(config.getString("home.marker", "pin"))
                        val marker = markerSet.createMarker(
                            null, homeName, location.world!!
                                .name, location.x, location.y, location.z, icon, false
                        )
                        marker.label = homeName
                        marker.setMarkerIcon(icon)
                        marker.setLocation(location.world!!.name, location.x, location.y, location.z)
                    }
                } catch (e: Exception) {
                    logger.warning("Unknown Error with $homeName")
                    logger.warning(e.message)
                }
            }
        }
    }
}
