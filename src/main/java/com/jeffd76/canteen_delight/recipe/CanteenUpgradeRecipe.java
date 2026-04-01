package com.jeffd76.canteen_delight.recipe;

import com.jeffd76.canteen_delight.CanteenDelight;
import com.jeffd76.canteen_delight.item.CanteenItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class CanteenUpgradeRecipe extends ShapedRecipe {
    private final ShapedRecipePattern savedPattern;
    private final ItemStack savedResult;

    public CanteenUpgradeRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.savedPattern = pattern;
        this.savedResult = result;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof CanteenItem) {
                ItemContainerContents contents = stack.get(CanteenDelight.CANTEEN_CONTENTS.get());
                if (contents != null) {
                    result.set(CanteenDelight.CANTEEN_CONTENTS.get(), contents);
                }
                break;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CanteenDelight.CANTEEN_UPGRADE_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<CanteenUpgradeRecipe> {
        public static final MapCodec<CanteenUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.savedPattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.savedResult),
                        Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
                ).apply(instance, CanteenUpgradeRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, CanteenUpgradeRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<CanteenUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CanteenUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static CanteenUpgradeRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            boolean showNotification = buf.readBoolean();
            return new CanteenUpgradeRecipe(group, category, pattern, result, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, CanteenUpgradeRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buf, recipe.savedPattern);
            ItemStack.STREAM_CODEC.encode(buf, recipe.savedResult);
            buf.writeBoolean(recipe.showNotification());
        }
    }
}
