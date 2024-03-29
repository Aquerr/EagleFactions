package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.DynmapConfig;

public class DynmapConfigImpl implements DynmapConfig
{
	//Configuration reference
	private final Configuration configuration;

	//Dynmap Integration
	private boolean dynmapIntegrationEnabled = false;

	private int dynmapFactionColor = 0x00FF00;
	private int dynmapSafezoneColor = 0x800080;
	private int dynmapWarzoneColor = 0xFF0000;
	private String dynmapFactionHomeIcon = "greenflag";

	private boolean dynmapShowFactionLeader = true;
	private boolean dynmapMemberInfo = true;

	public DynmapConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		this.dynmapIntegrationEnabled = this.configuration.getBoolean(true, "dynmap", "enabled");

		if (!this.dynmapIntegrationEnabled)
			return;

		this.dynmapFactionColor = Integer.decode(this.configuration.getString("#00FF00", "dynmap", "faction-color"));
		this.dynmapSafezoneColor = Integer.decode(this.configuration.getString("#800080", "dynmap", "safezone-color"));
		this.dynmapWarzoneColor = Integer.decode(this.configuration.getString("#FF0000", "dynmap", "warzone-color"));
		this.dynmapFactionHomeIcon = this.configuration.getString("greenflag", "dynmap", "faction-home-marker");

		this.dynmapShowFactionLeader = this.configuration.getBoolean(true, "dynmap", "show-faction-leader");
		this.dynmapMemberInfo = this.configuration.getBoolean(true, "dynmap", "members-info");
	}

	@Override
	public boolean isDynmapIntegrationEnabled()
	{
		return this.dynmapIntegrationEnabled;
	}

	@Override
	public int getDynmapFactionColor()
	{
		return this.dynmapFactionColor;
	}

	@Override
	public int getDynmapSafezoneColor()
	{
		return this.dynmapSafezoneColor;
	}

	@Override
	public int getDynmapWarzoneColor()
	{
		return this.dynmapWarzoneColor;
	}

	@Override
	public String getDynmapFactionHomeIcon()
	{
		return this.dynmapFactionHomeIcon;
	}

	@Override
	public boolean showDynmapFactionLeader()
	{
		return this.dynmapShowFactionLeader;
	}

	@Override
	public boolean showDynmapMemberInfo()
	{
		return this.dynmapMemberInfo;
	}
}
