package io.github.aquerr.eaglefactions.services;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerService
{
    public static Optional<String> getPlayerName(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        return Optional.of(oUser.get().getName());
    }

    public static Optional<Player> getPlayer(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        return oUser.get().getPlayer();
    }

    private static Optional<User> getUser(UUID playerUUID)
    {
        UserStorageService userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> oUser = userStorageService.get(playerUUID);

        if(oUser.isPresent())
        {
            return oUser;
        }
        else return Optional.empty();
    }

    public static boolean isPlayerOnline(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        if(oUser.isPresent())
        {
            return oUser.get().isOnline();
        }
        else return false;
    }

    public static Vector3i getPlayerChunkPosition(UUID playerUUID)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
            CommentedConfigurationNode playerNode = configLoader.load();
            ConfigurationNode chunkPositionNode = playerNode.getNode("chunkPosition");

            if(chunkPositionNode.getValue() != null)
            {
                String object = chunkPositionNode.getString();

                String vectors[] = object.replace("(", "").replace(")", "").replace(" ", "").split(",");

                int x = Integer.valueOf(vectors[0]);
                int y = Integer.valueOf(vectors[1]);
                int z = Integer.valueOf(vectors[2]);

                Vector3i chunk = Vector3i.from(x, y, z);

                return chunk;
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    public static void setPlayerChunkPosition(UUID playerUUID, Vector3i chunk)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("chunkPosition").setValue(chunk.toString());
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
