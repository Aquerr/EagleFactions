package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.BluemapConfig;
import io.github.aquerr.eaglefactions.api.config.Configuration;

public class BluemapConfigImpl implements BluemapConfig
{
    //Configuration reference
    private final Configuration configuration;

    //Bluemap Integration
    private boolean bluemapIntegrationEnabled = false;

    public BluemapConfigImpl(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void reload()
    {
        this.bluemapIntegrationEnabled = this.configuration.getBoolean(true, "bluemap", "enabled");

        if (!this.bluemapIntegrationEnabled)
            return;
    }

    @Override
    public boolean isBluemapIntegrationEnabled()
    {
        return this.bluemapIntegrationEnabled;
    }
}
