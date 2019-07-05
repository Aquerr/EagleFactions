package io.github.aquerr.eaglefactions.dynmap;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class DynmapMain {
    static MarkerAPI markerapi;


    public static void activate() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI api) {
                markerapi = api.getMarkerAPI();

                Task.builder().execute(new DynmapUpdateTask())
                        .interval(10, TimeUnit.SECONDS)
                        .name("EagleFactions Dynmap Update Task")
                        .submit(EagleFactions.getPlugin());
            }
        });
    }
}
