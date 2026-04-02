package com.jeffd76.lunchpail_delight.client;

import com.jeffd76.lunchpail_delight.LunchpailDelight;
import com.jeffd76.lunchpail_delight.menu.LunchpailScreen;
import com.jeffd76.lunchpail_delight.tooltip.LunchpailTooltip;
import com.jeffd76.lunchpail_delight.tooltip.ClientLunchpailTooltip;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientEvents {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(LunchpailDelight.LUNCHPAIL_MENU.get(), LunchpailScreen::new);
    }

    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(LunchpailTooltip.class, ClientLunchpailTooltip::new);
    }
}