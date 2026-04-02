package com.jeffd76.lunchpail_delight.datagen;

import com.jeffd76.lunchpail_delight.item.LunchpailItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class LunchpailTagProvider extends ItemTagsProvider {
    public LunchpailTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CompletableFuture.completedFuture(null));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(LunchpailItem.CHORUS_LIKE)
                .add(Items.CHORUS_FRUIT)
                .addOptional(ResourceLocation.fromNamespaceAndPath("ends_delight", "chorus_fruit_popsicle"));
    }

    @Override
    public String getName() {
        return "Lunchpail Delight Item Tags";
    }
}