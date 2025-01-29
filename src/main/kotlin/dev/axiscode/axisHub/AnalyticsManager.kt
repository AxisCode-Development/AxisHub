package dev.axiscode.axisHub

import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.logging.Level

class AnalyticsManager(private val plugin: AxisHub) {

    var totalTeleports = 0
    var totalReloads = 0

    val hubUsageCounts = mutableMapOf<String, Int>()

    private val dataFile: File
        get() = File(plugin.dataFolder, "analytics.yml")

    fun load() {
        if (!dataFile.exists()) {
            plugin.logger.info("[AxisHub] No analytics.yml found, starting fresh counters.")
            return
        }

        try {
            val config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(dataFile)
            totalTeleports = config.getInt("teleports", 0)
            totalReloads = config.getInt("reloads", 0)
            val hubsSection = config.getSection("hubUsage")
            hubsSection?.keys?.forEach { hubName ->
                hubUsageCounts[hubName] = hubsSection.getInt(hubName, 0)
            }
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "[AxisHub] Could not load analytics.yml", ex)
        }
    }

    fun save() {
        try {
            val config = Configuration()
            config.set("teleports", totalTeleports)
            config.set("reloads", totalReloads)

            val hubSection = Configuration()
            for ((hubName, usageCount) in hubUsageCounts) {
                hubSection.set(hubName, usageCount)
            }
            config.set("hubUsage", hubSection)

            ConfigurationProvider.getProvider(YamlConfiguration::class.java).save(config, dataFile)
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "[AxisHub] Could not save analytics.yml", ex)
        }
    }

    fun incrementTeleport() {
        totalTeleports++
    }

    fun incrementReload() {
        totalReloads++
    }

    fun incrementHubUsage(hubName: String) {
        hubUsageCounts[hubName] = (hubUsageCounts[hubName] ?: 0) + 1
    }

    fun getHubUsageCount(hubName: String): Int {
        return hubUsageCounts[hubName] ?: 0
    }
}
