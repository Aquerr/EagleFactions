package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class FactionRemoverTask implements EagleFactionsRunnableTask
{
    private final Configuration configuration;
    private final FactionLogic factionLogic;
    private final FactionsConfig factionsConfig;

    public FactionRemoverTask(final EagleFactions plugin)
    {
        this.configuration = plugin.getConfiguration();
        this.factionLogic = plugin.getFactionLogic();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public void run()
    {
        final long maxInactiveTimeInSeconds = this.factionsConfig.getMaxInactiveTime();
        final Map<String, Faction> factionsList = new HashMap<>(this.factionLogic.getFactions());
        final boolean shouldNotifyWhenRemoved = this.factionsConfig.shouldNotifyWhenFactionRemoved();
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
                Sponge.getServer().getBroadcastChannel().send(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.RED, Messages.FACTION + " ", TextColors.GOLD, factionEntry.getKey(), TextColors.RED, " has been removed due to its long inactivity time.")));
        }
    }
}
