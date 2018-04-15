package io.github.aquerr.eaglefactions.managers;

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

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerManager
{
    private static Path playersPath;

    public static void setup(Path configDir)
    {
        try
        {
            playersPath = configDir.resolve("players");
            if (!Files.exists(playersPath)) Files.createDirectory(playersPath);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

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

//    public static @Nullable Vector3i getPlayerBlockPosition(UUID playerUUID)
//    {
//        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");
//
//        try
//        {
//            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
//            CommentedConfigurationNode playerNode = configLoader.load();
//            ConfigurationNode chunkPositionNode = playerNode.getNode("chunkPosition");
//
//            if(chunkPositionNode.getValue() != null)
//            {
//                String object = chunkPositionNode.getString();
//
//                String vectors[] = object.replace("(", "").replace(")", "").replace(" ", "").split(",");
//
//                int x = Integer.valueOf(vectors[0]);
//                int y = Integer.valueOf(vectors[1]);
//                int z = Integer.valueOf(vectors[2]);
//
//                Vector3i chunk = Vector3i.from(x, y, z);
//
//                return chunk;
//            }
//            else
//            {
//                return null;
//            }
//        }
//        catch (Exception exception)
//        {
//            exception.printStackTrace();
//        }
//        return null;
//    }

//    public static void setPlayerBlockPosition(UUID playerUUID, @Nullable Vector3i chunk)
//    {
//        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");
//
//        try
//        {
//            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
//
//            CommentedConfigurationNode playerNode = configLoader.load();
//
//            if(chunk != null)
//            {
//                playerNode.getNode("chunkPosition").setValue(chunk.toString());
//            }
//            else
//            {
//                playerNode.getNode("chunkPosition").setValue(null);
//            }
//
//            configLoader.save(playerNode);
//        }
//        catch (Exception exception)
//        {
//            exception.printStackTrace();
//        }
//    }

    public static void setDeathInWarZone(UUID playerUUID, boolean didDieInWarZone)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("death-in-warzone").setValue(didDieInWarZone);

            configLoader.save(playerNode);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static boolean lastDeathAtWarZone(UUID playerUUID)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            Object value = playerNode.getNode("death-in-warzone").getValue();

            if (value != null)
            {
                return (boolean)value;
            }
            else
            {
                playerNode.getNode("death-in-warzone").setValue(false);
                configLoader.save(playerNode);
                return false;
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }
}
