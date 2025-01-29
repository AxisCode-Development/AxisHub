package dev.axiscode.axisHub

import java.util.*
import net.md_5.bungee.api.connection.ProxiedPlayer

class CooldownManager(private val plugin: AxisHub) {

    val cooldowns: MutableMap<UUID, Long> = mutableMapOf()

    fun isOnCooldown(player: ProxiedPlayer): Boolean {
        val now = System.currentTimeMillis()
        val end = cooldowns[player.uniqueId] ?: 0
        return now < end
    }

    fun setCooldown(player: ProxiedPlayer) {
        val durationMillis = plugin.configManager.cooldownDurationSeconds * 1000
        cooldowns[player.uniqueId] = System.currentTimeMillis() + durationMillis
    }
}
