package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.HomeConfig;

public class HomeConfigImpl implements HomeConfig
{
    private final Configuration configuration;
    private int homeDelay = 5;
    private int homeCooldown = 60;
    private boolean blockHomeAfterDeathInOwnFaction = false;
    private int homeBlockTimeAfterDeathInOwnFaction = 60;
    private boolean canHomeBetweenWorlds = false;
    private boolean canPlaceHomeOutsideFactionClaim = false;
    private boolean spawnAtHomeAfterDeath = false;
    private boolean sourceTeleportAnimationEnabled = true;
    private boolean destinationTeleportAnimationEnabled = true;

    public HomeConfigImpl(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void reload()
    {
        this.homeDelay = this.configuration.getInt(5, "home", "delay");
        this.homeCooldown = this.configuration.getInt(60, "home", "cooldown");
        this.blockHomeAfterDeathInOwnFaction = this.configuration.getBoolean(false, "home","block-home-after-death-in-own-faction", "enabled");
        this.homeBlockTimeAfterDeathInOwnFaction = this.configuration.getInt(60, "home", "block-home-after-death-in-own-faction", "block-time");
        this.canHomeBetweenWorlds = this.configuration.getBoolean(false, "home", "teleport-between-worlds");
        this.canPlaceHomeOutsideFactionClaim = this.configuration.getBoolean(false, "home", "can-place-outside-faction-claim");
        this.spawnAtHomeAfterDeath = this.configuration.getBoolean(false, "home", "spawn-at-home-after-death");
        this.sourceTeleportAnimationEnabled = this.configuration.getBoolean(true, "home", "teleport-animation", "source", "enabled");
        this.destinationTeleportAnimationEnabled = this.configuration.getBoolean(true, "home", "teleport-animation", "destination", "enabled");
    }

    @Override
    public int getHomeDelayTime()
    {
        return this.homeDelay;
    }

    @Override
    public int getHomeCooldown()
    {
        return this.homeCooldown;
    }

    @Override
    public boolean shouldSpawnAtHomeAfterDeath()
    {
        return this.spawnAtHomeAfterDeath;
    }

    @Override
    public boolean canHomeBetweenWorlds()
    {
        return this.canHomeBetweenWorlds;
    }

    @Override
    public boolean canPlaceHomeOutsideFactionClaim()
    {
        return this.canPlaceHomeOutsideFactionClaim;
    }

    @Override
    public boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        return this.blockHomeAfterDeathInOwnFaction;
    }

    @Override
    public int getHomeBlockTimeAfterDeathInOwnFaction()
    {
        return this.homeBlockTimeAfterDeathInOwnFaction;
    }

    @Override
    public boolean isSourceTeleportAnimationEnabled()
    {
        return sourceTeleportAnimationEnabled;
    }

    @Override
    public boolean isDestinationTeleportAnimationEnabled()
    {
        return destinationTeleportAnimationEnabled;
    }
}
