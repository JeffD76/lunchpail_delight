package com.jeffd76.canteen_delight;

import com.jeffd76.canteen_delight.item.CanteenItem;
import com.jeffd76.canteen_delight.menu.CanteenMenu;
import com.jeffd76.canteen_delight.recipe.CanteenUpgradeRecipe;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

@Mod(CanteenDelight.MODID)
public class CanteenDelight {
    public static final String MODID = "canteen_delight";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final Supplier<DataComponentType<ItemContainerContents>> CANTEEN_CONTENTS = DATA_COMPONENTS.register("canteen_contents", () ->
            DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build()
    );

    public static final DeferredItem<CanteenItem> LEATHER_CANTEEN = ITEMS.register("leather_canteen", () ->
            new CanteenItem(5, new Item.Properties().stacksTo(1).component(CANTEEN_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<CanteenItem> COPPER_CANTEEN = ITEMS.register("copper_canteen", () ->
            new CanteenItem(7, new Item.Properties().stacksTo(1).component(CANTEEN_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<CanteenItem> IRON_CANTEEN = ITEMS.register("iron_canteen", () ->
            new CanteenItem(9, new Item.Properties().stacksTo(1).component(CANTEEN_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<CanteenItem> GOLD_CANTEEN = ITEMS.register("gold_canteen", () ->
            new CanteenItem(12, new Item.Properties().stacksTo(1).component(CANTEEN_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<CanteenItem> DIAMOND_CANTEEN = ITEMS.register("diamond_canteen", () ->
            new CanteenItem(15, new Item.Properties().stacksTo(1).component(CANTEEN_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<CanteenItem> NETHERITE_CANTEEN = ITEMS.register("netherite_canteen", () ->
            new CanteenItem(18, new Item.Properties().stacksTo(1).component(CANTEEN_CONTENTS.get(), ItemContainerContents.EMPTY).fireResistant()));

    public static final Supplier<MenuType<CanteenMenu>> CANTEEN_MENU = MENUS.register("canteen_menu", () ->
            IMenuTypeExtension.create(CanteenMenu::new));

    public static final Supplier<RecipeSerializer<CanteenUpgradeRecipe>> CANTEEN_UPGRADE_RECIPE = RECIPE_SERIALIZERS.register("canteen_upgrade", CanteenUpgradeRecipe.Serializer::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("canteen_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.canteen_delight"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> LEATHER_CANTEEN.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(LEATHER_CANTEEN.get());
                        output.accept(COPPER_CANTEEN.get());
                        output.accept(IRON_CANTEEN.get());
                        output.accept(GOLD_CANTEEN.get());
                        output.accept(DIAMOND_CANTEEN.get());
                        output.accept(NETHERITE_CANTEEN.get());
                    }).build());

    public CanteenDelight(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (DeferredItem<CanteenItem> canteen : List.of(LEATHER_CANTEEN, COPPER_CANTEEN, IRON_CANTEEN, GOLD_CANTEEN, DIAMOND_CANTEEN, NETHERITE_CANTEEN)) {
            event.registerItem(
                    Capabilities.ItemHandler.ITEM,
                    (stack, context) -> new ComponentItemHandler(stack, CANTEEN_CONTENTS.get(), ((CanteenItem) stack.getItem()).getSlotCount()),
                    canteen.get()
            );
        }
    }
}
