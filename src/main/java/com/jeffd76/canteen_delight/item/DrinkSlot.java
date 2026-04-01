package com.jeffd76.canteen_delight.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;

public class DrinkSlot extends ItemHandlerCopySlot {
    public DrinkSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getUseAnimation() == UseAnim.DRINK && super.mayPlace(stack);
    }
}
