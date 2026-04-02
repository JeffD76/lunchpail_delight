package com.jeffd76.lunchpail_delight;

import com.jeffd76.lunchpail_delight.client.ClientEvents;
import com.jeffd76.lunchpail_delight.datagen.LunchpailTagProvider;
import com.jeffd76.lunchpail_delight.item.LunchpailItem;
import com.jeffd76.lunchpail_delight.menu.LunchpailMenu;
import com.jeffd76.lunchpail_delight.recipe.LunchpailUpgradeRecipe;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
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
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

@Mod(LunchpailDelight.MODID)
public class LunchpailDelight {
    public static final String MODID = "lunchpail_delight";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final Supplier<DataComponentType<ItemContainerContents>> LUNCHPAIL_CONTENTS = DATA_COMPONENTS.register("lunchpail_contents", () ->
            DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build()
    );

    public static final DeferredItem<LunchpailItem> LEATHER_LUNCHPAIL = ITEMS.register("leather_lunchpail", () ->
            new LunchpailItem(5, new Item.Properties().stacksTo(1).component(LUNCHPAIL_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<LunchpailItem> COPPER_LUNCHPAIL = ITEMS.register("copper_lunchpail", () ->
            new LunchpailItem(7, new Item.Properties().stacksTo(1).component(LUNCHPAIL_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<LunchpailItem> IRON_LUNCHPAIL = ITEMS.register("iron_lunchpail", () ->
            new LunchpailItem(9, new Item.Properties().stacksTo(1).component(LUNCHPAIL_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<LunchpailItem> GOLD_LUNCHPAIL = ITEMS.register("gold_lunchpail", () ->
            new LunchpailItem(12, new Item.Properties().stacksTo(1).component(LUNCHPAIL_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<LunchpailItem> DIAMOND_LUNCHPAIL = ITEMS.register("diamond_lunchpail", () ->
            new LunchpailItem(15, new Item.Properties().stacksTo(1).component(LUNCHPAIL_CONTENTS.get(), ItemContainerContents.EMPTY)));
    public static final DeferredItem<LunchpailItem> NETHERITE_LUNCHPAIL = ITEMS.register("netherite_lunchpail", () ->
            new LunchpailItem(18, new Item.Properties().stacksTo(1).component(LUNCHPAIL_CONTENTS.get(), ItemContainerContents.EMPTY).fireResistant()));

    public static final Supplier<MenuType<LunchpailMenu>> LUNCHPAIL_MENU = MENUS.register("lunchpail_menu", () ->
            IMenuTypeExtension.create(LunchpailMenu::new));

    public static final Supplier<RecipeSerializer<LunchpailUpgradeRecipe>> LUNCHPAIL_UPGRADE_RECIPE = RECIPE_SERIALIZERS.register("lunchpail_upgrade", LunchpailUpgradeRecipe.Serializer::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("lunchpail_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.lunchpail_delight"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> LEATHER_LUNCHPAIL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(LEATHER_LUNCHPAIL.get());
                        output.accept(COPPER_LUNCHPAIL.get());
                        output.accept(IRON_LUNCHPAIL.get());
                        output.accept(GOLD_LUNCHPAIL.get());
                        output.accept(DIAMOND_LUNCHPAIL.get());
                        output.accept(NETHERITE_LUNCHPAIL.get());
                    }).build());

    public LunchpailDelight(IEventBus modEventBus, ModContainer modContainer) {

        System.out.println("MOD ID IS: " + MODID);

        ITEMS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::gatherData);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ClientEvents::registerScreens);
            modEventBus.addListener(ClientEvents::registerTooltips);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (DeferredItem<LunchpailItem> canteen : List.of(LEATHER_LUNCHPAIL, COPPER_LUNCHPAIL, IRON_LUNCHPAIL, GOLD_LUNCHPAIL, DIAMOND_LUNCHPAIL, NETHERITE_LUNCHPAIL)) {
            event.registerItem(
                    Capabilities.ItemHandler.ITEM,
                    (stack, context) -> new ComponentItemHandler(stack, LUNCHPAIL_CONTENTS.get(), ((LunchpailItem) stack.getItem()).getSlotCount()),
                    canteen.get()
            );
        }
    }

    private void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeServer(),
                (DataProvider) new LunchpailTagProvider(
                        event.getGenerator().getPackOutput(),
                        event.getLookupProvider()
                )
        );
    }
}
