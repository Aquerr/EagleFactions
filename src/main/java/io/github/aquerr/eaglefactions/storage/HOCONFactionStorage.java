package io.github.aquerr.eaglefactions.storage;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.entities.Faction;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HOCONFactionStorage implements IStorage
{
    private Path filePath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    public HOCONFactionStorage(Path configDir)
    {
        try
        {
            filePath = Paths.get(configDir.resolve("data") + "/factions.conf");

            if (!Files.exists(filePath)) Files.createFile(filePath);

            configLoader = HoconConfigurationLoader.builder().setPath(filePath).build();

            preload();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    private void preload()
    {
        getStorage().getNode("factions").setComment("This file stores all data about factions");

        getStorage().getNode("factions", "WarZone", "claims").setValue(new ArrayList<>());
        getStorage().getNode("factions", "WarZone", "members").setValue(new ArrayList<>());
        getStorage().getNode("factions", "WarZone", "power").setValue(9999);

        getStorage().getNode("factions", "SafeZone", "claims").setValue(new ArrayList<>());
        getStorage().getNode("factions", "SafeZone", "members").setValue(new ArrayList<>());
        getStorage().getNode("factions", "SafeZone", "power").setValue(9999);

        load();
    }

    @Override
    public boolean addOrUpdateFaction(Faction faction)
    {
        try
        {
            configNode.getNode(new Object[]{"factions", faction.Name, "tag"}, faction.Tag);
            configNode.getNode(new Object[]{"factions", faction.Name, "leader"},faction.Leader);
            configNode.getNode(new Object[]{"factions", faction.Name, "officers"},faction.Officers);
            configNode.getNode(new Object[]{"factions", faction.Name, "home"}, null); //TODO: Add new home property in Faction class.
            configNode.getNode(new Object[]{"factions", faction.Name, "members"}, faction.Members);
            configNode.getNode(new Object[]{"factions", faction.Name, "enemies"}, faction.Enemies);
            configNode.getNode(new Object[]{"factions", faction.Name, "alliances"}, faction.Alliances);
            configNode.getNode(new Object[]{"factions", faction.Name, "claims"}, faction.Claims);

            return saveChanges();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeFaction(String factionName)
    {
        try
        {
            configNode.getNode("factions").removeChild(factionName);
            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public @Nullable Faction getFaction(String factionName)
    {
        Object object = configNode.getNode("factions", factionName).getValue();
        String test = "";

        //If it was not possible to get a faction then return null.
        return null;
    }

    @Override
    public List<Faction> getFactions()
    {
        if (getStorage().getNode("factions").getValue() != null)
        {
            try
            {
                final List<Faction> test = getStorage().getNode("factions").getList(TypeToken.of(Faction.class));
                String lol = "test";
            }
            catch (ObjectMappingException exception)
            {
                exception.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void load()
    {
        try
        {
            configNode = configLoader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean saveChanges()
    {
        try
        {
            configLoader.save(configNode);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private CommentedConfigurationNode getStorage()
    {
        return configNode;
    }
}
