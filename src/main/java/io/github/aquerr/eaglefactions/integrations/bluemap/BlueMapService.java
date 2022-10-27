package io.github.aquerr.eaglefactions.integrations.bluemap;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BlueMapService
{
    private static final String CLAIMS_MARKER_SET_ID = "eaglefactions-claims";

    private BlueMapAPI blueMapAPI;
    private MarkerSet claimsMarkerSet;

    private final List<Faction> drawnFactions = new ArrayList<>();

    private ScheduledTask bluemapUpdateTask;

    public void activate()
    {
        BlueMapAPI.onEnable((blueMapAPI) -> {
            this.blueMapAPI = blueMapAPI;

            this.claimsMarkerSet = MarkerSet.builder()
                    .label("Eagle Factions Claims")
                    .toggleable(true)
                    .build();

            restartBluemapUpdateTask();
        });
    }

    public BlueMapAPI getBlueMapAPI()
    {
        return blueMapAPI;
    }

    public MarkerSet getClaimsMarkerSet()
    {
        return claimsMarkerSet;
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
        this.bluemapUpdateTask = EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(new BlueMapUpdateTask(this), 0, TimeUnit.MINUTES, 1, TimeUnit.MINUTES);
    }

    private void saveMarkers()
    {
        try (FileWriter writer = new FileWriter("marker-file.json")) {
            MarkerGson.INSTANCE.toJson(claimsMarkerSet, writer);
        } catch (IOException ex) {
            // handle io-exception
            ex.printStackTrace();
        }
    }

    private void loadMarkers()
    {
        try (FileReader reader = new FileReader("marker-file.json")) {
            claimsMarkerSet = MarkerGson.INSTANCE.fromJson(reader, MarkerSet.class);
        } catch (IOException ex) {
            // handle io-exception
            ex.printStackTrace();
        }
    }

    public void deleteMarkersForFaction(Faction drawnFaction)
    {
        this.drawnFactions.remove(drawnFaction);
        this.claimsMarkerSet.remove(drawnFaction.getTag().content());
    }

    public void addMarkersForFaction(Faction faction)
    {
        for (final Claim claim : faction.getClaims())
        {
            Vector3i chunkPosition = claim.getChunkPosition();

            ShapeMarker marker = ShapeMarker.builder()
                    .label(faction.getName())
//                    .shape(new Shape()) //TODO: ...
                    .position(chunkPosition.x(), chunkPosition.y(), chunkPosition.z())
                    .maxDistance(1000)
                    .build();

            claimsMarkerSet.put(faction.getTag().content(), marker);

            blueMapAPI.getMaps().forEach(world -> {
                for (BlueMapMap map : world.getWorld().getMaps()) {
                    map.getMarkerSets().put(CLAIMS_MARKER_SET_ID, claimsMarkerSet);
                }
            });
        }
    }

    private Shape prepareClaimMarkerShapeForFaction(Faction faction)
    {
        List<Vector2d> points = new ArrayList<>();
        for (final Claim claim : faction.getClaims())
        {
            points.add(Vector2d.from(claim.getChunkPosition().x() * 16, claim.getChunkPosition().z() * 16));
            points.add(Vector2d.from((claim.getChunkPosition().x() * 16) - 16, (claim.getChunkPosition().z() * 16) - 16));
        }
        return new Shape(points);
    }
}
