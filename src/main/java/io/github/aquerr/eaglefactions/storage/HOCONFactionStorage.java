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
import java.util.stream.Collectors;

public class HOCONFactionStorage implements IStorage
{
    private Path filePath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    private boolean needToSave = false;

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
            configNode.getNode(new Object[]{"factions", faction.Name, "recruits"}).setValue(faction.Recruits);
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
        List<String> recruits = getFactionRecruits(factionName);
        List<String> alliances = getFactionAlliances(factionName);
        List<String> enemies = getFactionEnemies(factionName);
        List<String> claims = getFactionClaims(factionName);
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = getFactionFlags(factionName);

        Faction faction = new Faction(factionName, tag, leader, recruits, members, claims, officers, alliances, enemies, home, flags);

        if (needToSave) saveChanges();

        return faction;
    }

    private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFactionFlags(String factionName)
    {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flagMap = new LinkedHashMap<>();

        //Use TreeMap instead of LinkedHashMap to sort the map if needed.

        //TODO: Add map for recruit rank.

        Map<FactionFlagTypes, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> recruitMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> allyMap = new LinkedHashMap<>();

        //Get leader flags
        boolean leaderUSE = configNode.getNode("factions", factionName, "flags", "LEADER", "USE").getBoolean(true);
        boolean leaderPLACE = configNode.getNode("factions", factionName, "flags", "LEADER", "PLACE").getBoolean(true);
        boolean leaderDESTROY = configNode.getNode("factions", factionName, "flags", "LEADER", "DESTROY").getBoolean(true);
        boolean leaderCLAIM = configNode.getNode("factions", factionName, "flags", "LEADER", "CLAIM").getBoolean(true);
        boolean leaderATTACK = configNode.getNode("factions", factionName, "flags", "LEADER", "ATTACK").getBoolean(true);
        boolean leaderINVITE = configNode.getNode("factions", factionName, "flags", "LEADER", "INVITE").getBoolean(true);

        //Get officer flags
        boolean officerUSE = configNode.getNode("factions", factionName, "flags", "OFFICER", "USE").getBoolean(true);
        boolean officerPLACE = configNode.getNode("factions", factionName, "flags", "OFFICER", "PLACE").getBoolean(true);
        boolean officerDESTROY = configNode.getNode("factions", factionName, "flags", "OFFICER", "DESTROY").getBoolean(true);
        boolean officerCLAIM = configNode.getNode("factions", factionName, "flags", "OFFICER", "CLAIM").getBoolean(true);
        boolean officerATTACK = configNode.getNode("factions", factionName, "flags", "LEADER", "ATTACK").getBoolean(true);
        boolean officerINVITE = configNode.getNode("factions", factionName, "flags", "OFFICER", "INVITE").getBoolean(true);

        //Get member flags
        boolean memberUSE = configNode.getNode("factions", factionName, "flags", "MEMBER", "USE").getBoolean(true);
        boolean memberPLACE = configNode.getNode("factions", factionName, "flags", "MEMBER", "PLACE").getBoolean(true);
        boolean memberDESTROY = configNode.getNode("factions", factionName, "flags", "MEMBER", "DESTROY").getBoolean(true);
        boolean memberCLAIM = configNode.getNode("factions", factionName, "flags", "MEMBER", "CLAIM").getBoolean(false);
        boolean memberATTACK = configNode.getNode("factions", factionName, "flags", "LEADER", "ATTACK").getBoolean(false);
        boolean memberINVITE = configNode.getNode("factions", factionName, "flags", "MEMBER", "INVITE").getBoolean(true);

        //Get recruit flags
        boolean recruitUSE = configNode.getNode("factions", factionName, "flags", "RECRUIT", "USE").getBoolean(true);
        boolean recruitPLACE = configNode.getNode("factions", factionName, "flags", "RECRUIT", "PLACE").getBoolean(true);
        boolean recruitDESTROY = configNode.getNode("factions", factionName, "flags", "RECRUIT", "DESTROY").getBoolean(true);
        boolean recruitCLAIM = configNode.getNode("factions", factionName, "flags", "RECRUIT", "CLAIM").getBoolean(false);
        boolean recruitATTACK = configNode.getNode("factions", factionName, "flags", "RECRUIT", "ATTACK").getBoolean(false);
        boolean recruitINVITE = configNode.getNode("factions", factionName, "flags", "RECRUIT", "INVITE").getBoolean(false);

        //Get ally flags
        boolean allyUSE = configNode.getNode("factions", factionName, "flags", "ALLY", "USE").getBoolean(true);
        boolean allyPLACE = configNode.getNode("factions", factionName, "flags", "ALLY", "PLACE").getBoolean(false);
        boolean allyDESTROY = configNode.getNode("factions", factionName, "flags", "ALLY", "DESTROY").getBoolean(false);

        leaderMap.put(FactionFlagTypes.USE, leaderUSE);
        leaderMap.put(FactionFlagTypes.PLACE, leaderPLACE);
        leaderMap.put(FactionFlagTypes.DESTROY, leaderDESTROY);
        leaderMap.put(FactionFlagTypes.CLAIM, leaderCLAIM);
        leaderMap.put(FactionFlagTypes.ATTACK, leaderATTACK);
        leaderMap.put(FactionFlagTypes.INVITE, leaderINVITE);

        officerMap.put(FactionFlagTypes.USE, officerUSE);
        officerMap.put(FactionFlagTypes.PLACE, officerPLACE);
        officerMap.put(FactionFlagTypes.DESTROY, officerDESTROY);
        officerMap.put(FactionFlagTypes.CLAIM, officerCLAIM);
        officerMap.put(FactionFlagTypes.ATTACK, officerATTACK);
        officerMap.put(FactionFlagTypes.INVITE, officerINVITE);

        membersMap.put(FactionFlagTypes.USE, memberUSE);
        membersMap.put(FactionFlagTypes.PLACE, memberPLACE);
        membersMap.put(FactionFlagTypes.DESTROY, memberDESTROY);
        membersMap.put(FactionFlagTypes.CLAIM, memberCLAIM);
        membersMap.put(FactionFlagTypes.ATTACK, memberATTACK);
        membersMap.put(FactionFlagTypes.INVITE, memberINVITE);

        recruitMap.put(FactionFlagTypes.USE, recruitUSE);
        recruitMap.put(FactionFlagTypes.PLACE, recruitPLACE);
        recruitMap.put(FactionFlagTypes.DESTROY, recruitDESTROY);
        recruitMap.put(FactionFlagTypes.CLAIM, recruitCLAIM);
        recruitMap.put(FactionFlagTypes.ATTACK, recruitATTACK);
        recruitMap.put(FactionFlagTypes.INVITE, recruitINVITE);

        allyMap.put(FactionFlagTypes.USE, allyUSE);
        allyMap.put(FactionFlagTypes.PLACE, allyPLACE);
        allyMap.put(FactionFlagTypes.DESTROY, allyDESTROY);

        flagMap.put(FactionMemberType.LEADER, leaderMap);
        flagMap.put(FactionMemberType.OFFICER, officerMap);
        flagMap.put(FactionMemberType.MEMBER, membersMap);
        flagMap.put(FactionMemberType.RECRUIT, recruitMap);
        flagMap.put(FactionMemberType.ALLY, allyMap);

        return flagMap;
    }

    private List<String> getFactionClaims(String factionName)
    {
        Object claimsObject = configNode.getNode(new Object[]{"factions", factionName, "claims"}).getValue();

        if (claimsObject != null)
        {
            return (List<String>) claimsObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "claims"}).setValue(new ArrayList<>());
            needToSave = true;
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
            needToSave = true;
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
            needToSave = true;
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
            needToSave = true;
            return new ArrayList<>();
        }
    }

    private List<String> getFactionRecruits(String factionName)
    {
        Object recruitsObject = configNode.getNode(new Object[]{"factions", factionName, "recruits"}).getValue();

        if (recruitsObject != null)
        {
            return (List<String>) recruitsObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "recruits"}).setValue(new ArrayList<>());
            needToSave = true;
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
            needToSave = true;
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
            needToSave = true;
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
            needToSave = true;
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
            needToSave = true;
            return Text.of("");
        }
    }

    @Override
    public List<Faction> getFactions()
    {
        //TODO: Optimize this code as much as possible.

        List<String> factionList = FactionsCache.getFactionsList().stream().map(x->x.Name).collect(Collectors.toList());

        final Set<Object> keySet = getStorage().getNode("factions").getChildrenMap().keySet();

        for (Object object : keySet)
        {
            if (object instanceof String)
            {
                if (!factionList.contains(object))
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
