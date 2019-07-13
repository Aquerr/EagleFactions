package io.github.aquerr.eaglefactions.dynmap;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.dynmap.util.DynmapUtils;
import io.github.aquerr.eaglefactions.dynmap.util.TempAreaMarker;
import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;
import java.util.*;

/**
 * Dynmap Integration update task. It draws the faction areas by scanning all factions.
 * It stores the factions and compares new ones to the old ones when updating, and if they're different updates them.
 *
 * @author Iterator
 */

public class DynmapUpdateTask implements Runnable {
    private ArrayList<Faction> drawnFactions = new ArrayList<>();

    private HashMap<String, Marker> drawnMarkers = new HashMap<>();
    private HashMap<String, ArrayList<AreaMarker>> drawnAreas = new HashMap<>();

    @Override
    public void run() {
        for (Iterator<Faction> iterator = drawnFactions.iterator(); iterator.hasNext();) {
            Faction drawmFaction = iterator.next();

            Faction currentFaction = EagleFactions.getPlugin().getFactionLogic().getFactionByName(drawmFaction.getName());

            if (currentFaction == null || !currentFaction.equals(drawmFaction)) {
                /* Remove everything created */
                if (drawnMarkers.get(drawmFaction.getName()) != null) {
                    drawnMarkers.get(drawmFaction.getName()).deleteMarker();
                    drawnMarkers.remove(drawmFaction.getName());
                }

                if (drawnAreas.get(drawmFaction.getName()) != null) {
                    for (AreaMarker drawFactionArea : drawnAreas.get(drawmFaction.getName())) {
                        drawFactionArea.deleteMarker();
                    }

                    drawnAreas.remove(drawmFaction.getName());
                }

                /* Mark the faction for update */
                iterator.remove();
            }
        }

        for (Faction faction : new HashSet<>(EagleFactions.getPlugin().getFactionLogic().getFactions().values())) {
            if (faction.getClaims().size() < 1 || drawnFactions.contains(faction)) continue; /* Faction does not have any claims or it's already drawn */

            if (faction.getHome() != null) { /* Let's draw faction home first */
                World factionHomeWorld = Sponge.getServer().getWorld(faction.getHome().getWorldUUID()).isPresent()
                        ? Sponge.getServer().getWorld(faction.getHome().getWorldUUID()).get()
                        : null;

                if (factionHomeWorld != null) {
                    Vector3i blockPos = faction.getHome().getBlockPosition();

                    Marker marker = DynmapMain.markerSet.createMarker(null,
                            faction.getName() + " Home",
                            factionHomeWorld.getName(),
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            DynmapMain.markerapi.getMarkerIcon(EagleFactions.getPlugin().getConfiguration().getConfigFields().getDynmapFactionHomeIcon()),
                            false);

                    drawnMarkers.put(faction.getName(), marker);
                }
            }

            /*
            Code below may not be very clean. BUT IT WORKS!
             */

            HashMap<UUID, Set<Claim>> claimsWorld = new HashMap<>();

            /* Now sorting the claims by their worlds */
            Claim[] claims = new Claim[faction.getClaims().size()];
            claims = faction.getClaims().toArray(claims);
            for (Claim claim : claims) {
                claimsWorld.computeIfAbsent(claim.getWorldUUID(), k -> new HashSet<>());

                claimsWorld.get(claim.getWorldUUID()).add(claim);
            }

            /* Now making TempAreaMarkers */
            HashMap<UUID, ArrayList<TempAreaMarker>> areaMarkers = new HashMap<>();
            claimsWorld.forEach((k, v) -> {
                ArrayList<TempAreaMarker> tempMarkers = DynmapUtils.createAreas(v);

                areaMarkers.put(k, tempMarkers);
            });

            /* Finally, lets draw areas! */
            drawnAreas.put(faction.getName(), new ArrayList<>());

            for (Map.Entry<UUID, ArrayList<TempAreaMarker>> entry : areaMarkers.entrySet()) {
                for (TempAreaMarker tempMarker : entry.getValue()) {
                    World world = Sponge.getServer().getWorld(entry.getKey()).isPresent() ? Sponge.getServer().getWorld(entry.getKey()).get() : null;

                    if (world == null) continue; /* Somehow there's no world for that area */

                    AreaMarker areaMarker = DynmapMain.markerSet.createAreaMarker(null,
                            faction.getName(),
                            false,
                            world.getName(),
                            new double[1000],
                            new double[1000],
                            false);

                    areaMarker.setDescription(DynmapUtils.getFactionInfoWindow(faction));

                    int areaColor = DynmapUtils.getAreaColor(faction);
                    areaMarker.setLineStyle(3, 0.8, areaColor);
                    areaMarker.setFillStyle(0.35, areaColor);

                    areaMarker.setCornerLocations(tempMarker.x, tempMarker.z);

                    drawnAreas.get(faction.getName()).add(areaMarker);
                }
            }

            drawnFactions.add(faction);
        }
    }
}
