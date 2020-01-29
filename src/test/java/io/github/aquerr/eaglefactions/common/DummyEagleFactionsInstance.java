package io.github.aquerr.eaglefactions.common;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.*;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.config.ConfigurationImpl;
import io.github.aquerr.eaglefactions.common.logic.AttackLogicImpl;
import io.github.aquerr.eaglefactions.common.logic.FactionLogicImpl;
import io.github.aquerr.eaglefactions.common.logic.PVPLoggerImpl;
import io.github.aquerr.eaglefactions.common.managers.PlayerManagerImpl;
import io.github.aquerr.eaglefactions.common.managers.PowerManagerImpl;
import io.github.aquerr.eaglefactions.common.managers.ProtectionManagerImpl;
import io.github.aquerr.eaglefactions.common.storage.StorageManagerImpl;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DummyEagleFactionsInstance implements EagleFactions
{
	@Override
	public void printInfo(String message)
	{
		//Does nothing...
	}

	@Override
	public Configuration getConfiguration()
	{
		return new ConfigurationImpl(Paths.get("eaglefactions").resolve("Settings.conf"), null);
	}

	@Override
	public Path getConfigDir()
	{
		return Paths.get("eaglefactions").resolve("Settings.conf");
	}

	@Override
	public URL getResource(String fileName)
	{
		return null;
	}

	@Override
	public PlayerManager getPlayerManager()
	{
		return PlayerManagerImpl.getInstance(this);
	}

	@Override
	public FlagManager getFlagManager()
	{
		return FlagManagerImpl.getInstance(this);
	}

	@Override
	public PowerManager getPowerManager()
	{
		return PowerManagerImpl.getInstance(this);
	}

	@Override
	public ProtectionManager getProtectionManager()
	{
		return ProtectionManagerImpl.getInstance(this);
	}

	@Override
	public PVPLogger getPVPLogger()
	{
		return null;
	}

	@Override
	public FactionLogic getFactionLogic()
	{
		return null;
	}

	@Override
	public AttackLogic getAttackLogic()
	{
		return AttackLogicImpl.getInstance(this);
	}

	@Override
	public StorageManager getStorageManager()
	{
		return StorageManagerImpl.getInstance(this);
	}
}
