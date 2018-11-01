package io.github.aquerr.eaglefactions.storage.hocon;

import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.storage.IPlayerStorage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HOCONPlayerStorage implements IPlayerStorage
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
    public boolean checkIfPlayerExists(UUID playerUUID, String playerName)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");
        return Files.exists(playerFile);
    }

    @Override
    public boolean addPlayer(UUID playerUUID, String playerName, BigDecimal startingPower, BigDecimal maxPower)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            configurationNode.getNode("name").setValue(playerName);
            configurationNode.getNode("power").setValue(startingPower);
            configurationNode.getNode("maxpower").setValue(maxPower);
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
    public boolean setDeathInWarzone(UUID playerUUID, boolean didDieInWarZone)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        if(!Files.exists(playerFile))
            return false;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            configurationNode.getNode("death-in-warzone").setValue(didDieInWarZone);
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
    public boolean getLastDeathInWarzone(UUID playerUUID)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        if(!Files.exists(playerFile))
            return false;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            return configurationNode.getNode("death-in-warzone").getBoolean();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public BigDecimal getPlayerPower(UUID playerUUID)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        if(!Files.exists(playerFile))
            return BigDecimal.ZERO;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            return new BigDecimal(configurationNode.getNode("power").getString());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    @Override
    public boolean setPlayerPower(UUID playerUUID, BigDecimal power)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        if(!Files.exists(playerFile))
            return false;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            configurationNode.getNode("power").setValue(power.toString());
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
    public BigDecimal getPlayerMaxPower(UUID playerUUID)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        if(!Files.exists(playerFile))
            return BigDecimal.ZERO;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            return new BigDecimal(configurationNode.getNode("maxpower").getString());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    @Override
    public boolean setPlayerMaxPower(UUID playerUUID, BigDecimal maxpower)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        if(!Files.exists(playerFile))
            return false;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            configurationNode.getNode("maxpower").setValue(maxpower.toString());
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
    public Set<String> getServerPlayerNames()
    {
        Set<String> playerSet = new HashSet<>();

        File playerDir = new File(playersDirectoryPath.toUri());
        File[] playerFiles = playerDir.listFiles();

        for(File playerFile : playerFiles)
        {
            HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile.toPath()).build();
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
    public Set<IFactionPlayer> getServerPlayers()
    {
        Set<IFactionPlayer> playerSet = new HashSet<>();

        File playerDir = new File(playersDirectoryPath.toUri());
        File[] playerFiles = playerDir.listFiles();

        for(File playerFile : playerFiles)
        {
            HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile.toPath()).build();
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
                FactionMemberType factionMemberType = null;

                if(!factionMemberTypeString.equals(""))
                    factionMemberType = FactionMemberType.valueOf(factionMemberTypeString);

                FactionPlayer factionPlayer = new FactionPlayer(playerName, playerUUID, factionName, factionMemberType);
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
    public String getPlayerName(UUID playerUUID)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        try
        {
            ConfigurationNode configurationNode = configurationLoader.load();
            return configurationNode.getNode("name").getString("");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void updatePlayerName(UUID playerUUID, String playerName)
    {
        Path playerFile = playersDirectoryPath.resolve(playerUUID.toString() + ".conf");
        if(!Files.exists(playerFile))
            return;

        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
        ConfigurationNode configurationNode = null;
        try
        {
            configurationNode = configurationLoader.load();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        Object playerNameInFile = configurationNode.getNode("name").getValue();
        if(playerNameInFile != null)
        {
            String oldPlayerName = (String) playerNameInFile;
            if(!oldPlayerName.equals(playerName))
            {
                configurationNode.getNode("name").setValue(playerName);
                try
                {
                    configurationLoader.save(configurationNode);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
