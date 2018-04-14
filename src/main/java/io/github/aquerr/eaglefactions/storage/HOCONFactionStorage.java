package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
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

            String tag = getFactionTag(factionName);
            String leader = getFactionLeader(factionName);
            String home = getFactionHome(factionName);
            List<String> officers = getFactionOfficers(factionName);
            List<String> members = getFactionMembers(factionName);
            List<String> alliances = getFactionAlliances(factionName);
            List<String> enemies = getFactionEnemies(factionName);
            List<String> claims = getFactionClaims(factionName);
            Map<FactionMemberType, Map<FactionFlagType, Boolean>> flags = getFactionFlags(factionName);

            Faction faction = new Faction(factionName, tag, leader, members, claims, officers, alliances, enemies, home, flags);

            //TODO: Refactor this code so that the power can be sended to the faction constructor like other parameters.
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

    private Map<FactionMemberType,Map<FactionFlagType,Boolean>> getFactionFlags(String factionName)
    {
        Map<FactionMemberType, Map<FactionFlagType, Boolean>> flagMap = new LinkedHashMap<>();

        Map<FactionFlagType, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagType, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagType, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagType, Boolean> allyMap = new LinkedHashMap<>();

        //Get leader flags
        Object leaderUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "USE"}).getValue();
        Object leaderPLACE = configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "PLACE"}).getValue();
        Object leaderDESTROY = configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "DESTROY"}).getValue();

        if (leaderUSE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "USE"}).setValue(true);
            leaderUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "USE"}).getValue();
        }
        if (leaderPLACE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "PLACE"}).setValue(true);
            leaderPLACE = configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "PLACE"}).getValue();
        }
        if (leaderDESTROY == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "DESTROY"}).setValue(true);
            leaderDESTROY = configNode.getNode(new Object[]{"factions", factionName, "flags", "LEADER", "DESTROY"}).getValue();
        }

        //Get officer flags
        Object officerUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "USE"}).getValue();
        Object officerPLACE = configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "PLACE"}).getValue();
        Object officerDESTROY = configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "DESTROY"}).getValue();

        if (officerUSE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "USE"}).setValue(true);
            officerUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "USE"}).getValue();
        }
        if (officerPLACE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "PLACE"}).setValue(true);
            officerUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "PLACE"}).getValue();
        }
        if (officerDESTROY == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "DESTROY"}).setValue(true);
            officerUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "OFFICER", "DESTROY"}).getValue();
        }

        //Get member flags
        Object memberUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "USE"}).getValue();
        Object memberPLACE = configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "PLACE"}).getValue();
        Object memberDESTROY = configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "DESTROY"}).getValue();

        if (memberUSE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "USE"}).setValue(true);
            memberUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "USE"}).getValue();
        }
        if (memberPLACE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "PLACE"}).setValue(true);
            memberPLACE = configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "PLACE"}).getValue();
        }
        if (memberDESTROY == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "DESTROY"}).setValue(true);
            memberDESTROY = configNode.getNode(new Object[]{"factions", factionName, "flags", "MEMBER", "DESTROY"}).getValue();
        }

        //Get member flags
        Object allyUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "USE"}).getValue();
        Object allyPLACE = configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "PLACE"}).getValue();
        Object allyDESTROY = configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "DESTROY"}).getValue();

        if (allyUSE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "USE"}).setValue(true);
            allyUSE = configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "USE"}).getValue();
        }
        if (allyPLACE == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "PLACE"}).setValue(false);
            allyPLACE = configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "PLACE"}).getValue();
        }
        if (allyDESTROY == null)
        {
            configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "DESTROY"}).setValue(false);
            allyDESTROY = configNode.getNode(new Object[]{"factions", factionName, "flags", "ALLY", "DESTROY"}).getValue();
        }

        leaderMap.put(FactionFlagType.USE, (boolean)leaderUSE);
        leaderMap.put(FactionFlagType.PLACE, (boolean)leaderPLACE);
        leaderMap.put(FactionFlagType.DESTROY, (boolean)leaderDESTROY);

        officerMap.put(FactionFlagType.USE, (boolean)officerUSE);
        officerMap.put(FactionFlagType.PLACE, (boolean)officerPLACE);
        officerMap.put(FactionFlagType.DESTROY, (boolean)officerDESTROY);

        membersMap.put(FactionFlagType.USE, (boolean)memberUSE);
        membersMap.put(FactionFlagType.PLACE, (boolean)memberPLACE);
        membersMap.put(FactionFlagType.DESTROY, (boolean)memberDESTROY);

        allyMap.put(FactionFlagType.USE, (boolean)allyUSE);
        allyMap.put(FactionFlagType.PLACE, (boolean)allyPLACE);
        allyMap.put(FactionFlagType.DESTROY, (boolean)allyDESTROY);

        flagMap.put(FactionMemberType.LEADER, leaderMap);
        flagMap.put(FactionMemberType.OFFICER, officerMap);
        flagMap.put(FactionMemberType.MEMBER, membersMap);
        flagMap.put(FactionMemberType.ALLY, allyMap);

        return flagMap;

