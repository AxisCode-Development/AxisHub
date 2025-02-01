package dev.axiscode.axisHub

import jdk.internal.platform.Metrics
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager
import java.util.logging.Level


class AxisHub : Plugin() {

    lateinit var configManager: ConfigManager
        private set

    var analyticsManager: AnalyticsManager? = null
        private set

    lateinit var cooldownManager: CooldownManager
        private set

    lateinit var hubManager: HubManager
        private set

    override fun onEnable() {

        configManager = ConfigManager(this).apply { load() }

        if (configManager.enableAnalytics) {
            analyticsManager = AnalyticsManager(this).apply { load() }
            logger.info("[AxisHub] Analytics enabled and loaded.")
        } else {
            analyticsManager = null
            logger.info("[AxisHub] Analytics disabled by config.")
        }

        cooldownManager = CooldownManager(this)

        hubManager = HubManager(this)

        val pluginManager: PluginManager = proxy.pluginManager
        pluginManager.registerCommand(this, HubCommand(this))

        logger.log(Level.INFO, "[AxisHub] Plugin enabled.")
    }

    override fun onDisable() {
        if (configManager.enableAnalytics) {
            analyticsManager?.save()
            logger.log(Level.INFO, "[AxisHub] Analytics data saved.")
        }
        logger.log(Level.INFO, "[AxisHub] Plugin disabled.")
    }

    fun sendMessage(player: ProxiedPlayer, message: String, placeholders: Map<String, String> = emptyMap()) {
        val replaced = replacePlaceholders(message, placeholders)
        player.sendMessage(*net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(replaced)))
    }

    private fun colorize(message: String): String {
        return message.replace("&", "§")
    }

    private fun replacePlaceholders(message: String, placeholders: Map<String, String>): String {
        var result = message
        placeholders.forEach { (k, v) ->
            result = result.replace("%$k%", v)
        }
        return result
    }

    fun getRemainingCooldownSeconds(player: ProxiedPlayer): Long {
        val now = System.currentTimeMillis()
        val end = cooldownManager.cooldowns[player.uniqueId] ?: 0
        return if (end > now) (end - now) / 1000 else 0
    }

}
