package io.github.aquerr.eaglefactions.dynmap;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.spongepowered.api.scheduler.Task;
import java.util.concurrent.TimeUnit;

/**
 * Main Dynmap Integration class, contains Dynmap API fields and activate void
 * @author Iterator
 */

public class DynmapMain {
    static MarkerAPI markerapi;
    static MarkerSet markerSet;

    public static void activate() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI api) {
                markerapi = api.getMarkerAPI();
                markerSet = DynmapMain.markerapi.createMarkerSet("purpleflag", "EagleFactions", DynmapMain.markerapi.getMarkerIcons(), false);

                Task.builder().execute(new DynmapUpdateTask())
                        .interval(10, TimeUnit.SECONDS)
                        .name("EagleFactions Dynmap Update Task")
                        .submit(EagleFactions.getPlugin());
            }
        });
    }
}
