package com.jeffd76.canteen_delight.menu;

import com.jeffd76.canteen_delight.CanteenDelight;
import com.jeffd76.canteen_delight.item.CanteenItem;
import com.jeffd76.canteen_delight.item.DrinkSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class CanteenMenu extends AbstractContainerMenu {
    private final int slotCount;
    private final int containerSlot;

    public CanteenMenu(int containerId, Inventory playerInventory, ItemStack canteenStack) {
        super(CanteenDelight.CANTEEN_MENU.get(), containerId);

        CanteenItem canteenItem = (CanteenItem) canteenStack.getItem();
        this.slotCount = canteenItem.getSlotCount();
        this.containerSlot = playerInventory.selected;

        IItemHandler handler = canteenStack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler != null) {
            int rows = (int) Math.ceil(slotCount / 9.0);
            for (int i = 0; i < slotCount; i++) {
                int row = i / 9;
                int col = i % 9;
                int slotsInRow = Math.min(9, slotCount - row * 9);
                int xOffset = (9 - slotsInRow) * 9;
                this.addSlot(new DrinkSlot(handler, i, 8 + xOffset + col * 18, 18 + row * 18));
            }
        }

        int rows = (int) Math.ceil(slotCount / 9.0);
        int playerInvY = rows * 18 + 32;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public CanteenMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, getCanteenStack(playerInventory, data));
    }

    private static ItemStack getCanteenStack(Inventory inventory, RegistryFriendlyByteBuf data) {
        int handOrdinal = data.readVarInt();
        if (handOrdinal == 0) {
            return inventory.player.getMainHandItem();
        } else {
            return inventory.player.getOffhandItem();
        }
    }

    public int getSlotCount() {
        return slotCount;
    }

    public int getRows() {
        return (int) Math.ceil(slotCount / 9.0);
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof CanteenItem || offHand.getItem() instanceof CanteenItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack originalStack = slotStack.copy();

        if (index < slotCount) {
            if (!this.moveItemStackTo(slotStack, slotCount, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(slotStack, 0, slotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return originalStack;
    }
}
