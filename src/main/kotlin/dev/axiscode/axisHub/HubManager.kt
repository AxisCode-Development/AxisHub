package dev.axiscode.axisHub

import net.md_5.bungee.api.ProxyServer
import kotlin.random.Random

class HubManager(private val plugin: AxisHub) {

    fun pickBestHub(): String? {
        val config = plugin.configManager
        val proxy = ProxyServer.getInstance()
        val validHubs = config.hubs.filter { proxy.getServerInfo(it) != null }
        if (validHubs.isEmpty()) return null

        val hubCounts = validHubs.associateWith {
            proxy.getServerInfo(it)?.players?.size ?: Int.MAX_VALUE
        }

        val minCount = hubCounts.values.minOrNull() ?: return null
        val leastPopulated = hubCounts.filterValues { it == minCount }.keys
        return if (leastPopulated.size == 1) {
            leastPopulated.first()
        } else {
            leastPopulated.random(Random(System.nanoTime()))
        }
    }
}
