package com.jeffd76.canteen_delight.client;

import com.jeffd76.canteen_delight.CanteenDelight;
import com.jeffd76.canteen_delight.menu.CanteenScreen;
import com.jeffd76.canteen_delight.tooltip.CanteenTooltip;
import com.jeffd76.canteen_delight.tooltip.ClientCanteenTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = CanteenDelight.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CanteenDelight.CANTEEN_MENU.get(), CanteenScreen::new);
    }

    @SubscribeEvent
    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CanteenTooltip.class, ClientCanteenTooltip::new);
    }
}
