package dev.axiscode.axisHub

import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.logging.Level

class ConfigManager(private val plugin: AxisHub) {

    private val dataFolder: File = plugin.dataFolder
    private lateinit var configuration: Configuration

    var enableAnalytics: Boolean = true

    var hubs: List<String> = listOf("default")
    var disabledServers: List<String> = emptyList()

    var requiresPermission: Boolean = false

    var alreadyInHubMessage: String = "&cYou are already connected to the Hub!"
    var disabledServersError: String = "&cYou can't do /hub here!"
    var hubDoesNotExistMessage: String = "&cThe configured hub server (%server%) doesn't exist!"
    var sendingToHubMessage: String = "&aSending you to the hub: %server%"
    var reloadCompleteMessage: String = "&aConfiguration reloaded."
    var reloadNoPermissionMessage: String = "&cYou don't have permission to reload this plugin!"
    var cooldownActiveMessage: String = "&cYou must wait %cooldown% seconds before using /hub again."

    var cooldownDurationSeconds: Long = 5

    fun load() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        val configFile = File(dataFolder, "config.yml")

        if (!configFile.exists()) {
            plugin.logger.info("[AxisHub] Creating default config.yml.")
            plugin.getResourceAsStream("config.yml")?.use { input ->
                configFile.outputStream().use { output -> input.copyTo(output) }
            } ?: createDefaultConfig(configFile)
        }

        configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
            .load(configFile)

        enableAnalytics = configuration.getBoolean("enableAnalytics", true)

        hubs = configuration.getStringList("hubs")?.takeIf { it.isNotEmpty() }
            ?: listOf(configuration.getString("hub", "default"))
        disabledServers = configuration.getStringList("disabled-servers") ?: emptyList()
        requiresPermission = configuration.getBoolean("requiresPermission", false)

        alreadyInHubMessage = configuration.getString("alreadyInHub", alreadyInHubMessage)
        disabledServersError = configuration.getString("disabledServersError", disabledServersError)
        hubDoesNotExistMessage = configuration.getString("hubDoesNotExistMessage", hubDoesNotExistMessage)
        sendingToHubMessage = configuration.getString("sendingToHubMessage", sendingToHubMessage)
        reloadCompleteMessage = configuration.getString("reloadCompleteMessage", reloadCompleteMessage)
        reloadNoPermissionMessage = configuration.getString("reloadNoPermissionMessage", reloadNoPermissionMessage)
        cooldownActiveMessage = configuration.getString("cooldownActiveMessage", cooldownActiveMessage)

        cooldownDurationSeconds = configuration.getLong("cooldownDurationSeconds", 5)

        plugin.logger.log(Level.INFO, "[AxisHub] Configuration loaded.")
    }

    fun reload() {
        load()
    }

    private fun createDefaultConfig(file: File) {
        try {
            if (file.createNewFile()) {
                file.writeText(
                    """
                    # AxisHub Configuration (edit as needed)

                    # Enable or disable analytics
                    enableAnalytics: true

                    # Multiple hubs for load balancing
                    hubs:
                    - "hub1"    
                    - "hub2"

                    # Require permission "axishub.command" for /hub
                    requiresPermission: false

                    # Servers where /hub is disabled
                    disabled-servers: []

                    # Cooldown (seconds) before using /hub again
                    cooldownDurationSeconds: 5

                    # Messages
                    alreadyInHub: "&cYou are already connected to this server!"
                    disabledServersError: "&cYou can't do /hub here!"
                    hubDoesNotExistMessage: "&cThe configured hub server (%server%) doesn't exist!"
                    sendingToHubMessage: "&aSending you to the hub: %server%"
                    reloadCompleteMessage: "&aConfiguration reloaded."
                    reloadNoPermissionMessage: "&cYou don't have permission to reload this plugin!"
                    cooldownActiveMessage: "&cYou must wait %cooldown% seconds before using /hub again."
                    """.trimIndent()
                )
            }
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "[AxisHub] Could not create default config.yml", ex)
        }
    }
}
