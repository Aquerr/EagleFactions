package io.github.aquerr.eaglefactions.common.integrations.dynmap;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.*;
import org.spongepowered.api.scheduler.Task;

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
                markerSet = DynmapService.markerapi.createMarkerSet("purpleflag", "EagleFactions", DynmapService.markerapi.getMarkerIcons(), false);

                Task.builder().execute(new DynmapUpdateTask())
                        .interval(10, TimeUnit.SECONDS)
                        .name("EagleFactions Dynmap Update Task")
                        .async()
                        .submit(plugin);
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
    }
}
