package io.github.aquerr.eaglefactions.storage;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagTypes;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
            } else
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
            configNode.getNode(new Object[]{"factions", faction.Name, "tag"}).setValue(TypeToken.of(Text.class), faction.Tag);
            configNode.getNode(new Object[]{"factions", faction.Name, "leader"}).setValue(faction.Leader);
            configNode.getNode(new Object[]{"factions", faction.Name, "officers"}).setValue(faction.Officers);
            configNode.getNode(new Object[]{"factions", faction.Name, "members"}).setValue(faction.Members);
            configNode.getNode(new Object[]{"factions", faction.Name, "enemies"}).setValue(faction.Enemies);
            configNode.getNode(new Object[]{"factions", faction.Name, "alliances"}).setValue(faction.Alliances);
            configNode.getNode(new Object[]{"factions", faction.Name, "claims"}).setValue(faction.Claims);
            configNode.getNode(new Object[]{"factions", faction.Name, "flags"}).setValue(faction.Flags);

            if (faction.Home == null)
            {
                configNode.getNode(new Object[]{"factions", faction.Name, "home"}).setValue(faction.Home);
            }
            else
            {
                configNode.getNode(new Object[]{"factions", faction.Name, "home"}).setValue(faction.Home.WorldUUID.toString() + '|' + faction.Home.BlockPosition.toString());
            }

            FactionsCache.addOrUpdateFactionCache(faction);

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
            FactionsCache.removeFactionCache(factionName);
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
            Faction factionCache = FactionsCache.getFactionCache(factionName);
            if (factionCache != null) return factionCache;

            if (configNode.getNode("factions", factionName).getValue() == null)
            {
                return null;
            }

            Faction faction = createFactionObject(factionName);

            FactionsCache.addOrUpdateFactionCache(faction);

            return faction;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        //If it was not possible to get a faction then return null.
        return null;
    }

    private Faction createFactionObject(String factionName)
    {
        Text tag = getFactionTag(factionName);
        String leader = getFactionLeader(factionName);
        FactionHome home = getFactionHome(factionName);
        List<String> officers = getFactionOfficers(factionName);
        List<String> members = getFactionMembers(factionName);
        List<String> alliances = getFactionAlliances(factionName);
        List<String> enemies = getFactionEnemies(factionName);
        List<String> claims = getFactionClaims(factionName);
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = getFactionFlags(factionName);

        Faction faction = new Faction(factionName, tag, leader, members, claims, officers, alliances, enemies, home, flags);

        //TODO: Refactor this code so that the power can be sent to the faction constructor like other parameters.
        faction.Power = PowerManager.getFactionPower(faction); //Get power from all players in faction.

        return faction;
    }

    private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFactionFlags(String factionName)
    {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flagMap = new LinkedHashMap<>();

        //TODO: Get the entire map from file if it is possible and handle CLAIM and INVITE map here.
        //TODO: Use TreeMap instead of LinkedHashMap to sort the map.

        Map<FactionFlagTypes, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> allyMap = new LinkedHashMap<>();

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

        leaderMap.put(FactionFlagTypes.USE, (boolean) leaderUSE);
        leaderMap.put(FactionFlagTypes.PLACE, (boolean) leaderPLACE);
        leaderMap.put(FactionFlagTypes.DESTROY, (boolean) leaderDESTROY);

        officerMap.put(FactionFlagTypes.USE, (boolean) officerUSE);
        officerMap.put(FactionFlagTypes.PLACE, (boolean) officerPLACE);
        officerMap.put(FactionFlagTypes.DESTROY, (boolean) officerDESTROY);

        membersMap.put(FactionFlagTypes.USE, (boolean) memberUSE);
        membersMap.put(FactionFlagTypes.PLACE, (boolean) memberPLACE);
        membersMap.put(FactionFlagTypes.DESTROY, (boolean) memberDESTROY);

        allyMap.put(FactionFlagTypes.USE, (boolean) allyUSE);
        allyMap.put(FactionFlagTypes.PLACE, (boolean) allyPLACE);
        allyMap.put(FactionFlagTypes.DESTROY, (boolean) allyDESTROY);

        flagMap.put(FactionMemberType.LEADER, leaderMap);
        flagMap.put(FactionMemberType.OFFICER, officerMap);
        flagMap.put(FactionMemberType.MEMBER, membersMap);
        flagMap.put(FactionMemberType.ALLY, allyMap);

        return flagMap;
    }

    private List<String> getFactionClaims(String factionName)
    {
        Object claimsObject = configNode.getNode(new Object[]{"factions", factionName, "claims"}).getValue();

        if (claimsObject != null)
        {
            return (List<String>) claimsObject;
        } else
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
            return (List<String>) enemiesObject;
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
            return (List<String>) alliancesObject;
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
            return (List<String>) membersObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "members"}).setValue(new ArrayList<>());
            saveChanges();
            return new ArrayList<>();
        }
    }

    private FactionHome getFactionHome(String factionName)
    {
        Object homeObject = configNode.getNode(new Object[]{"factions", factionName, "home"}).getValue();

        if (homeObject != null)
        {
            if (String.valueOf(homeObject).equals(""))
            {
                return null;
            }
            else return new FactionHome(String.valueOf(homeObject));

        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "home"}).setValue("");
            saveChanges();
            return null;
        }
    }

    private List<String> getFactionOfficers(String factionName)
    {
        Object officersObject = configNode.getNode(new Object[]{"factions", factionName, "officers"}).getValue();

        if (officersObject != null)
        {
            return (List<String>) officersObject;
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
        Object leaderObject = configNode.getNode(new Object[]{"factions", factionName, "leader"}).getValue();

        if (leaderObject != null)
        {
            return String.valueOf(leaderObject);
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "leader"}).setValue("");
            saveChanges();
            return "";
        }
    }

    private Text getFactionTag(String factionName)
    {
        Object tagObject = null;
        try
        {
            tagObject = configNode.getNode(new Object[]{"factions", factionName, "tag"}).getValue(TypeToken.of(Text.class));
        }
        catch (ObjectMappingException e)
        {
            e.printStackTrace();
        }

        if (tagObject != null)
        {
            return (Text)tagObject;
        }
        else
        {
            try
            {
                configNode.getNode(new Object[]{"factions", factionName, "tag"}).setValue(TypeToken.of(Text.class), Text.of(""));
            }
            catch (ObjectMappingException e)
            {
                e.printStackTrace();
            }
            saveChanges();
            return Text.of("");
        }
    }

    @Override
    public List<Faction> getFactions()
    {
        List<Faction> factionList = FactionsCache.getFactionsList();

        final Set<Object> keySet = getStorage().getNode("factions").getChildrenMap().keySet();

        for (Object object : keySet)
        {
            if (object instanceof String)
            {
                if (factionList.stream().noneMatch(x -> x.Name.equals(String.valueOf(object))))
                {
                    Faction faction = createFactionObject(String.valueOf(object));
                    FactionsCache.addOrUpdateFactionCache(faction);
                }
            }
        }

        return FactionsCache.getFactionsList();
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
