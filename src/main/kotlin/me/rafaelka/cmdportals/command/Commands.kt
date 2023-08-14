package me.rafaelka.cmdportals.command

import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import io.papermc.paper.adventure.PaperAdventure
import me.rafaelka.cmdportals.config.messages
import me.rafaelka.cmdportals.config.portals
import me.rafaelka.cmdportals.config.settings
import me.rafaelka.cmdportals.playerListener
import me.rafaelka.cmdportals.plugin
import me.rafaelka.cmdportals.portal.Portal
import me.rafaelka.cmdportals.util.Permissions
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R1.CraftServer
import org.bukkit.entity.Player

fun registerCommands() {
    val dispatcher = (Bukkit.getServer() as CraftServer).server.commands.dispatcher

    dispatcher.register(portalCommand())
}

private fun portalCommand(): LiteralArgumentBuilder<CommandSourceStack> = literal<CommandSourceStack?>("portal")
    .then(literal<CommandSourceStack?>("reload")
        .requires { it.hasPermission(2, Permissions.RELOAD) }
        .executes { ctx ->
            plugin.reload()
            ctx.source.sendSystemMessage(vanilla(messages.commands.reload))
            1
        }
    )
    .then(literal<CommandSourceStack?>("wand")
        .requires { it.hasPermission(2, Permissions.WAND) }
        .executes { ctx ->
            val sender = ctx.source.bukkitSender
            if (sender is Player) {
                sender.inventory.addItem(settings.wand)
                ctx.source.sendSystemMessage(vanilla(messages.commands.wand))
            } else {
                ctx.source.sendSystemMessage(vanilla(messages.errors.onlyForPlayers))
            }
            1
        }
    )
    .then(literal<CommandSourceStack?>("create")
        .requires { it.hasPermission(2, Permissions.CREATE) }
        .then(argument<CommandSourceStack?, String?>("portal_id", word())
            .executes { ctx ->
                val sender = ctx.source.bukkitSender
                if (sender is Player) {
                    val selection = playerListener.selections[sender.uniqueId]
                    if (selection != null && selection.isComplete()) {
                        val id = getString(ctx, "portal_id")
                        val portal = Portal(id, selection.pos1!!, selection.pos2!!)
                        portals.portals.add(portal)
                        portals.save()
                        ctx.source.sendSystemMessage(vanilla(messages.commands.create, Placeholder.unparsed("portal_id", id)))
                    } else {
                        ctx.source.sendSystemMessage(vanilla(messages.errors.noSelection))
                    }
                } else {
                    ctx.source.sendSystemMessage(vanilla(messages.errors.onlyForPlayers))
                }
                1
            }
        )
    )
    .then(literal<CommandSourceStack?>("delete")
        .requires { it.hasPermission(2, Permissions.DELETE) }
        .then(argument<CommandSourceStack?, String?>("portal_id", word())
            .executes { ctx ->
                val sender = ctx.source.bukkitSender
                if (sender is Player) {
                    val id = getString(ctx, "portal_id")
                    portals.portals.removeIf { it.id == id }
                    portals.save()
                    ctx.source.sendSystemMessage(vanilla(messages.commands.delete, Placeholder.unparsed("portal_id", id)))
                } else {
                    ctx.source.sendSystemMessage(vanilla(messages.errors.onlyForPlayers))
                }
                1
            }
            .suggests { ctx, builder ->
                try {
                    val arg = ctx.getArgument("portal_id", String::class.java).lowercase()
                    portals.portals.forEach {
                        if (it.id.startsWith(arg)) builder.suggest(it.id)
                    }
                } catch (ex: IllegalArgumentException) {
                    portals.portals.stream().map(Portal::id).forEach(builder::suggest)
                }
                builder.buildFuture()
            }
        )
    )
    .then(literal<CommandSourceStack?>("actions")
        .then(argument<CommandSourceStack?, String?>("portal_id", word())
            .then(literal<CommandSourceStack?>("add")
                .requires { it.hasPermission(2, Permissions.ACTIONS_ADD) }
                .then(argument<CommandSourceStack?, String?>("action", greedyString())
                    .executes { ctx ->
                        val portalId = getString(ctx, "portal_id")
                        val portal = portals.portals.stream().filter { it.id == portalId }.findFirst().orElse(null)
                        if (portal != null) {
                            val action = getString(ctx, "action")
                            portals.portals.remove(portal)
                            portal.actions[portal.actions.size] = action
                            portals.portals.add(portal)
                            portals.save()
                            ctx.source.sendSystemMessage(
                                vanilla(
                                    messages.commands.actions.add,
                                    Placeholder.unparsed("action", action),
                                    Placeholder.unparsed("action_id", (portal.actions.size - 1).toString()),
                                    Placeholder.unparsed("portal_id", portalId),
                                )
                            )
                        } else {
                            ctx.source.sendSystemMessage(vanilla(messages.errors.portalNotFound))
                        }
                        1
                    }
                )
            )
            .then(literal<CommandSourceStack?>("remove")
                .requires { it.hasPermission(2, Permissions.ACTIONS_REMOVE) }
                .then(argument<CommandSourceStack?, Int?>("action_id", integer())
                    .executes { ctx ->
                        val portalId = getString(ctx, "portal_id")
                        val portal = portals.portals.stream().filter { it.id == portalId }.findFirst().orElse(null)
                        if (portal != null) {
                            val actionId = getInteger(ctx, "action_id")
                            if (portal.actions.containsKey(actionId)) {
                                portals.portals.remove(portal)
                                portal.actions.remove(actionId)
                                portals.portals.add(portal)
                                portals.save()
                                ctx.source.sendSystemMessage(
                                    vanilla(
                                        messages.commands.actions.remove,
                                        Placeholder.unparsed("portal_id", portalId),
                                        Placeholder.unparsed("action_id", portal.actions.size.toString())
                                    )
                                )
                            } else {
                                ctx.source.sendSystemMessage(vanilla(messages.errors.actionNotFound))
                            }
                        } else {
                            ctx.source.sendSystemMessage(vanilla(messages.errors.portalNotFound))
                        }
                        1
                    }
                )
            )
            .then(literal<CommandSourceStack?>("clear")
                .requires { it.hasPermission(2, Permissions.ACTIONS_CLEAR) }
                .executes { ctx ->
                    val portalId = getString(ctx, "portal_id")
                    val portal = portals.portals.stream().filter { it.id == portalId }.findFirst().orElse(null)
                    if (portal != null) {
                        if (portal.actions.isNotEmpty()) {
                            portals.portals.remove(portal)
                            portal.actions.clear()
                            portals.portals.add(portal)
                            portals.save()
                            ctx.source.sendSystemMessage(vanilla(messages.commands.actions.clear))
                        } else {
                            ctx.source.sendSystemMessage(vanilla(messages.errors.noActions))
                        }
                    } else {
                        ctx.source.sendSystemMessage(vanilla(messages.errors.portalNotFound))
                    }
                    1
                }
            )
            .then(literal<CommandSourceStack?>("list")
                .requires { it.hasPermission(2, Permissions.ACTIONS_LIST) }
                .executes { ctx ->
                    val portalId = getString(ctx, "portal_id")
                    val portal = portals.portals.stream().filter { it.id == portalId }.findFirst().orElse(null)
                    if (portal != null) {
                        if (portal.actions.isNotEmpty()) {
                            val msg = vanilla(
                                messages.commands.actions.listHeader,
                                Placeholder.unparsed("portal_id", portalId)
                            ).copy()
                            portal.actions.forEach { id, action ->
                                msg.append(
                                    vanilla(
                                        messages.commands.actions.listEntry,
                                        Placeholder.unparsed("action_id", id.toString()),
                                        Placeholder.unparsed("action", action)
                                    )
                                )
                            }
                            ctx.source.sendSystemMessage(msg)
                        } else {
                            ctx.source.sendSystemMessage(vanilla(messages.errors.noActions))
                        }
                    } else {
                        ctx.source.sendSystemMessage(vanilla(messages.errors.portalNotFound))
                    }
                    1
                }
            )
            .suggests { ctx, builder ->
                try {
                    val arg = ctx.getArgument("portal_id", String::class.java).lowercase()
                    portals.portals.forEach {
                        if (it.id.startsWith(arg)) builder.suggest(it.id)
                    }
                } catch (ex: IllegalArgumentException) {
                    portals.portals.stream().map(Portal::id).forEach(builder::suggest)
                }
                builder.buildFuture()
            }
        )
    )
    .then(literal<CommandSourceStack?>("permission")
        .then(argument<CommandSourceStack?, String?>("portal_id", word())
            .then(literal<CommandSourceStack?>("set")
                .requires { it.hasPermission(2, Permissions.PERMISSION_SET) }
                .then(argument<CommandSourceStack?, String?>("permission", word())
                    .executes { ctx ->
                        val portalId = getString(ctx, "portal_id")
                        val portal = portals.portals.stream().filter { it.id == portalId }.findFirst().orElse(null)
                        if (portal != null) {
                            val permission = getString(ctx, "permission")
                            portals.portals.remove(portal)
                            portal.permission = permission
                            portals.portals.add(portal)
                            portals.save()
                            ctx.source.sendSystemMessage(
                                vanilla(
                                    messages.commands.permission.set,
                                    Placeholder.unparsed("permission", permission),
                                    Placeholder.unparsed("portal_id", portalId)
                                )
                            )
                        } else {
                            ctx.source.sendSystemMessage(vanilla(messages.errors.portalNotFound))
                        }
                        1
                    }
                )
            )
            .then(literal<CommandSourceStack?>("remove")
                .requires { it.hasPermission(2, Permissions.PERMISSION_REMOVE) }
                .executes { ctx ->
                    val portalId = getString(ctx, "portal_id")
                    val portal = portals.portals.stream().filter { it.id == portalId }.findFirst().orElse(null)
                    if (portal != null) {
                        portals.portals.remove(portal)
                        portal.permission = null
                        portals.portals.add(portal)
                        portals.save()
                        ctx.source.sendSystemMessage(
                            vanilla(
                                messages.commands.permission.remove,
                                Placeholder.unparsed("portal_id", portalId)
                            )
                        )
                    } else {
                        ctx.source.sendSystemMessage(vanilla(messages.errors.portalNotFound))
                    }
                    1
                }
            )
            .suggests { ctx, builder ->
                try {
                    val arg = ctx.getArgument("portal_id", String::class.java).lowercase()
                    portals.portals.forEach {
                        if (it.id.startsWith(arg)) builder.suggest(it.id)
                    }
                } catch (ex: IllegalArgumentException) {
                    portals.portals.stream().map(Portal::id).forEach(builder::suggest)
                }
                builder.buildFuture()
            }
        )
    )

private fun vanilla(string: String, vararg tags: TagResolver): Component =
    PaperAdventure.asVanilla(MiniMessage.miniMessage().deserialize(string, *tags))
