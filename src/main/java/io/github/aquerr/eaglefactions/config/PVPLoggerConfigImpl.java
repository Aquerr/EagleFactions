package io.github.aquerr.eaglefactions.config;

import com.google.common.collect.Sets;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.PVPLoggerConfig;

import java.util.Set;

public class PVPLoggerConfigImpl implements PVPLoggerConfig
{
	private final Configuration configuration;

	private boolean isPvpLoggerActive = true;
	private int pvpLoggerBlockTime = 60;
	private boolean showPvpLoggerInScoreboard = true;
	private Set<String> blockedCommandsDuringFight = Sets.newHashSet("/f home", "spawn", "tpa", "/tp");

	public PVPLoggerConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		this.isPvpLoggerActive = this.configuration.getBoolean(true, "pvp-logger", "active");
		this.pvpLoggerBlockTime = this.configuration.getInt(60, "pvp-logger", "time");
		this.showPvpLoggerInScoreboard = this.configuration.getBoolean(true, "pvp-logger", "show-in-scoreboard");
		this.blockedCommandsDuringFight = this.configuration.getSetOfStrings(Sets.newHashSet("/f home", "spawn", "tpa", "/tp"), "pvp-logger", "blocked-commands-during-fight");
	}

	@Override
	public boolean isPVPLoggerActive()
	{
		return this.isPvpLoggerActive;
	}

	@Override
	public int getPVPLoggerBlockTime()
	{
		return this.pvpLoggerBlockTime;
	}

	@Override
	public Set<String> getBlockedCommandsDuringFight()
	{
		return this.blockedCommandsDuringFight;
	}

	@Override
	public boolean shouldDisplayPvpLoggerInScoreboard()
	{
		return this.showPvpLoggerInScoreboard;
	}
}
