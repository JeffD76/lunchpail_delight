package com.jeffd76.canteen_delight.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientCanteenTooltip implements ClientTooltipComponent {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot");
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/background");
    private static final int SLOT_W = 18;
    private static final int SLOT_H = 20;
    private static final int MARGIN = 4;

    private final List<ItemStack> items;
    private final int slotCount;

    public ClientCanteenTooltip(CanteenTooltip tooltip) {
        this.items = tooltip.items();
        this.slotCount = tooltip.slotCount();
    }

    @Override
    public int getHeight() {
        return gridRows() * SLOT_H + 2 + MARGIN;
    }

    @Override
    public int getWidth(Font font) {
        return gridCols() * SLOT_W + 2;
    }

    private int gridCols() {
        return Math.min(slotCount, 9);
    }

    private int gridRows() {
        return (int) Math.ceil((double) slotCount / gridCols());
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int cols = gridCols();
        int rows = gridRows();
        int bgW = cols * SLOT_W + 2;
        int bgH = rows * SLOT_H + 2;

        graphics.blitSprite(BACKGROUND_SPRITE, x, y, bgW, bgH);

        int idx = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int slotX = x + col * SLOT_W + 1;
                int slotY = y + row * SLOT_H + 1;

                if (idx < slotCount) {
                    graphics.blitSprite(SLOT_SPRITE, slotX, slotY, 0, SLOT_W, SLOT_H);
                    if (idx < items.size() && !items.get(idx).isEmpty()) {
                        graphics.renderItem(items.get(idx), slotX + 1, slotY + 1, idx);
                        graphics.renderItemDecorations(font, items.get(idx), slotX + 1, slotY + 1);
                    }
                }
                idx++;
            }
        }
    }
}