//        Map<FactionMemberType, Map<FactionFlagType, Boolean>> flagMap = configNode.getNode(new Object[]{"factions", factionName, "flags"}).getValue(flagsTransformer);
//
//        if (flagMap != null && areFlagsComplete(flagMap))
//        {
//            return sortFlags(flagMap);
//        }
//        else
//        {
//            //TODO: Sort map here...
//            configNode.getNode(new Object[]{"factions", factionName, "flags"}).setValue(FlagManager.getDefaultFactionFlags());
//            saveChanges();
//            return FlagManager.getDefaultFactionFlags();
//        }
    }

    private List<String> getFactionClaims(String factionName)
    {
        Object claimsObject = configNode.getNode(new Object[]{"factions", factionName, "claims"}).getValue();

        if (claimsObject != null)
        {
            return (List<String>)claimsObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "claims"}).setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private List<String> getFactionEnemies(String factionName)
    {
        Object enemiesObject = configNode.getNode(new Object[]{"factions", factionName, "enemies"}).getValue();

        if (enemiesObject != null)
        {
            return (List<String>)enemiesObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "enemies"}).setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private List<String> getFactionAlliances(String factionName)
    {
        Object alliancesObject = configNode.getNode(new Object[]{"factions", factionName, "alliances"}).getValue();

        if (alliancesObject != null)
        {
            return (List<String>)alliancesObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "alliances"}).setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private List<String> getFactionMembers(String factionName)
    {
        Object membersObject = configNode.getNode(new Object[]{"factions", factionName, "members"}).getValue();

        if (membersObject != null)
        {
            return (List<String>)membersObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "members"}).setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private String getFactionHome(String factionName)
    {
        Object homeObject = configNode.getNode(new Object[]{"factions", factionName, "home"}).getValue();

        if (homeObject != null)
        {
            return String.valueOf(homeObject);
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "home"}).setValue("");
            saveChanges();
            return "";
        }
    }

    private List<String> getFactionOfficers(String factionName)
    {
        Object officersObject = configNode.getNode(new Object[]{"factions", factionName, "officers"}).getValue();

        if (officersObject != null)
        {
            return (List<String>)officersObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "officers"}).setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private String getFactionLeader(String factionName)
    {
        Object tagObject = configNode.getNode(new Object[]{"factions", factionName, "leader"}).getValue();

        if (tagObject != null)
        {
            return String.valueOf(tagObject);
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "leader"}).setValue("");
            saveChanges();
            return "";
        }
    }

    private String getFactionTag(String factionName)
    {
        Object tagObject = configNode.getNode(new Object[]{"factions", factionName, "tag"}).getValue();

        if (tagObject != null)
        {
            return String.valueOf(tagObject);
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "tag"}).setValue("");
            saveChanges();
            return "";
        }
    }

//    private boolean areFlagsComplete(Map<FactionMemberType,Map<FactionFlagType,Boolean>> flags)
//    {
//        if (!flags.containsKey(FactionMemberType.LEADER))
//        {
//            return false;
//        }
//        else
//        {
//            if (!flags.get(FactionMemberType.LEADER).containsKey(FactionFlagType.USE)
//                    || !flags.get(FactionMemberType.LEADER).containsKey(FactionFlagType.PLACE)
//                    || !flags.get(FactionMemberType.LEADER).containsKey(FactionFlagType.DESTROY))
//            {
//                return false;
//            }
//        }
//
//        if (!flags.containsKey(FactionMemberType.OFFICER))
//        {
//            return false;
//        }
//        else
//        {
//            if (!flags.get(FactionMemberType.OFFICER).containsKey(FactionFlagType.USE)
//                    || !flags.get(FactionMemberType.OFFICER).containsKey(FactionFlagType.PLACE)
//                    || !flags.get(FactionMemberType.OFFICER).containsKey(FactionFlagType.DESTROY))
//            {
//                return false;
//            }
//        }
//
//        if (!flags.containsKey(FactionMemberType.MEMBER))
//        {
//            return false;
//        }
//        else
//        {
//            if (!flags.get(FactionMemberType.MEMBER).containsKey(FactionFlagType.USE)
//                    || !flags.get(FactionMemberType.MEMBER).containsKey(FactionFlagType.PLACE)
//                    || !flags.get(FactionMemberType.MEMBER).containsKey(FactionFlagType.DESTROY))
//            {
//                return false;
//            }
//        }
//
//        if (!flags.containsKey(FactionMemberType.ALLY))
//        {
//            return false;
//        }
//        else
//        {
//            if (!flags.get(FactionMemberType.ALLY).containsKey(FactionFlagType.USE)
//                    || !flags.get(FactionMemberType.ALLY).containsKey(FactionFlagType.PLACE)
//                    || !flags.get(FactionMemberType.ALLY).containsKey(FactionFlagType.DESTROY))
//            {
//                return false;
//            }
//        }
//
//        return true;
//    }

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

