package io.github.mattidragon.advancednetworking.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;

public class ModmenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ConfigClient.createScreen(parent, AdvancedNetworking.CONFIG.get(), AdvancedNetworking.CONFIG::set);
    }
}
