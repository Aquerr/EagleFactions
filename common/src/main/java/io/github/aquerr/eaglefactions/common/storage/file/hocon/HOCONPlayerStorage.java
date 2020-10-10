package io.github.aquerr.eaglefactions.common.storage.file.hocon;

import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.storage.PlayerStorage;
import io.github.aquerr.eaglefactions.common.util.FileUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

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

        return ConfigurateHelper.getPlayerFromFile(playerFilePath.toFile());
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

        if (playerFiles == null)
            return playerSet;

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

        if (playerFiles == null)
            return playerSet;

        for(File playerFile : playerFiles)
        {
            final FactionPlayer factionPlayer = ConfigurateHelper.getPlayerFromFile(playerFile);
            if (factionPlayer != null)
                playerSet.add(factionPlayer);
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
