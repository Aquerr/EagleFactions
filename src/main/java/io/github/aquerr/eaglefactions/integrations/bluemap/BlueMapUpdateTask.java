package io.github.aquerr.eaglefactions.integrations.bluemap;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsRunnableTask;

import java.util.Iterator;

public class BlueMapUpdateTask implements EagleFactionsRunnableTask
{
    private final BlueMapService blueMapService;

    public BlueMapUpdateTask(BlueMapService blueMapService)
    {
        this.blueMapService = blueMapService;
    }

    @Override
    public void run()
    {
        for (Iterator<Faction> iterator = this.blueMapService.getDrawnFactions().iterator(); iterator.hasNext();)
        {
            Faction drawnFaction = iterator.next();
            Faction faction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByName(drawnFaction.getName());

            // Delete drawn markers for changed factions...
            if (faction == null || hasNewClaims(drawnFaction, faction))
            {
                this.blueMapService.deleteMarkersForFaction(drawnFaction);
            }

            if (faction != null)
            {
                this.blueMapService.addMarkersForFaction(faction);
            }
        }
    }

    private boolean hasNewClaims(final Faction drawnFaction, final Faction faction)
    {
        return drawnFaction.getClaims().hashCode() != faction.getClaims().hashCode();
    }
}
