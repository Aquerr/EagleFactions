package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.BluemapConfig;
import io.github.aquerr.eaglefactions.api.config.Configuration;

public class BluemapConfigImpl implements BluemapConfig
{
    //Configuration reference
    private final Configuration configuration;

    //Bluemap Integration
    private boolean bluemapIntegrationEnabled = false;

    private int bluemapFactionColor = 0x00FF00;
    private int bluemapSafezoneColor = 0x800080;
    private int bluemapWarzoneColor = 0xFF0000;
    private String bluemapFactionHomeIcon = "greenflag";

    private boolean bluemapShowFactionLeader = true;
    private boolean bluemapMemberInfo = true;

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

        this.bluemapFactionColor = Integer.decode(this.configuration.getString("#00FF00", "bluemap", "faction-color"));
        this.bluemapSafezoneColor = Integer.decode(this.configuration.getString("#800080", "bluemap", "safezone-color"));
        this.bluemapWarzoneColor = Integer.decode(this.configuration.getString("#FF0000", "bluemap", "warzone-color"));
        this.bluemapFactionHomeIcon = this.configuration.getString("greenflag", "bluemap", "faction-home-marker");

        this.bluemapShowFactionLeader = this.configuration.getBoolean(true, "bluemap", "show-faction-leader");
        this.bluemapMemberInfo = this.configuration.getBoolean(true, "bluemap", "members-info");
    }

    @Override
    public boolean isBluemapIntegrationEnabled()
    {
        return this.bluemapIntegrationEnabled;
    }

    @Override
    public int getBluemapFactionColor()
    {
        return this.bluemapFactionColor;
    }

    @Override
    public int getBluemapSafezoneColor()
    {
        return this.bluemapSafezoneColor;
    }

    @Override
    public int getBluemapWarzoneColor()
    {
        return this.bluemapWarzoneColor;
    }

    @Override
    public String getBluemapFactionHomeIcon()
    {
        return this.bluemapFactionHomeIcon;
    }

    @Override
    public boolean showBluemapFactionLeader()
    {
        return this.bluemapShowFactionLeader;
    }

    @Override
    public boolean showBluemapMemberInfo()
    {
        return this.bluemapMemberInfo;
    }
}
