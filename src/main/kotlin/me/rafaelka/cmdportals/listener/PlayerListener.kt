package me.rafaelka.cmdportals.listener

import me.rafaelka.cmdportals.config.messages
import me.rafaelka.cmdportals.config.portals
import me.rafaelka.cmdportals.config.settings
import me.rafaelka.cmdportals.plugin
import me.rafaelka.cmdportals.util.Permissions
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class PlayerListener : Listener {

    val selections = mutableMapOf<UUID, Selection>()
    val inPortals = mutableMapOf<UUID, MutableList<String>>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (!inPortals.containsKey(player.uniqueId)) {
            inPortals[player.uniqueId] = mutableListOf()
        }

        for (portal in portals.portals) {
            if (portal.isIn(player.location)) {
                if (!inPortals[player.uniqueId]!!.contains(portal.id)) {
                    inPortals[player.uniqueId]!!.add(portal.id)
                    portal.playerIn(player)
                }
            } else {
                inPortals[player.uniqueId]!!.remove(portal.id)
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item
        if (item != null && item.isSimilar(settings.wand) && player.hasPermission(Permissions.WAND) && event.clickedBlock != null) {
            event.isCancelled = true
            val selection = selections[player.uniqueId] ?: Selection(null, null)
            if (event.action.isLeftClick) {
                if (event.clickedBlock!!.location != selection.pos1) {
                    val pos = event.clickedBlock!!.location
                    selection.pos1 = pos
                    selections[player.uniqueId] = selection
                    player.sendRichMessage(
                        messages.commands.wandPos1,
                        Placeholder.unparsed("x", pos.x.toInt().toString()),
                        Placeholder.unparsed("y", pos.y.toInt().toString()),
                        Placeholder.unparsed("z", pos.z.toInt().toString()),
                    )
                } else {
                    player.sendRichMessage(messages.errors.alreadySelected)
                }
            } else if (event.action.isRightClick) {
                if (event.clickedBlock?.location != selection.pos2) {
                    val pos = event.clickedBlock!!.location
                    selection.pos2 = pos
                    selections[player.uniqueId] = selection
                    player.sendRichMessage(
                        messages.commands.wandPos2,
                        Placeholder.unparsed("x", pos.x.toInt().toString()),
                        Placeholder.unparsed("y", pos.y.toInt().toString()),
                        Placeholder.unparsed("z", pos.z.toInt().toString()),
                    )
                } else {
                    player.sendRichMessage(messages.errors.alreadySelected)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        selections.remove(event.player.uniqueId)
    }

    data class Selection(var pos1: Location?, var pos2: Location?) {
        fun isComplete(): Boolean {
            return pos1 != null && pos2 != null
        }
    }
}
