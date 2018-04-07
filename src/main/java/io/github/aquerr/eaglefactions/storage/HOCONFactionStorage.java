package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

            Object tagObject = configNode.getNode(new Object[]{"factions", factionName, "tag"}).getValue();
            Object leaderObject = configNode.getNode(new Object[]{"factions", factionName, "leader"}).getValue();
            Object officersObject = configNode.getNode(new Object[]{"factions", factionName, "officers"}).getValue();
            Object homeObject = configNode.getNode(new Object[]{"factions", factionName, "home"}).getValue(); //TODO: Add new home property in Faction class.
            Object membersObject = configNode.getNode(new Object[]{"factions", factionName, "members"}).getValue();
            Object enemiesObject = configNode.getNode(new Object[]{"factions", factionName, "enemies"}).getValue();
            Object alliancesObject = configNode.getNode(new Object[]{"factions", factionName, "alliances"}).getValue();
            Object claimsObject = configNode.getNode(new Object[]{"factions", factionName, "claims"}).getValue();

            if (tagObject != null) tag = String.valueOf(tagObject);
            if (leaderObject != null) leader = String.valueOf(leaderObject);
            if (officersObject != null) officersList = (List<String>)officersObject;
            if (membersObject != null) membersList = (List<String>)membersObject;
            if (enemiesObject != null) enemiesList = (List<String>)enemiesObject;
            if (alliancesObject != null) alliancesList = (List<String>)alliancesObject;
            if (claimsObject != null) claimsList = (List<String>)claimsObject;
            if (homeObject != null) home = String.valueOf(homeObject);

            Faction faction = new Faction(factionName, tag, leader);
            faction.Home = home;
            faction.Officers = officersList;
            faction.Members = membersList;
            faction.Alliances = alliancesList;
            faction.Enemies = enemiesList;
            faction.Claims = claimsList;
            faction.Power = PowerManager.getFactionPower(faction); //Get power from all players in faction.

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

    private Function<Object, Faction> objectToFactionTransformer = new Function<Object, Faction>()
    {
        @Override
        public Faction apply(Object input)
        {
            if (input != null)
            {
                Map<String, String> map = (Map<String, String>)input;

                map.get("key");

                return null;
            }

            return null;
        }
    };
}
