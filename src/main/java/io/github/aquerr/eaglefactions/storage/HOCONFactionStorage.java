package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class HOCONFactionStorage implements IStorage
{
    private Path filePath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    public HOCONFactionStorage(Path configDir)
    {
        try
        {
            Path dataPath = configDir.resolve("data");

            if (!Files.exists(dataPath))
            {
                Files.createDirectory(dataPath);
            }

            filePath = dataPath.resolve("factions.conf");

            if (!Files.exists(filePath))
            {
                Files.createFile(filePath);

                configLoader = HoconConfigurationLoader.builder().setPath(filePath).build();
                precreate();
            }
            else
            {
                configLoader = HoconConfigurationLoader.builder().setPath(filePath).build();
                load();
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    private void precreate()
    {
        load();
        getStorage().getNode("factions").setComment("This file stores all data about factions");

        getStorage().getNode("factions", "WarZone", "claims").setValue(new ArrayList<>());
        getStorage().getNode("factions", "WarZone", "members").setValue(new ArrayList<>());
        getStorage().getNode("factions", "WarZone", "power").setValue(9999);

        getStorage().getNode("factions", "SafeZone", "claims").setValue(new ArrayList<>());
        getStorage().getNode("factions", "SafeZone", "members").setValue(new ArrayList<>());
        getStorage().getNode("factions", "SafeZone", "power").setValue(9999);

        saveChanges();
    }

    @Override
    public boolean addOrUpdateFaction(Faction faction)
    {
        try
        {
            configNode.getNode(new Object[]{"factions", faction.Name, "tag"}).setValue(faction.Tag);
            configNode.getNode(new Object[]{"factions", faction.Name, "leader"}).setValue(faction.Leader);
            configNode.getNode(new Object[]{"factions", faction.Name, "officers"}).setValue(faction.Officers);
            configNode.getNode(new Object[]{"factions", faction.Name, "home"}).setValue(faction.Home);
            configNode.getNode(new Object[]{"factions", faction.Name, "members"}).setValue(faction.Members);
            configNode.getNode(new Object[]{"factions", faction.Name, "enemies"}).setValue(faction.Enemies);
            configNode.getNode(new Object[]{"factions", faction.Name, "alliances"}).setValue(faction.Alliances);
            configNode.getNode(new Object[]{"factions", faction.Name, "claims"}).setValue(faction.Claims);
            configNode.getNode(new Object[]{"factions", faction.Name, "flags"}).setValue(faction.Flags);

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
        try
        {
            if (configNode.getNode("factions", factionName).getValue() == null)
            {
                return null;
            }

            String tag = "";
            String leader = "";
            String home = "";
            List<String> officersList = new ArrayList<>();
            List<String> membersList = new ArrayList<>();
            List<String> enemiesList = new ArrayList<>();
            List<String> alliancesList = new ArrayList<>();
            List<String> claimsList = new ArrayList<>();
            Map<FactionMemberType, Map<FactionFlagType, Boolean>> flagsMap = new HashMap<>();

            Object tagObject = configNode.getNode(new Object[]{"factions", factionName, "tag"}).getValue();
            Object leaderObject = configNode.getNode(new Object[]{"factions", factionName, "leader"}).getValue();
            Object officersObject = configNode.getNode(new Object[]{"factions", factionName, "officers"}).getValue();
            Object homeObject = configNode.getNode(new Object[]{"factions", factionName, "home"}).getValue();
            Object membersObject = configNode.getNode(new Object[]{"factions", factionName, "members"}).getValue();
            Object enemiesObject = configNode.getNode(new Object[]{"factions", factionName, "enemies"}).getValue();
            Object alliancesObject = configNode.getNode(new Object[]{"factions", factionName, "alliances"}).getValue();
            Object claimsObject = configNode.getNode(new Object[]{"factions", factionName, "claims"}).getValue();
            Map<FactionMemberType, Map<FactionFlagType, Boolean>> flags = configNode.getNode(new Object[]{"factions", factionName, "flags"}).getValue(flagsTransformer);

            if (tagObject != null) tag = String.valueOf(tagObject);
            if (leaderObject != null) leader = String.valueOf(leaderObject);
            if (officersObject != null) officersList = (List<String>)officersObject;
            if (membersObject != null) membersList = (List<String>)membersObject;
            if (enemiesObject != null) enemiesList = (List<String>)enemiesObject;
            if (alliancesObject != null) alliancesList = (List<String>)alliancesObject;
            if (claimsObject != null) claimsList = (List<String>)claimsObject;
            if (homeObject != null) home = String.valueOf(homeObject);
            if (flags != null) flagsMap = flags;

            Faction faction = new Faction(factionName, tag, leader);
            faction.Home = home;
            faction.Officers = officersList;
            faction.Members = membersList;
            faction.Alliances = alliancesList;
            faction.Enemies = enemiesList;
            faction.Claims = claimsList;
            faction.Power = PowerManager.getFactionPower(faction); //Get power from all players in faction.
            faction.Flags = flagsMap;

            return faction;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

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
                List<Faction> factionList = new ArrayList<>();

                final Set<Object> keySet = getStorage().getNode("factions").getChildrenMap().keySet();

                for (Object object : keySet)
                {
                    if(object instanceof String)
                    {
                        Faction faction = getFaction(String.valueOf(object));

                        if (faction != null) factionList.add(faction);
                    }
                }

                return factionList;
            }
            catch (Exception exception)
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

    private Function<Object, Map<FactionMemberType, Map<FactionFlagType, Boolean>>> flagsTransformer = input ->
    {
        if (input != null)
        {
            Map<FactionMemberType, Map<FactionFlagType, Boolean>> resultMap = new HashMap<>();

            Map<Object, Object> memberMap = (Map<Object, Object>) input;

            for (Map.Entry<Object, Object> memberEntry : memberMap.entrySet())
            {
                Map<Object, Object> flagMap = (Map<Object, Object>)memberEntry.getValue();
                Map<FactionFlagType, Boolean> helpMap = new HashMap<>();

                for (Map.Entry<Object, Object> flagEntry : flagMap.entrySet())
                {
                    helpMap.put(FactionFlagType.valueOf(flagEntry.getKey().toString().toUpperCase()), (Boolean)flagEntry.getValue());
                }

                resultMap.put(FactionMemberType.valueOf(memberEntry.getKey().toString().toUpperCase()), helpMap);
            }

            return resultMap;
        }

        return null;
    };
}
