/*
 * Copyright (c) 2023 Rafaelka
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.example.testplugin;

import me.denarydev.crystal.config.CrystalConfigs;
import me.denarydev.crystal.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.nio.file.Path;

@ConfigSerializable
public class Settings {

    private static Settings instance;

    public static Settings get() {
        return instance;
    }

    public static void load(Path file) throws ConfigurateException {
        instance = CrystalConfigs.loadConfig(file, Settings.class, true);
    }

    @Comment("This is an example setting")
    public String exampleString = "Example String";

    @Comment("With crystal utils you can create items easily and save it to config!")
    public ItemStack exampleItem = ItemUtils.itemBuilder()
            .type(Material.NETHERITE_SWORD)
            .displaynameRich("<red>Super Sword")
            .enchantment(Enchantment.DAMAGE_ALL, 10)
            .enchantment(Enchantment.FIRE_ASPECT, 2)
            .enchantment(Enchantment.DURABILITY, 5)
            .enchantment(Enchantment.MENDING, 1)
            .build();
}
