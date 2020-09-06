package io.github.aquerr.eaglefactions.common.storage.file.hocon;

import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.common.storage.PlayerStorage;
import io.github.aquerr.eaglefactions.common.util.FileUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HOCONPlayerStorage implements PlayerStorage
{
    private Path playersDirectoryPath;

    public HOCONPlayerStorage(Path configDir)
    {
        try
        {
            playersDirectoryPath = configDir.resolve("players");

            if(!Files.exists(playersDirectoryPath))
            {
                Files.createDirectory(playersDirectoryPath);
            }
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public FactionPlayer getPlayer(final UUID playerUUID)
    {
        final Path playerFilePath = this.playersDirectoryPath.resolve(playerUUID.toString() + ".conf");
        if (Files.notExists(playerFilePath))
            return null;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(playerFilePath).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            String playerName = configurationNode.getNode("name").getString("");
            String factionName = configurationNode.getNode("faction").getString("");
            String factionMemberTypeString = configurationNode.getNode("faction-member-type").getString("");
            float power = configurationNode.getNode("power").getFloat(0.0f);
            float maxpower = configurationNode.getNode("maxpower").getFloat(0.0f);
            boolean diedInWarZone = configurationNode.getNode("death-in-warzone").getBoolean(false);
            FactionMemberType factionMemberType = null;

            if(!factionMemberTypeString.equals(""))
                factionMemberType = FactionMemberType.valueOf(factionMemberTypeString);

            return new FactionPlayerImpl(playerName, playerUUID, factionName, power, maxpower, factionMemberType, diedInWarZone);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not get player from the file. Tried to get player for UUID: " + playerUUID)));
        }
        return null;
    }

    @Override
    public boolean savePlayer(FactionPlayer player)
    {

        try
        {
            FileUtils.createDirectoryIfNotExists(this.playersDirectoryPath);

            Path playerFile = playersDirectoryPath.resolve(player.getUniqueId().toString() + ".conf");
            HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(playerFile).build();

            ConfigurationNode configurationNode = configurationLoader.load();
            configurationNode.getNode("name").setValue(player.getName());
            configurationNode.getNode("faction").setValue(player.getFactionName().orElse(""));
            configurationNode.getNode("power").setValue(player.getPower());
            configurationNode.getNode("maxpower").setValue(player.getMaxPower());
            configurationNode.getNode("death-in-warzone").setValue(false);
            configurationLoader.save(configurationNode);
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean savePlayers(List<FactionPlayer> players)
    {
        for (final FactionPlayer player : players)
        {
            savePlayer(player);
        }
        return true;
    }

    @Override
    public Set<String> getServerPlayerNames()
    {
        Set<String> playerSet = new HashSet<>();

        File playerDir = new File(playersDirectoryPath.toUri());
        File[] playerFiles = playerDir.listFiles();

        for(File playerFile : playerFiles)
        {
            HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(playerFile.toPath()).build();
            try
            {
                ConfigurationNode configurationNode = configurationLoader.load();
                playerSet.add(configurationNode.getNode("name").getString(""));
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return playerSet;
    }

    @Override
    public Set<FactionPlayer> getServerPlayers()
    {
        final Set<FactionPlayer> playerSet = new HashSet<>();
        final File playerDir = new File(playersDirectoryPath.toUri());
        final File[] playerFiles = playerDir.listFiles();

        for(File playerFile : playerFiles)
        {
            HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(playerFile.toPath()).build();
            try
            {
                ConfigurationNode configurationNode = configurationLoader.load();
                String playerName = configurationNode.getNode("name").getString("");
                UUID playerUUID;
                try
                {
                    playerUUID = UUID.fromString(playerFile.getName().substring(0, playerFile.getName().indexOf('.')));

                }
                catch(Exception exception)
                {
                    exception.printStackTrace();
                    Files.delete(playerFile.toPath());
                    continue;
                }
                String factionName = configurationNode.getNode("faction").getString("");
                String factionMemberTypeString = configurationNode.getNode("faction-member-type").getString("");
                float power = configurationNode.getNode("power").getFloat(0.0f);
                float maxpower = configurationNode.getNode("maxpower").getFloat(0.0f);
                boolean diedInWarZone = configurationNode.getNode("death-in-warzone").getBoolean(false);
                FactionMemberType factionMemberType = null;

                if(!factionMemberTypeString.equals(""))
                    factionMemberType = FactionMemberType.valueOf(factionMemberTypeString);

                FactionPlayer factionPlayer = new FactionPlayerImpl(playerName, playerUUID, factionName, power, maxpower, factionMemberType, diedInWarZone);
                playerSet.add(factionPlayer);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return playerSet;
    }

    @Override
    public void deletePlayers()
    {
        final File[] files = this.playersDirectoryPath.toFile().listFiles();
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++)
        {
            files[i].delete();
        }
    }
}
