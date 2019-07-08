package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.common.config.IConfiguration;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.common.logic.FactionLogic;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class FactionRemoverTask implements EagleFactionsRunnableTask
{
    private final IConfiguration configuration;
    private final FactionLogic factionLogic;

    public FactionRemoverTask(final EagleFactionsPlugin eagleFactions)
    {
        this.configuration = eagleFactions.getConfiguration();
        this.factionLogic = eagleFactions.getFactionLogic();
    }

    @Override
    public void run()
    {
        final long maxInactiveTimeInSeconds = configuration.getConfigFields().getMaxInactiveTime();
        final Map<String, Faction> factionsList = new HashMap<>(this.factionLogic.getFactions());
        final boolean shouldNotifyWhenRemoved = this.configuration.getConfigFields().shouldNotifyWhenFactionRemoved();
        for(Map.Entry<String, Faction> factionEntry : factionsList.entrySet())
        {
            if(factionLogic.hasOnlinePlayers(factionEntry.getValue()))
                continue;

            if(factionEntry.getValue().getName().equalsIgnoreCase("safezone") || factionEntry.getValue().getName().equalsIgnoreCase("warzone"))
                continue;

            final Duration inactiveTime = Duration.between(factionEntry.getValue().getLastOnline(), Instant.now());
            if(inactiveTime.getSeconds() < maxInactiveTimeInSeconds)
                continue;

            boolean didSucceed = this.factionLogic.disbandFaction(factionEntry.getValue().getName());

            if(didSucceed && shouldNotifyWhenRemoved)
                Sponge.getServer().getBroadcastChannel().send(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.RED, PluginMessages.FACTION + " ", TextColors.GOLD, factionEntry.getKey(), TextColors.RED, " has been removed due to its long inactivity time.")));
        }
    }
}
