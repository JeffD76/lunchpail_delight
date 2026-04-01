package com.jeffd76.canteen_delight.menu;

import com.jeffd76.canteen_delight.CanteenDelight;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CanteenScreen extends AbstractContainerScreen<CanteenMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CanteenDelight.MODID, "textures/gui/canteen.png");

    public CanteenScreen(CanteenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        int rows = menu.getRows();
        this.imageHeight = rows * 18 + 113;
        this.inventoryLabelY = rows * 18 + 20;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int rows = this.menu.getRows();
        int containerHeight = rows * 18 + 17;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, containerHeight);
        guiGraphics.blit(TEXTURE, x, y + containerHeight, 0, 126, this.imageWidth, 96);

        int canteenSlots = this.menu.getSlotCount();
        for (int i = 0; i < canteenSlots; i++) {
            Slot slot = this.menu.getSlot(i);
            guiGraphics.blit(TEXTURE, x + slot.x - 1, y + slot.y - 1, 176, 0, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
