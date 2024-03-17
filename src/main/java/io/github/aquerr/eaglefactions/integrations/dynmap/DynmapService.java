package io.github.aquerr.eaglefactions.integrations.dynmap;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.config.DynmapConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
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

    private final DynmapConfig dynmapConfig;
    private final PlayerManager playerManager;

    private ScheduledTask dynmapUpdateTask;

    public DynmapService(final DynmapConfig dynmapConfig,
                         final PlayerManager playerManager)
    {
        this.dynmapConfig = dynmapConfig;
        this.playerManager = playerManager;
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
                    EagleFactionsPlugin.getPlugin().getLogger().error("Could not create MarkerSet in Dynmap. Restarting the server may help.");
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

    public String getFactionInfoWindow(final Faction faction) throws ExecutionException, InterruptedException
    {
        // TODO: fix missing line breaks. Sometimes they are missing. I don't know why.

        PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();

        StringBuilder description = new StringBuilder();

        String factionName = faction.getName();
        String factionDesc = faction.getDescription();

        description.append("<div class=\"infowindow\">\n")
                .append("<span style=\"font-weight: bold; font-size: 150%;\">%name%</span></br>\n".replace("%name%", factionName))
                .append("<span style=\"font-style: italic; font-size: 110%;\">%description%</span></br>\n".replace("%description%", factionDesc.length() > 0 ? factionDesc : "No description"));


        if (plainTextComponentSerializer.serialize(faction.getTag()).length() > 0) {
            description.append("<span style=\"font-weight: bold;\">Tag:</span> %tag%</br>\n".replace("%tag%", plainTextComponentSerializer.serialize(faction.getTag())))
                    .append("</br>\n");
        }

        if (dynmapConfig.showDynmapFactionLeader())
        {
            ServerPlayer serverPlayer = faction.getLeader()
                    .map(FactionMember::getUniqueId)
                    .flatMap(playerManager::getPlayer)
                    .orElse(null);

            if (serverPlayer != null)
            {
                description.append("<span style=\"font-weight: bold;\">Leader:</span> %leader%</br>\n"
                        .replace("%leader%", serverPlayer.name()));
            }
        }

        if (dynmapConfig.showDynmapMemberInfo()) {
            int memberCount = faction.getMembers().size();
            description.append("<span style=\"font-weight: bold;\">Total members:</span> %players%</br>\n"
                    .replace("%players%",
                            String.valueOf(memberCount)));
        }

        description.append("</br>\n</div>");

        return description.toString();
    }

    public int getAreaColor(final Faction faction) {

        int areaColor;

        if(EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig().canColorTags())
        {
            areaColor = Integer.parseInt(faction.getTag().color().asHexString());
        }
        else
        {
            areaColor = dynmapConfig.getDynmapFactionColor();
        }

        if (faction.isSafeZone()) {
            areaColor = dynmapConfig.getDynmapSafezoneColor();
        } else if (faction.isWarZone()) {
            areaColor = dynmapConfig.getDynmapWarzoneColor();
        }

        return areaColor;
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
        this.dynmapUpdateTask = EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(
                new DynmapUpdateTask(this),
                0, TimeUnit.SECONDS,
                10, TimeUnit.SECONDS);
    }
}
