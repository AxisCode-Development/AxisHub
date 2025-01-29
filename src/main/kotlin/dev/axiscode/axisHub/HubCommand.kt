package dev.axiscode.axisHub

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.ChatColor

class HubCommand(private val plugin: AxisHub) :
    Command(
        "hub",
        if (plugin.configManager.requiresPermission) "axishub.command" else null,
        *arrayOf()
    ) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val config = plugin.configManager

        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            handleReload(sender)
            return
        }

        if (sender !is ProxiedPlayer) {
            sender.sendMessage(*TextComponent.fromLegacyText(ChatColor.RED.toString() + "Only players can use this command!"))
            return
        }

        if (plugin.cooldownManager.isOnCooldown(sender)) {
            val secondsLeft = plugin.getRemainingCooldownSeconds(sender)
            plugin.sendMessage(
                sender,
                config.cooldownActiveMessage,
                mapOf(
                    "player" to sender.name,
                    "cooldown" to secondsLeft.toString(),
                    "teleports_count" to (plugin.analyticsManager?.totalTeleports?.toString() ?: "0")
                )
            )
            return
        }

        val currentServerName = sender.server?.info?.name
        if (currentServerName != null && config.disabledServers.contains(currentServerName)) {
            plugin.sendMessage(sender, config.disabledServersError, placeholders(sender, null))
            return
        }

        if (currentServerName != null && config.hubs.any { it.equals(currentServerName, ignoreCase = true) }) {
            plugin.sendMessage(sender, config.alreadyInHubMessage, placeholders(sender, currentServerName))
            return
        }


        val bestHub = plugin.hubManager.pickBestHub()
        if (bestHub == null) {
            plugin.sendMessage(sender, "&cNo valid hub servers found! Check config.")
            return
        }

        val serverInfo = ProxyServer.getInstance().getServerInfo(bestHub)
        if (serverInfo == null) {
            plugin.sendMessage(sender, config.hubDoesNotExistMessage, placeholders(sender, bestHub))
            return
        }

        plugin.sendMessage(sender, config.sendingToHubMessage, placeholders(sender, bestHub))

        if (plugin.configManager.enableAnalytics) {
            plugin.analyticsManager?.incrementTeleport()
            plugin.analyticsManager?.incrementHubUsage(bestHub)
        }

        plugin.cooldownManager.setCooldown(sender)

        sender.connect(serverInfo)
    }

    private fun handleReload(sender: CommandSender) {
        if (sender.hasPermission("axishub.reload")) {
            plugin.configManager.reload()
            if (plugin.configManager.enableAnalytics) {
                plugin.analyticsManager?.incrementReload()
            }

            if (sender is ProxiedPlayer) {
                plugin.sendMessage(sender, plugin.configManager.reloadCompleteMessage)
            } else {
                sender.sendMessage(*TextComponent.fromLegacyText(ChatColor.stripColor(plugin.configManager.reloadCompleteMessage)))
            }
        } else {
            if (sender is ProxiedPlayer) {
                plugin.sendMessage(sender, plugin.configManager.reloadNoPermissionMessage)
            } else {
                sender.sendMessage(*TextComponent.fromLegacyText(ChatColor.stripColor(plugin.configManager.reloadNoPermissionMessage)))
            }
        }
    }

    private fun placeholders(player: ProxiedPlayer, hubName: String?): Map<String, String> {
        val config = plugin.configManager
        val placeholders = mutableMapOf<String, String>(
            "player" to player.name
        )
        val totalTeleports = if (config.enableAnalytics) plugin.analyticsManager?.totalTeleports else 0
        placeholders["teleports_count"] = totalTeleports.toString()

        hubName?.let {
            placeholders["server"] = it
            val usage = if (config.enableAnalytics) plugin.analyticsManager?.getHubUsageCount(it) ?: 0 else 0
            placeholders["hub_usage"] = usage.toString()
        }
        return placeholders
    }
}
