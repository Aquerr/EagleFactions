package io.github.aquerr.eaglefactions.integrations.dynmap;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.integrations.dynmap.util.DynmapUtils;
import io.github.aquerr.eaglefactions.integrations.dynmap.util.TempAreaMarker;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsRunnableTask;
import net.kyori.adventure.text.TextComponent;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Dynmap Integration update task. It draws the faction areas by scanning all factions.
 * It stores the factions and compares new ones to the old ones when updating, and if they're different updates them.
 *
 * @author Iterator
 */

public class DynmapUpdateTask implements EagleFactionsRunnableTask
{
    @Override
    public String getName()
    {
        return "eaglefactions-dynmap-update-task";
    }

    @Override
    public void run()
    {
        // Check old factions
        for (Iterator<Faction> iterator = DynmapService.drawnFactions.iterator(); iterator.hasNext();)
        {
            Faction drawnFaction = iterator.next();
            Faction faction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByName(drawnFaction.getName());

            if (faction == null || hasNewClaims(drawnFaction, faction) || hasNewHome(drawnFaction, faction))
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
                ServerWorld factionHomeWorld = Sponge.server().worldManager().worlds().stream()
                        .filter(serverWorld -> serverWorld.uniqueId().equals(faction.getHome().getWorldUUID()))
                        .findFirst().orElse(null);

                if (factionHomeWorld != null)
                {
                    Vector3i blockPos = faction.getHome().getBlockPosition();

                    Marker marker = DynmapService.markerSet.createMarker(null,
                            faction.getName() + " Home",
                            ((TextComponent)factionHomeWorld.properties().displayName().get()).content(),
                            blockPos.x(),
                            blockPos.y(),
                            blockPos.z(),
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
                    ServerWorld world = Sponge.server().worldManager().worlds().stream()
                        .filter(serverWorld -> serverWorld.uniqueId().equals(entry.getKey()))
                        .findFirst().orElse(null);

                    if (world == null) continue; /* Somehow there's no world for that area */

                    AreaMarker areaMarker = DynmapService.markerSet.createAreaMarker(null,
                            faction.getName(),
                            false,
                            ((TextComponent)world.properties().displayName().get()).content(),
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

    private boolean hasNewClaims(final Faction drawnFaction, final Faction faction)
    {
        return drawnFaction.getClaims().hashCode() != faction.getClaims().hashCode();
    }

    private boolean hasNewHome(final Faction drawnFaction, final Faction faction)
    {
        return !Objects.equals(drawnFaction.getHome(), faction.getHome());
    }
}
