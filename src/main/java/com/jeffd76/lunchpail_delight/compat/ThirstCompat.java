package com.jeffd76.lunchpail_delight.compat;

import dev.ghen.thirst.content.thirst.PlayerThirst;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ThirstCompat {

    public static void onDrink(ItemStack drinkStack, Player player) {
        PlayerThirst.drink(drinkStack, player);
    }
}
