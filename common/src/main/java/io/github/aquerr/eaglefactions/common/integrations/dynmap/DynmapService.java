package io.github.aquerr.eaglefactions.common.integrations.dynmap;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.scheduling.EagleFactionsScheduler;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Main Dynmap Integration class, contains Dynmap API fields and activate void
 * @author Iterator
 *
 * Edited by Aquerr
 */

public class DynmapService
{
    static MarkerAPI markerapi;
    static MarkerSet markerSet;

    final static ArrayList<Faction> drawnFactions = new ArrayList<>();

    // Faction Name --> Marker
    final static HashMap<String, Marker> drawnMarkers = new HashMap<>();

    // Faction name --> AreaMarkers
    final static HashMap<String, ArrayList<AreaMarker>> drawnAreas = new HashMap<>();

    private final EagleFactions plugin;

    private Task dynmapUpdateTask;

    public DynmapService(final EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    public void activate()
    {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI api) {
                markerapi = api.getMarkerAPI();
                markerSet = createOrGetMarkerSet();

                if (markerSet == null)
                {
                    Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "Could not create MarkerSet in Dynmap. Restarting the server may help."));
                    return;
                }

                restartDynmapUpdateTask();
            }
        });
    }

    /**
     * Clears all markers and dynmap-factions cache.
     */
    public void reload()
    {
        drawnFactions.clear();
        drawnMarkers.values().forEach(GenericMarker::deleteMarker);
        drawnMarkers.clear();
        drawnAreas.values().forEach(x->
        {
            x.forEach(GenericMarker::deleteMarker);
            x.clear();
        });
        drawnAreas.clear();
        markerSet.deleteMarkerSet();
        markerSet = createOrGetMarkerSet();
        restartDynmapUpdateTask();
    }

    private MarkerSet createOrGetMarkerSet()
    {
        MarkerSet markerSet = markerapi.getMarkerSet("purpleflag");
        if (markerSet == null)
            markerSet = markerapi.createMarkerSet("purpleflag", "EagleFactions", markerapi.getMarkerIcons(), false);
        return markerSet;
    }

    private void restartDynmapUpdateTask()
    {
        if (this.dynmapUpdateTask != null)
        {
            this.dynmapUpdateTask.cancel();
        }
        this.dynmapUpdateTask = EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(new DynmapUpdateTask(), 0, TimeUnit.SECONDS, 10, TimeUnit.SECONDS);
    }
}
