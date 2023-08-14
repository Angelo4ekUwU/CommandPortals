package me.rafaelka.cmdportals

import me.rafaelka.cmdportals.command.registerCommands
import me.rafaelka.cmdportals.config.loadSettings
import me.rafaelka.cmdportals.listener.PlayerListener
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger

lateinit var plugin: Main
lateinit var logger: Logger
lateinit var playerListener: PlayerListener

class Main : JavaPlugin() {

    override fun onEnable() {
        plugin = this
        me.rafaelka.cmdportals.logger = slF4JLogger
        playerListener = PlayerListener()
        reload()

        server.pluginManager.registerEvents(playerListener, this)
        registerCommands()
    }

    fun reload() {
        server.globalRegionScheduler.cancelTasks(this)
        loadSettings(dataFolder.toPath())
    }

    override fun onDisable() {
        if (::playerListener.isInitialized) {
            playerListener.selections.clear()
            playerListener.inPortals.clear()
        }
    }
}