//    private Function<Object, Map<FactionMemberType, Map<FactionFlagType, Boolean>>> flagsTransformer = input ->
//    {
//        if (input != null && ((Map<Object, Object>)input).size() >= 4)
//        {
//            Map<FactionMemberType, Map<FactionFlagType, Boolean>> resultMap = new LinkedHashMap<>();
//
//            Map<Object, Object> memberMap = (Map<Object, Object>) input;
//
//            for (Map.Entry<Object, Object> memberEntry : memberMap.entrySet())
//            {
//                Map<Object, Object> flagMap = (Map<Object, Object>)memberEntry.getValue();
//                Map<FactionFlagType, Boolean> helpMap = new HashMap<>();
//
//                for (Map.Entry<Object, Object> flagEntry : flagMap.entrySet())
//                {
//                    helpMap.put(FactionFlagType.valueOf(flagEntry.getKey().toString().toUpperCase()), (Boolean)flagEntry.getValue());
//                }
//
//                resultMap.put(FactionMemberType.valueOf(memberEntry.getKey().toString().toUpperCase()), helpMap);
//            }
//
//            return resultMap;
//        }
//
//        return null;
//    };
//
//    private Map<FactionMemberType, Map<FactionFlagType, Boolean>> sortFlags(Map<FactionMemberType, Map<FactionFlagType, Boolean>> flagsToSort)
//    {
//        Map<FactionMemberType, Map<FactionFlagType, Boolean>> sortedFlags = new LinkedHashMap<>();
//
//        Map<FactionFlagType, Boolean> leaderMap = new LinkedHashMap<>();
//        Map<FactionFlagType, Boolean> officersMap = new LinkedHashMap<>();
//        Map<FactionFlagType, Boolean> membersMap = new LinkedHashMap<>();
//        Map<FactionFlagType, Boolean> allyMap = new LinkedHashMap<>();
//
//        leaderMap.put(FactionFlagType.USE, flagsToSort.get(FactionMemberType.LEADER).get(FactionFlagType.USE));
//        leaderMap.put(FactionFlagType.PLACE, flagsToSort.get(FactionMemberType.LEADER).get(FactionFlagType.PLACE));
//        leaderMap.put(FactionFlagType.DESTROY, flagsToSort.get(FactionMemberType.LEADER).get(FactionFlagType.DESTROY));
//
//        officersMap.put(FactionFlagType.USE, flagsToSort.get(FactionMemberType.OFFICER).get(FactionFlagType.USE));
//        officersMap.put(FactionFlagType.PLACE, flagsToSort.get(FactionMemberType.OFFICER).get(FactionFlagType.PLACE));
//        officersMap.put(FactionFlagType.DESTROY, flagsToSort.get(FactionMemberType.OFFICER).get(FactionFlagType.DESTROY));
//
//        membersMap.put(FactionFlagType.USE, flagsToSort.get(FactionMemberType.MEMBER).get(FactionFlagType.USE));
//        membersMap.put(FactionFlagType.PLACE, flagsToSort.get(FactionMemberType.MEMBER).get(FactionFlagType.PLACE));
//        membersMap.put(FactionFlagType.DESTROY, flagsToSort.get(FactionMemberType.MEMBER).get(FactionFlagType.DESTROY));
//
//        allyMap.put(FactionFlagType.USE, flagsToSort.get(FactionMemberType.ALLY).get(FactionFlagType.USE));
//        allyMap.put(FactionFlagType.PLACE, flagsToSort.get(FactionMemberType.ALLY).get(FactionFlagType.PLACE));
//        allyMap.put(FactionFlagType.DESTROY, flagsToSort.get(FactionMemberType.ALLY).get(FactionFlagType.DESTROY));
//
//        sortedFlags.put(FactionMemberType.LEADER, leaderMap);
//        sortedFlags.put(FactionMemberType.OFFICER, officersMap);
//        sortedFlags.put(FactionMemberType.MEMBER, membersMap);
//        sortedFlags.put(FactionMemberType.ALLY, allyMap);
//
//        return sortedFlags;
//    }
}
