package io.github.aquerr.eaglefactions.dynmap;

import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import org.spongepowered.api.Sponge;

public class DynmapUpdateTask implements Runnable {
    @Override
    public void run() {
        // TODO: add optimizations
        if (!Sponge.getServer().getDefaultWorld().isPresent()) {
            return;
        }

        String worldName = Sponge.getServer().getDefaultWorld().get().getWorldName();

        MarkerSet markerSwt = DynmapMain.markerapi.createMarkerSet("purpleflag", "EagleFactions", DynmapMain.markerapi.getMarkerIcons(), false);
        String markerId = worldName + "_" + "FactionTest";

        AreaMarker areaMarker = markerSwt.createAreaMarker(markerId, "FactionTest", false, worldName, new double[1000], new double[1000], false);

        double[] d1 = {-50, -9};
        double[] d2 = {-720, -679};

        areaMarker.setCornerLocations(d1, d2);

        areaMarker.setLabel("Test");
        areaMarker.setDescription("Test?");
    }
}
