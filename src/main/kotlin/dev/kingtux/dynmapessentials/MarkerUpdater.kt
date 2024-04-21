package dev.kingtux.dynmapessentials

import com.earth2me.essentials.Essentials
import com.earth2me.essentials.Warps
import com.earth2me.essentials.commands.WarpNotFoundException
import net.ess3.api.InvalidWorldException
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.dynmap.DynmapAPI
import org.dynmap.markers.MarkerAPI
data class MarkerConfig(
    val showHomes: Boolean,
    val showWarps: Boolean,
    val showOffline: Boolean,
    val homeFormat: String,
    val homeMarker: String,
    val warpMarker: String
){
    constructor(config: FileConfiguration) : this(
        config.getBoolean("show-homes"),
        config.getBoolean("show-warps"),
        config.getBoolean("home.show-offline"),
        config.getString("home.format", "{name}: {home}")!!,
        config.getString("home.marker", "pin")!!,
        config.getString("warp.marker", "pin")!!,
    )
}
class MarkerUpdater(
    private var essentials: Essentials,
    private var dynmapAPI: DynmapAPI,
    private var markerAPI: MarkerAPI,
    private var essentialsWarps: Warps,
    private var plugin: DynmapEssentials,
    private var config: MarkerConfig
) : Runnable {
    constructor(plugin: DynmapEssentials, essentials: Essentials, dynmapAPI: DynmapAPI) : this(
        essentials,
        dynmapAPI,
        dynmapAPI.markerAPI,
        essentials.warps,
        plugin,
        MarkerConfig(plugin.config)
    )

    private fun updateWarps() {
        val icon = markerAPI.getMarkerIcon(config.warpMarker)

        var markerSet = markerAPI.getMarkerSet("Essentials_Warps_V1_3")
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("Essentials_Warps_V1_3", "Warps", null, false)
        }
        for (s in essentialsWarps.list) {
            var location: Location
            try {
                location = essentialsWarps.getWarp(s)
            } catch (e: WarpNotFoundException) {
                plugin.logger.warning("WarpNotFound but is in list. ${e.message}")
                continue
            } catch (e: InvalidWorldException) {
                plugin.logger.warning("Invalid World: ${e.message}")
                continue
            }
            val markerId = "${s}_warp"
            if (markerSet.findMarker(markerId) != null) {
                //Update the Location
                markerSet.findMarker(markerId).let {
                    it.setLocation(location.world!!.name, location.x, location.y, location.z)
                    it.markerIcon = icon
                }
            } else {
                 markerSet.createMarker(
                    markerId,
                    s,
                    location.world!!.name,
                    location.x,
                    location.y,
                    location.z,
                    icon,
                    false
                )
            }
        }
    }

    private fun updateHomes() {
        val icon = markerAPI.getMarkerIcon(config.homeMarker)
        var markerSet = markerAPI.getMarkerSet("Essentials_Homes_V1_3")
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("Essentials_Homes_V1_3", "Homes", null, false)
        }
        for (uuid in essentials.userMap.allUniqueUsers) {
            val user = essentials.getUser(uuid) ?: continue
            val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
            if (!offlinePlayer.isOnline && !config.showOffline) continue
            val homes = user.homes
            for (home in homes) {
                val homeId = "${offlinePlayer.uniqueId}_$home"
                val homeName = config.homeFormat
                    .replace("{name}", offlinePlayer.name!!).replace("{home}", home)
                val location = runCatching { user.getHome(home) }.getOrNull();
                if (location == null) {
                    plugin.logger.warning("Home not found but is in list. ${user.name} $home")
                    continue
                }
                if (markerSet.findMarker(homeId) != null) {
                    //Update the Location
                    markerSet.findMarker(homeId).let {
                        it.setLocation(location.world!!.name, location.x, location.y, location.z)
                        it.markerIcon = markerAPI.getMarkerIcon(config.homeMarker)
                    }
                } else {
                    markerSet.createMarker(
                        homeId, homeName, location.world!!
                            .name, location.x, location.y, location.z, icon, false
                    )
                }

            }
        }
    }

    override fun run() {
        if (config.showWarps) {
            updateWarps()
        }
        if (config.showHomes) {
            updateHomes()
        }
    }
}