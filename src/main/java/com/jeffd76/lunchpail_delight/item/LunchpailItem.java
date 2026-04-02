package com.jeffd76.lunchpail_delight.item;

import com.jeffd76.lunchpail_delight.LunchpailDelight;
import com.jeffd76.lunchpail_delight.menu.LunchpailMenu;
import com.jeffd76.lunchpail_delight.tooltip.LunchpailTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LunchpailItem extends Item {
    private final int slotCount;

    public LunchpailItem(int slotCount, Properties properties) {
        super(properties);
        this.slotCount = slotCount;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public static final TagKey<Item> CHORUS_LIKE = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(LunchpailDelight.MODID, "chorus_like")
    );

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new LunchpailMenu(id, inv, stack),
                                stack.getHoverName()
                        ),
                        buf -> buf.writeVarInt(hand.ordinal())
                );
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler != null && hasContents(handler)) {
            boolean hasAlwaysEdible = false;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack slotStack = handler.getStackInSlot(i);
                FoodProperties food = slotStack.getFoodProperties(player);
                if (food != null && food.canAlwaysEat()) {
                    hasAlwaysEdible = true;
                    break;
                }
            }
            if (player.canEat(hasAlwaysEdible)) {
                return ItemUtils.startUsingInstantly(level, player, hand);
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide) return stack;

        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) return stack;

        List<Integer> filledSlots = new ArrayList<>();
        List<Integer> alwaysEdibleSlots = new ArrayList<>();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
                filledSlots.add(i);
                FoodProperties food = slotStack.getFoodProperties(entity);
                if (food != null && food.canAlwaysEat()) {
                    alwaysEdibleSlots.add(i);
                }
            }
        }

        if (filledSlots.isEmpty()) return stack;

        // If the player is full, only pick from always-edible slots
        List<Integer> candidates = filledSlots;
        if (entity instanceof Player player && !player.canEat(false) && !alwaysEdibleSlots.isEmpty()) {
            candidates = alwaysEdibleSlots;
        }

        int chosenSlot = candidates.get(level.random.nextInt(candidates.size()));
        ItemStack foodStack = handler.extractItem(chosenSlot, 1, false);

        if (!foodStack.isEmpty()) {

            FoodProperties food = foodStack.getFoodProperties(entity);
            if (food != null) {
                entity.eat(level, foodStack.copy(), food);
            }

            // Handle Chorus Fruit teleport
            if (foodStack.is(CHORUS_LIKE)) {
                double x = entity.getX();
                double y = entity.getY();
                double z = entity.getZ();
                for (int i = 0; i < 16; i++) {
                    double tx = entity.getX() + (level.random.nextDouble() - 0.5) * 16.0;
                    double ty = Mth.clamp(entity.getY() + (level.random.nextInt(16) - 8), level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
                    double tz = entity.getZ() + (level.random.nextDouble() - 0.5) * 16.0;
                    if (entity.randomTeleport(tx, ty, tz, true)) {
                        level.gameEvent(GameEvent.TELEPORT, new Vec3(x, y, z), GameEvent.Context.of(entity));
                        SoundEvent soundevent = entity instanceof Player ? SoundEvents.CHORUS_FRUIT_TELEPORT : SoundEvents.ENDERMAN_TELEPORT;
                        level.playSound(null, x, y, z, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                        entity.playSound(soundevent, 1.0F, 1.0F);
                        break;
                    }
                }
            }
            // Handle crafting remainder (e.g. bowls from stew)
            if (entity instanceof Player player) {
                ItemStack remainder = foodStack.getCraftingRemainingItem();
                if (!remainder.isEmpty() && !player.hasInfiniteMaterials()) {
                    if (!player.getInventory().add(remainder)) {
                        player.drop(remainder, false);
                    }
                }
            }
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return stack;
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
            tooltip.add(Component.translatable("item.lunchpail_delight.lunchpail.fullness", filled, slotCount)
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
        return Optional.of(new LunchpailTooltip(items, slotCount));
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