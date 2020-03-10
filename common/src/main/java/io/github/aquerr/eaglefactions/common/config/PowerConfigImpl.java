package io.github.aquerr.eaglefactions.common.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;

public class PowerConfigImpl implements PowerConfig
{
	private final Configuration configuration;

	private float globalMaxPower = 10.0f;
	private float startingPower = 5.0f;
	private float powerIncrement = 0.04f;
	private float powerDecrement = 2.00f;
	private float killAward = 2.00f;
	private float penalty = 1.0f;
	private float neededPowerPercentageToAttack = 0.20f;

	public PowerConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		this.globalMaxPower = configuration.getFloat(10.0f, "power", "max-power");
		this.startingPower = configuration.getFloat(5.0f, "power", "start-power");
		this.powerIncrement = configuration.getFloat(0.04f, "power", "increment");
		this.powerDecrement = configuration.getFloat(2.0f, "power", "decrement");
		this.killAward = configuration.getFloat(2.0f, "power", "kill-award");
		this.penalty = configuration.getFloat(1.0f, "power", "penalty");
		this.neededPowerPercentageToAttack = configuration.getFloat(20.0f, "attack-min-power-percentage") / 100;
	}

	@Override
	public float getGlobalMaxPower()
	{
		return this.globalMaxPower;
	}

	@Override
	public float getStartingPower()
	{
		return this.startingPower;
	}

	@Override
	public float getPowerIncrement()
	{
		return this.powerIncrement;
	}

	@Override
	public float getPowerDecrement()
	{
		return this.powerDecrement;
	}

	@Override
	public float getKillAward()
	{
		return this.killAward;
	}

	@Override
	public float getPenalty()
	{
		return this.penalty;
	}

	@Override
	public float getNeededPowerPercentageToAttack()
	{
		return this.neededPowerPercentageToAttack;
	}
}
