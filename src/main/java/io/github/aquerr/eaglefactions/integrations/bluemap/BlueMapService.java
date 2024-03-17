package io.github.aquerr.eaglefactions.integrations.bluemap;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.config.BluemapConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.integrations.bluemap.util.TempAreaMarker;
import io.github.aquerr.eaglefactions.integrations.bluemap.util.TileFlags;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BlueMapService
{
    private static final String CLAIMS_MARKER_SET_ID = "eaglefactions-claims";

    private final FactionLogic factionLogic;
    private final PlayerManager playerManager;
    private final BluemapConfig bluemapConfig;
    private BlueMapAPI blueMapAPI;

    private final Map<UUID, MarkerSet> markerSetsPerWorld = new HashMap<>();

    private final List<Faction> drawnFactions = new ArrayList<>();

    private ScheduledTask bluemapUpdateTask;

    public BlueMapService(BluemapConfig bluemapConfig,
                          PlayerManager playerManager,
                          FactionLogic factionLogic)
    {
        this.bluemapConfig = bluemapConfig;
        this.playerManager = playerManager;
        this.factionLogic = factionLogic;
    }

    public void activate()
    {
        BlueMapAPI.onEnable((blueMapAPI) -> {
            this.blueMapAPI = blueMapAPI;

            restartBluemapUpdateTask();
        });
    }

    public BlueMapAPI getBlueMapAPI()
    {
        return blueMapAPI;
    }

    public List<Faction> getDrawnFactions()
    {
        return drawnFactions;
    }

    private void restartBluemapUpdateTask()
    {
        if (this.bluemapUpdateTask != null)
        {
            this.bluemapUpdateTask.cancel();
        }
        this.bluemapUpdateTask = EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(
                new BlueMapUpdateTask(this, this.factionLogic),
                0, TimeUnit.MINUTES,
                1, TimeUnit.MINUTES);
    }

    public void reload()
    {
        this.markerSetsPerWorld.forEach((uuid, markerSet) -> {
            markerSet.getMarkers().clear();
        });

        this.drawnFactions.clear();
        this.markerSetsPerWorld.clear();

        restartBluemapUpdateTask();
    }

    public void deleteMarkersForFaction(Faction drawnFaction)
    {
        this.drawnFactions.remove(drawnFaction);
        this.markerSetsPerWorld.forEach((uuid, markerSet) -> {
            Set<Map.Entry<String, Marker>> markers = new HashSet<>(markerSet.getMarkers().entrySet());
            markers.forEach(markerEntry -> {
                if (markerEntry.getValue().getLabel().equals(drawnFaction.getName()))
                {
                    markerSet.remove(markerEntry.getKey());
                }
            });
        });
    }

    public void addMarkersForFaction(Faction faction)
    {
        if (faction.getClaims().isEmpty())
            return;

        createClaimMarkerShapeForFaction(faction);

        markerSetsPerWorld.forEach((worldUniqueId, markerSet) -> {
            blueMapAPI.getWorld(worldUniqueId).ifPresent(world -> world.getMaps().forEach(blueMapMap -> blueMapMap.getMarkerSets().put(CLAIMS_MARKER_SET_ID, markerSet)));
        });
    }

    private void createClaimMarkerShapeForFaction(Faction faction)
    {
        int index = 0;

        HashMap<UUID, Set<Claim>> claimsWorld = new HashMap<>();

        /* Now sorting the claims by their worlds */
        Claim[] claims = new Claim[faction.getClaims().size()];
        claims = faction.getClaims().toArray(claims);
        for (Claim claim : claims)
        {
            claimsWorld.computeIfAbsent(claim.getWorldUUID(), k -> new HashSet<>());

            claimsWorld.get(claim.getWorldUUID()).add(claim);
        }

        HashMap<UUID, ArrayList<TempAreaMarker>> areaMarkers = new HashMap<>();
        claimsWorld.forEach((k, v) ->
        {
            ArrayList<TempAreaMarker> tempMarkers = createAreas(v);

            areaMarkers.put(k, tempMarkers);
        });

        for (Map.Entry<UUID, ArrayList<TempAreaMarker>> entry : areaMarkers.entrySet())
        {
            for (TempAreaMarker tempMarker : entry.getValue())
            {
                ServerWorld world = WorldUtil.getWorldByUUID(entry.getKey())
                        .orElse(null);

                if (world == null) continue;

                List<Vector2d> vectors = toVector2DList(tempMarker.x, tempMarker.z);

                Shape shape = new Shape(vectors);

                ExtrudeMarker marker = ExtrudeMarker.builder()
                        .label(faction.getName())
                        .shape(shape, -64, 255)
                        .maxDistance(1000)
                        .fillColor(determineMarkerColor(faction, 0.3f))
                        .lineColor(determineMarkerColor(faction, 1))
                        .detail(prepareMarkerDetailsForFaction(faction))
                        .build();

                this.markerSetsPerWorld.computeIfAbsent(world.uniqueId(), k -> MarkerSet.builder()
                        .label("Eagle Factions Claims")
                        .toggleable(true)
                        .build());

                this.markerSetsPerWorld.get(world.uniqueId()).put(faction.getTag().content() + "-" + index, marker);
                index++;
            }
        }
    }

    private List<Vector2d> toVector2DList(double[] x, double[] z)
    {
        List<Vector2d> vectors = new ArrayList<>(x.length);
        for (int i = 0; i < x.length; i++)
        {
            vectors.add(Vector2d.from(x[i], z[i]));
        }
        return vectors;
    }

    public String prepareMarkerDetailsForFaction(final Faction faction)
    {
        // TODO: fix missing line breaks. Sometimes they are missing. I don't know why.
        StringBuilder description = new StringBuilder();

        String factionName = faction.getName();
        String factionDesc = faction.getDescription();

        description.append("<div class=\"infowindow\">\n")
                .append("<span style=\"font-weight: bold; font-size: 150%;\">%name%</span></br>\n".replace("%name%", factionName))
                .append("<span style=\"font-style: italic; font-size: 110%;\">%description%</span></br>\n".replace("%description%", factionDesc.length() > 0 ? factionDesc : "No description"));


        if (PlainTextComponentSerializer.plainText().serialize(faction.getTag()).length() > 0) {
            description.append("<span style=\"font-weight: bold;\">Tag:</span> %tag%</br>\n".replace("%tag%", PlainTextComponentSerializer.plainText().serialize(faction.getTag())))
                    .append("</br>\n");
        }

        if (this.bluemapConfig.showBluemapFactionLeader())
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

        if (this.bluemapConfig.showBluemapMemberInfo()) {
            int memberCount = faction.getMembers().size();
            description.append("<span style=\"font-weight: bold;\">Total members:</span> %players%</br>\n"
                    .replace("%players%",
                            String.valueOf(memberCount)));
        }

        description.append("</br>\n</div>");

        return description.toString();
    }

    private Color determineMarkerColor(final Faction faction, final float alpha) {

        int areaColor;
        if(EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig().canColorTags())
        {
            areaColor = Optional.ofNullable(faction.getTag().color())
                    .map(TextColor::asHexString)
                    .map(Integer::parseInt)
                    .orElse(this.bluemapConfig.getBluemapFactionColor());
        }
        else
        {
            areaColor = this.bluemapConfig.getBluemapFactionColor();
        }
        if (faction.isSafeZone())
        {
            areaColor = this.bluemapConfig.getBluemapSafezoneColor();
        }
        else if (faction.isWarZone())
        {
            areaColor = this.bluemapConfig.getBluemapWarzoneColor();
        }
        return new Color(areaColor, alpha);
    }

    private void floodFillTarget(TileFlags source, TileFlags destination, int x, int y)
    {
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] { x, y });

        while (!stack.isEmpty())
        {
            int[] nxt = stack.pop();
            x = nxt[0];
            y = nxt[1];
            if (source.getFlag(x, y))
            { // Set in src
                source.setFlag(x, y, false); // Clear source
                destination.setFlag(x, y, true); // Set in destination
                if (source.getFlag(x + 1, y)) stack.push(new int[] { x + 1, y });
                if (source.getFlag(x - 1, y)) stack.push(new int[] { x - 1, y });
                if (source.getFlag(x, y + 1)) stack.push(new int[] { x, y + 1 });
                if (source.getFlag(x, y - 1)) stack.push(new int[] { x, y - 1 });
            }
        }
    }

    public ArrayList<TempAreaMarker> createAreas(Set<Claim> chunks)
    {
        ArrayList<TempAreaMarker> ret = new ArrayList<>();

        // Loop through chunks: set flags on chunk map
        TileFlags allChunkFlags = new TileFlags();
        LinkedList<Claim> allChunks = new LinkedList<Claim>();
        for (Claim chunk : chunks)
        {
            allChunkFlags.setFlag(chunk.getChunkPosition().x(), chunk.getChunkPosition().z(), true); // Set flag for chunk
            allChunks.addLast(chunk);
        }

        // Loop through until we don't find more areas
        while (allChunks != null)
        {
            TileFlags ourChunkFlags = null;
            LinkedList<Claim> ourChunks = null;
            LinkedList<Claim> newChunks = null;

            int minimumX = Integer.MAX_VALUE;
            int minimumZ = Integer.MAX_VALUE;
            for (Claim chunk : allChunks)
            {
                int chunkX = chunk.getChunkPosition().x();
                int chunkZ = chunk.getChunkPosition().z();

                // If we need to start shape, and this block is not part of one yet
                if (ourChunkFlags == null && allChunkFlags.getFlag(chunkX, chunkZ))
                {
                    ourChunkFlags = new TileFlags(); // Create map for shape
                    ourChunks = new LinkedList<Claim>();
                    floodFillTarget(allChunkFlags, ourChunkFlags, chunkX, chunkZ); // Copy shape
                    ourChunks.add(chunk); // Add it to our chunk list
                    minimumX = chunkX;
                    minimumZ = chunkZ;
                }
                // If shape found, and we're in it, add to our node list
                else if (ourChunkFlags != null && ourChunkFlags.getFlag(chunkX, chunkZ))
                {
                    ourChunks.add(chunk);
                    if (chunkX < minimumX)
                    {
                        minimumX = chunkX;
                        minimumZ = chunkZ;
                    }
                    else if (chunkX == minimumX && chunkZ < minimumZ)
                    {
                        minimumZ = chunkZ;
                    }
                }
                // Else, keep it in the list for the next polygon
                else
                {
                    if (newChunks == null) newChunks = new LinkedList<>();
                    newChunks.add(chunk);
                }
            }

            // Replace list (null if no more to process)
            allChunks = newChunks;

            if (ourChunkFlags == null) continue;

            // Trace outline of blocks - start from minx, minz going to x+
            int initialX = minimumX;
            int initialZ = minimumZ;
            int currentX = minimumX;
            int currentZ = minimumZ;
            Direction direction = Direction.XPLUS;
            ArrayList<int[]> linelist = new ArrayList<>();
            linelist.add(new int[]{ initialX, initialZ }); // Add start point
            while ((currentX != initialX) || (currentZ != initialZ) || (direction != Direction.ZMINUS))
            {
                switch (direction)
                {
                    case XPLUS: // Segment in X+ direction
                        if (!ourChunkFlags.getFlag(currentX + 1, currentZ))
                        { // Right turn?
                            linelist.add(new int[]{ currentX + 1, currentZ }); // Finish line
                            direction = Direction.ZPLUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX + 1, currentZ - 1))
                        { // Straight?
                            currentX++;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[]{ currentX + 1, currentZ }); // Finish line
                            direction = Direction.ZMINUS;
                            currentX++;
                            currentZ--;
                        }
                        break;
                    case ZPLUS: // Segment in Z+ direction
                        if (!ourChunkFlags.getFlag(currentX, currentZ + 1))
                        { // Right turn?
                            linelist.add(new int[]{ currentX + 1, currentZ + 1 }); // Finish line
                            direction = Direction.XMINUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX + 1, currentZ + 1))
                        { // Straight?
                            currentZ++;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[]{ currentX + 1, currentZ + 1 }); // Finish line
                            direction = Direction.XPLUS;
                            currentX++;
                            currentZ++;
                        }
                        break;
                    case XMINUS: // Segment in X- direction
                        if (!ourChunkFlags.getFlag(currentX - 1, currentZ))
                        { // Right turn?
                            linelist.add(new int[]{ currentX, currentZ + 1 }); // Finish line
                            direction = Direction.ZMINUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX - 1, currentZ + 1))
                        { // Straight?
                            currentX--;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[] { currentX, currentZ + 1 }); // Finish line
                            direction = Direction.ZPLUS;
                            currentX--;
                            currentZ++;
                        }
                        break;
                    case ZMINUS: // Segment in Z- direction
                        if (!ourChunkFlags.getFlag(currentX, currentZ - 1))
                        { // Right turn?
                            linelist.add(new int[]{ currentX, currentZ }); // Finish line
                            direction = Direction.XPLUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX - 1, currentZ - 1))
                        { // Straight?
                            currentZ--;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[] { currentX, currentZ }); // Finish line
                            direction = Direction.XMINUS;
                            currentX--;
                            currentZ--;
                        }
                        break;
                }
            }

            int sz = linelist.size();
            double[] x = new double[sz];
            double[] z = new double[sz];
            for (int i = 0; i < sz; i++)
            {
                int[] line = linelist.get(i);
                x[i] = (double) line[0] * (double) 16;
                z[i] = (double) line[1] * (double) 16;
            }

            TempAreaMarker temp = new TempAreaMarker(x, z);

            ret.add(temp);
        }

        return ret;
    }

    enum Direction
    {
        XPLUS, ZPLUS, XMINUS, ZMINUS
    }
}
