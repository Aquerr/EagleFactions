package io.github.aquerr.eaglefactions.integrations.bluemap;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsRunnableTask;

import java.util.Iterator;

public class BlueMapUpdateTask implements EagleFactionsRunnableTask
{
    private final BlueMapService blueMapService;
    private final FactionLogic factionLogic;

    public BlueMapUpdateTask(BlueMapService blueMapService,
                             FactionLogic factionLogic)
    {
        this.blueMapService = blueMapService;
        this.factionLogic = factionLogic;
    }

    @Override
    public void run()
    {
        for (Iterator<Faction> iterator = this.blueMapService.getDrawnFactions().iterator(); iterator.hasNext();)
        {
            Faction drawnFaction = iterator.next();
            Faction faction = this.factionLogic.getFactionByName(drawnFaction.getName());

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

        for (Faction faction : this.factionLogic.getFactions().values())
        {
            this.blueMapService.addMarkersForFaction(faction);
        }
    }

    private boolean hasNewClaims(final Faction drawnFaction, final Faction faction)
    {
        return drawnFaction.getClaims().hashCode() != faction.getClaims().hashCode();
    }
}
