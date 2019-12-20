package io.github.aquerr.eaglefactions.common.dynmap;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.spongepowered.api.scheduler.Task;

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
                        .submit(plugin);
            }
        });
    }
}
