package me.rafaelka.cmdportals.portal

import me.rafaelka.cmdportals.config.messages
import me.rafaelka.cmdportals.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.math.max
import kotlin.math.min

@ConfigSerializable
class Portal() {
    lateinit var id: String
    lateinit var pos1: Location
    lateinit var pos2: Location
    var permission: String? = null
    var actions: MutableMap<Int, String> = mutableMapOf()

    constructor(id: String, pos1: Location, pos2: Location) : this() {
        this.id = id
        this.pos1 = pos1
        this.pos2 = pos2
    }

    fun isIn(loc: Location): Boolean {
        return loc.getWorld().name == pos1.getWorld().name
                && loc.blockX >= min(pos1.blockX.toDouble(), pos2.blockX.toDouble())
                && loc.blockX <= max(pos1.blockX.toDouble(), pos2.blockX.toDouble())
                && loc.blockY >= min(pos1.blockY.toDouble(), pos2.blockY.toDouble())
                && loc.blockY <= max(pos1.blockY.toDouble(), pos2.blockY.toDouble())
                && loc.blockZ >= min(pos1.blockZ.toDouble(), pos2.blockZ.toDouble())
                && loc.blockZ <= max(pos1.blockZ.toDouble(), pos2.blockZ.toDouble())
    }

    fun playerIn(player: Player) {
        if (actions.isEmpty()) return
        if (permission != null && !player.hasPermission(permission!!)) {
            player.sendRichMessage(messages.errors.noPermissionPortal)
            return
        }

        Bukkit.getGlobalRegionScheduler().execute(plugin) {
            actions.values.forEach {
                val action = it.replace("%player%", player.name)
                if (action.startsWith("[player] ")) {
                    player.performCommand(action.substring(8))
                } else if (action.startsWith("[console] ")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.substring(10))
                } else if (action.startsWith("[message] ")) {
                    player.sendRichMessage(action.substring(10))
                }
            }
        }
    }
}
