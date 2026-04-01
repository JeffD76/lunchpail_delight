package com.jeffd76.canteen_delight.item;

import com.jeffd76.canteen_delight.compat.ThirstCompat;
import com.jeffd76.canteen_delight.menu.CanteenMenu;
import com.jeffd76.canteen_delight.tooltip.CanteenTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CanteenItem extends Item {
    private final int slotCount;

    public CanteenItem(int slotCount, Properties properties) {
        super(properties);
        this.slotCount = slotCount;
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new CanteenMenu(id, inv, stack),
                                stack.getHoverName()
                        ),
                        buf -> buf.writeVarInt(hand.ordinal())
                );
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler != null && hasContents(handler)) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide) {
            return stack;
        }

        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) {
            return stack;
        }

        List<Integer> filledSlots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                filledSlots.add(i);
            }
        }

        if (filledSlots.isEmpty()) {
            return stack;
        }

        int chosenSlot = filledSlots.get(level.random.nextInt(filledSlots.size()));
        ItemStack drinkStack = handler.extractItem(chosenSlot, 1, false);

        if (!drinkStack.isEmpty()) {
            simulateDrink(drinkStack, level, entity);
        }

        return stack;
    }

    private void simulateDrink(ItemStack drinkStack, Level level, LivingEntity entity) {
        FoodProperties food = drinkStack.getFoodProperties(entity);
        if (food != null) {
            entity.eat(level, drinkStack.copy(), food);
        }

        boolean isMilkItem = drinkStack.getItem() instanceof MilkBucketItem
                || drinkStack.getDescriptionId().toLowerCase().contains("milk");
        if (isMilkItem && !level.isClientSide) {
            entity.removeAllEffects();
        }

        PotionContents potionContents = drinkStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null && food == null) {
            potionContents.forEachEffect(effect -> {
                if (effect.getEffect().value().isInstantenous()) {
                    Player player = entity instanceof Player ? (Player) entity : null;
                    effect.getEffect().value().applyInstantenousEffect(player, player, entity, effect.getAmplifier(), 1.0);
                } else {
                    entity.addEffect(effect);
                }
            });
        }

        if (entity instanceof Player player) {
            if (ModList.get().isLoaded("thirst")) {
                ThirstCompat.onDrink(drinkStack, player);
            }

            ItemStack container = drinkStack.getCraftingRemainingItem();
            if (!container.isEmpty() && !player.hasInfiniteMaterials()) {
                if (!player.getInventory().add(container)) {
                    player.drop(container, false);
                }
            }

            if (drinkStack.getItem() instanceof PotionItem && !player.hasInfiniteMaterials()) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
            }

            if (drinkStack.getItem() instanceof MilkBucketItem && !player.hasInfiniteMaterials()) {
                ItemStack bucket = new ItemStack(Items.BUCKET);
                if (!player.getInventory().add(bucket)) {
                    player.drop(bucket, false);
                }
            }


            if (food != null && food.usingConvertsTo().isPresent() && !player.hasInfiniteMaterials()) {
                ItemStack convertedItem = food.usingConvertsTo().get().copy();
                if (!player.getInventory().add(convertedItem)) {
                    player.drop(convertedItem, false);
                }
            }
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private boolean hasContents(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler != null) {
            int filled = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    filled++;
                }
            }
            tooltip.add(Component.translatable("item.canteen_delight.canteen.fullness", filled, slotCount)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) {
            return Optional.empty();
        }
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            items.add(handler.getStackInSlot(i));
        }
        return Optional.of(new CanteenTooltip(items, slotCount));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) return false;
        return hasContents(handler);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) return 0;
        int filled = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                filled++;
            }
        }
        return Math.round(13.0F * filled / handler.getSlots());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x3399FF;
    }
}
