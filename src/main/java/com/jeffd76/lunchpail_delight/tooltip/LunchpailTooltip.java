package com.jeffd76.lunchpail_delight.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record LunchpailTooltip(List<ItemStack> items, int slotCount) implements TooltipComponent {
}
