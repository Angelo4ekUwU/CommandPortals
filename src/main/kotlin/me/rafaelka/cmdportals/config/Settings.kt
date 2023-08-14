package me.rafaelka.cmdportals.config

import me.rafaelka.cmdportals.logger
import me.rafaelka.cmdportals.portal.Portal
import me.denarydev.crystal.config.BukkitConfigs
import me.denarydev.crystal.config.CrystalConfigs
import me.denarydev.crystal.utils.ItemUtils
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.nio.file.Path

lateinit var settings: Settings
lateinit var portals: Portals
lateinit var messages: Messages

fun loadSettings(path: Path) {
    if (::portals.isInitialized) portals.save()
    settings = CrystalConfigs.loadConfig(path.resolve("settings.conf"), BukkitConfigs.serializers(), Settings::class.java, false)
    messages = CrystalConfigs.loadConfig(path.resolve("messages.conf"), Messages::class.java, false)
    portals = Portals(path.resolve("portals.conf"))
    portals.save()
}

@ConfigSerializable
data class Settings(
    var wand: ItemStack = ItemUtils.itemBuilder()
        .type(Material.STICK)
        .displaynameRich("<gold>Селектор 3000")
        .loreRich(
            listOf(
                "<gray>ЛКМ - первая точка",
                "<gray>ПКМ - вторая точка",
                "<gray>/portal create <id>"
            )
        )
        .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
        .itemFlags(ItemFlag.HIDE_ENCHANTS)
        .build(),
)

class Portals(path: Path) {
    private val loader: HoconConfigurationLoader
    private val node: CommentedConfigurationNode

    var portals: MutableList<Portal> = mutableListOf()

    init {
        loader = CrystalConfigs.hoconLoader(path, BukkitConfigs.serializers())
        node = loader.load()
        portals = node.node("portals").getList(Portal::class.java, portals)
        logger.info("Loaded ${portals.size} portals")
    }

    fun save() {
        node.node("portals").setList(Portal::class.java, portals)
        loader.save(node)
    }
}

@ConfigSerializable
data class Messages(
    var errors: Errors = Errors(),
    var commands: Commands = Commands(),
) {
    @ConfigSerializable
    data class Errors(
        var noPermission: String = "<red>Недостаточно полномочий!",
        var onlyForPlayers: String = "<red>Это могут делать только игроки!",
        var idNotSpecified: String = "<red>Требуется указать ID портала!",
        var noPermissionPortal: String = "<red>Вы не можете использовать этот портал!",
        var noSelection: String = "<red>Сначала нужно выделить область! Для этого используйте /portal wand",
        var alreadySelected: String = "<red>Эта точка уже установлена здесь!",
        var portalNotFound: String = "<red>Портал с таким ID не найден!",
        var actionNotFound: String = "<red>Действие с таким ID не найдено!",
        var noActions: String = "<red>У этого портала нет действий",
    )

    @ConfigSerializable
    data class Commands(
        var reload: String = "<green>Конфигурация успешно перезагружена",
        var wand: String = "<green>Инструмент выдан! ЛКМ - первая точка, ПКМ - вторая точка.",
        var wandPos1: String = "<green>Первая точка установлена на <gold><x> <y> <z></gold>",
        var wandPos2: String = "<green>Вторая точка установлена на <gold><x> <y> <z></gold>",
        var create: String = "<green>Портал <gold><portal_id></gold> создан!",
        var delete: String = "<green>Портал <gold><portal_id></gold> удалён!",
        var actions: Actions = Actions(),
        var permission: Permission = Permission(),
    ) {
        @ConfigSerializable
        data class Actions(
            var add: String = "<green>Действие <gray>\"<gold><action></gold>\" (<gold>ID: <action_id></gold>)</gray> добавлено к порталу <gold><portal_id></gold>!",
            var remove: String = "<green>Действие <gold><action_id></gold> удалено!",
            var clear: String = "<green>Все действия портала <gold><portal_id></gold> удалены!",
            var listHeader: String = "<green>Действия портала <gold><portal_id></gold>:",
            var listEntry: String = "\n<white><action_id><dark_gray>: <gray><action>",
        )

        @ConfigSerializable
        data class Permission(
            var set: String = "<green>Право <gray>\"<gold><permission></gold>\"</gray> установлено для портала <gold><portal_id></gold>!",
            var remove: String = "<green>Право на использование портала <gold><portal_id></gold> удалено!",
        )
    }
}
