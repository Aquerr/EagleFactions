package io.github.aquerr.eaglefactions.common.integrations.dynmap;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.integrations.dynmap.util.DynmapUtils;
import io.github.aquerr.eaglefactions.common.integrations.dynmap.util.TempAreaMarker;
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

public class DynmapUpdateTask implements Runnable
{
    @Override
    public void run()
    {
        // Check old factions
        for (Iterator<Faction> iterator = DynmapService.drawnFactions.iterator(); iterator.hasNext();)
        {
            Faction drawnFaction = iterator.next();
            Faction currentFaction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByName(drawnFaction.getName());

            if (currentFaction == null || !currentFaction.equals(drawnFaction))
            {
                /* Remove everything created */
                if (DynmapService.drawnMarkers.get(drawnFaction.getName()) != null)
                {
                    DynmapService.drawnMarkers.get(drawnFaction.getName()).deleteMarker();
                    DynmapService.drawnMarkers.remove(drawnFaction.getName());
                }

                if (DynmapService.drawnAreas.get(drawnFaction.getName()) != null)
                {
                    for (AreaMarker drawFactionArea : DynmapService.drawnAreas.get(drawnFaction.getName()))
                    {
                        drawFactionArea.deleteMarker();
                    }

                    DynmapService.drawnAreas.remove(drawnFaction.getName());
                }

                /* Mark the faction for update */
                iterator.remove();
            }
        }

        for (Faction faction : EagleFactionsPlugin.getPlugin().getFactionLogic().getFactions().values())
        {
            if (faction.getClaims().size() < 1 || DynmapService.drawnFactions.contains(faction)) continue; /* Faction does not have any claims or it's already drawn */

            if (faction.getHome() != null)
            { /* Let's draw faction home first */
                World factionHomeWorld = Sponge.getServer().getWorld(faction.getHome().getWorldUUID()).isPresent()
                        ? Sponge.getServer().getWorld(faction.getHome().getWorldUUID()).get()
                        : null;

                if (factionHomeWorld != null)
                {
                    Vector3i blockPos = faction.getHome().getBlockPosition();

                    Marker marker = DynmapService.markerSet.createMarker(null,
                            faction.getName() + " Home",
                            factionHomeWorld.getName(),
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            DynmapService.markerapi.getMarkerIcon(EagleFactionsPlugin.getPlugin().getConfiguration().getDynmapConfig().getDynmapFactionHomeIcon()),
                            false);

                    DynmapService.drawnMarkers.put(faction.getName(), marker);
                }
            }

            /*
            Code below may not be very clean. BUT IT WORKS!
             */

            HashMap<UUID, Set<Claim>> claimsWorld = new HashMap<>();

            /* Now sorting the claims by their worlds */
            Claim[] claims = new Claim[faction.getClaims().size()];
            claims = faction.getClaims().toArray(claims);
            for (Claim claim : claims)
            {
                claimsWorld.computeIfAbsent(claim.getWorldUUID(), k -> new HashSet<>());

                claimsWorld.get(claim.getWorldUUID()).add(claim);
            }

            /* Now making TempAreaMarkers */
            HashMap<UUID, ArrayList<TempAreaMarker>> areaMarkers = new HashMap<>();
            claimsWorld.forEach((k, v) ->
            {
                ArrayList<TempAreaMarker> tempMarkers = DynmapUtils.createAreas(v);

                areaMarkers.put(k, tempMarkers);
            });

            /* Finally, lets draw areas! */
            DynmapService.drawnAreas.put(faction.getName(), new ArrayList<>());

            for (Map.Entry<UUID, ArrayList<TempAreaMarker>> entry : areaMarkers.entrySet())
            {
                for (TempAreaMarker tempMarker : entry.getValue())
                {
                    World world = Sponge.getServer().getWorld(entry.getKey()).isPresent() ? Sponge.getServer().getWorld(entry.getKey()).get() : null;

                    if (world == null) continue; /* Somehow there's no world for that area */

                    AreaMarker areaMarker = DynmapService.markerSet.createAreaMarker(null,
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

                    DynmapService.drawnAreas.get(faction.getName()).add(areaMarker);
                }
            }

            DynmapService.drawnFactions.add(faction);
        }
    }
}
