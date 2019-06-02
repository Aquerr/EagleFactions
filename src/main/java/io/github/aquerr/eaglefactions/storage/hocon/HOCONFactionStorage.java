package io.github.aquerr.eaglefactions.storage.hocon;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.storage.IFactionStorage;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HOCONFactionStorage implements IFactionStorage
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

            if(!Files.exists(dataPath))
            {
                Files.createDirectory(dataPath);
            }

            filePath = dataPath.resolve("factions.conf");

            if(!Files.exists(filePath))
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
        catch(IOException exception)
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

        getStorage().getNode("factions", "SafeZone", "claims").setValue(new ArrayList<>());
        getStorage().getNode("factions", "SafeZone", "members").setValue(new ArrayList<>());

        saveChanges();
    }

    public boolean saveFaction(Faction faction)
    {
        try
        {
//            List<DataView> serializedInventorySlots = InventorySerializer.serializeInventory(faction.getChest().toInventory());
//            List<String> serializedHoconChest = new ArrayList<>();
//            for(DataView dataView : serializedInventorySlots)
//            {
//                serializedHoconChest.add(DataFormats.HOCON.write(dataView));
//            }
            configNode.getNode("factions", faction.getName(), "tag").setValue(TypeToken.of(Text.class), faction.getTag());
            configNode.getNode("factions", faction.getName(), "leader").setValue(faction.getLeader().toString());
            configNode.getNode("factions", faction.getName(), "description").setValue(faction.getDescription());
            configNode.getNode("factions", faction.getName(), "motd").setValue(faction.getMessageOfTheDay());
            configNode.getNode("factions", faction.getName(), "officers").setValue(new TypeToken<Set<UUID>>(){}, faction.getOfficers());
            configNode.getNode("factions", faction.getName(), "members").setValue(new TypeToken<Set<UUID>>(){}, faction.getMembers());
            configNode.getNode("factions", faction.getName(), "recruits").setValue(new TypeToken<Set<UUID>>(){}, faction.getRecruits());
            configNode.getNode("factions", faction.getName(), "enemies").setValue(faction.getEnemies());
            configNode.getNode("factions", faction.getName(), "alliances").setValue(faction.getAlliances());
            configNode.getNode("factions", faction.getName(), "claims").setValue(faction.getClaims().stream().map(x->x.toString()).collect(Collectors.toList()));
            configNode.getNode("factions", faction.getName(), "last_online").setValue(faction.getLastOnline().toString());
            configNode.getNode("factions", faction.getName(), "flags").setValue(faction.getFlags());
            configNode.getNode("factions", faction.getName(), "chest").setValue(new TypeToken<List<FactionChest.SlotItem>>(){}, faction.getChest().getItems());
//            configNode.getNode("factions", faction.getName(), "chest").setValue(serializedHoconChest);

            if(faction.getHome() == null)
            {
                configNode.getNode("factions", faction.getName(), "home").setValue(faction.getHome());
            }
            else
            {
                configNode.getNode("factions", faction.getName(), "home").setValue(faction.getHome().getWorldUUID().toString() + '|' + faction.getHome().getBlockPosition().toString());
            }
            return saveChanges();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean addOrUpdateFaction(final Faction faction)
    {
        return saveFaction(faction);
    }

    @Override
    public boolean deleteFaction(String factionName)
    {
        try
        {
            configNode.getNode("factions").removeChild(factionName);
            return saveChanges();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        return saveChanges();
    }

    @Override
    public @Nullable Faction getFaction(String factionName)
    {
            if(configNode.getNode("factions", factionName).getValue() == null)
                return null;

            return createFactionObject(factionName);
    }

    private Faction createFactionObject(String factionName)
    {
        final Text tag = getFactionTag(factionName);
        final String description = getFactionDescription(factionName);
        final String messageOfTheDay = getFactionMessageOfTheDay(factionName);
        final UUID leader = getFactionLeader(factionName);
        final FactionHome home = getFactionHome(factionName);
        final Set<UUID> officers = getFactionOfficers(factionName);
        final Set<UUID> members = getFactionMembers(factionName);
        final Set<UUID> recruits = getFactionRecruits(factionName);
        final Set<String> alliances = getFactionAlliances(factionName);
        final Set<String> enemies = getFactionEnemies(factionName);
        final Set<Claim> claims = getFactionClaims(factionName);
        final Instant lastOnline = getLastOnline(factionName);
        final Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = getFactionFlags(factionName);
        final FactionChest chest = getFactionChest(factionName);

        final Faction faction = Faction.builder(factionName, tag, leader)
                .setDescription(description)
                .setMessageOfTheDay(messageOfTheDay)
                .setHome(home)
                .setOfficers(officers)
                .setMembers(members)
                .setRecruits(recruits)
                .setAlliances(alliances)
                .setEnemies(enemies)
                .setClaims(claims)
                .setLastOnline(lastOnline)
                .setFlags(flags)
                .setChest(chest)
                .build();

        if(needToSave)
        {
            saveChanges();
        }

        return faction;
    }

    private FactionChest getFactionChest(String factionName)
    {
//        FactionChest factionChest = null;
        List<FactionChest.SlotItem> slotItems = null;
        try
        {

//            factionChest = configNode.getNode("factions", factionName, "chest").getValue(objectToFactionChestTransformer);
            slotItems = configNode.getNode("factions", factionName, "chest").getValue(new TypeToken<List<FactionChest.SlotItem>>() {});
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return new FactionChest(factionName);
        }

//        if(factionChest != null)
//        {
//            return factionChest;
//        }
        if(slotItems != null)
        {
            return new FactionChest(factionName, slotItems);
        }
        else
        {
            configNode.getNode("factions", factionName, "chest").setValue(new ArrayList<FactionChest.SlotItem>());
            needToSave = true;
            return new FactionChest(factionName);
        }
    }

    private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFactionFlags(String factionName)
    {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flagMap = new LinkedHashMap<>();

    //    Object flagnode = configNode.getNode("factions", factionName, "flags");

//        //TODO: Test this code.
//        if(flagnode instanceof Map)
//        {
//            Map<String, Object> flags = new HashMap<>();
//
//            for(Map.Entry<String, Object> flagEntry : flags.entrySet())
//            {
//                Map<FactionFlagTypes, Boolean> memberTypeMap = new LinkedHashMap<>();
//                if(flagEntry.getValue() instanceof Map)
//                {
//                    Map<String, Boolean> map = (Map<String, Boolean>)flagEntry.getValue();
//                    for(Map.Entry<String, Boolean> testEntry : map.entrySet())
//                    {
//                        map.put(FactionFlagTypes.valueOf(testEntry.getKey()), testEntry.getValue());
//                    }
//                }
//                flagMap.put(FactionMemberType.valueOf(flagEntry.getKey()), memberTypeMap);
//            }
//        }

        //Use TreeMap instead of LinkedHashMap to sort the map if needed.

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

    private Instant getLastOnline(String factionName)
    {
        Object lastOnline = configNode.getNode("factions", factionName, "last_online").getValue();

        if(lastOnline != null)
        {
            return Instant.parse(lastOnline.toString());
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "last_online"}).setValue(Instant.now().toString());
            needToSave = true;
            return Instant.now();
        }
    }

    private Set<Claim> getFactionClaims(String factionName)
    {
        Object claimsObject = configNode.getNode(new Object[]{"factions", factionName, "claims"}).getValue();

        if(claimsObject != null)
        {
            Set<Claim> claims = new HashSet<>();
            for (String claimAsString : (List<String>)claimsObject)
            {
                Claim claim = Claim.valueOf(claimAsString);
                claims.add(claim);
            }
            return claims;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "claims"}).setValue(new HashSet<>());
            needToSave = true;
            return new HashSet<>();
        }
    }

    private Set<String> getFactionEnemies(String factionName)
    {
        Object enemiesObject = configNode.getNode(new Object[]{"factions", factionName, "enemies"}).getValue();

        if(enemiesObject != null)
        {
            return new HashSet<>((List<String>) enemiesObject);
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "enemies"}).setValue(new HashSet<>());
            needToSave = true;
            return new HashSet<>();
        }
    }

    private Set<String> getFactionAlliances(String factionName)
    {
        Object alliancesObject = configNode.getNode(new Object[]{"factions", factionName, "alliances"}).getValue();

        if(alliancesObject != null)
        {
            return new HashSet<>((List<String>) alliancesObject);
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "alliances"}).setValue(new HashSet<>());
            needToSave = true;
            return new HashSet<>();
        }
    }

    private Set<UUID> getFactionMembers(String factionName)
    {
        Set<UUID> membersObject = configNode.getNode(new Object[]{"factions", factionName, "members"}).getValue(objectToUUIDListTransformer);

        if(membersObject != null)
        {
            return membersObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "members"}).setValue(new HashSet<>());
            needToSave = true;
            return new HashSet<>();
        }
    }

    private Set<UUID> getFactionRecruits(String factionName)
    {
        Set<UUID> recruitsObject = configNode.getNode(new Object[]{"factions", factionName, "recruits"}).getValue(objectToUUIDListTransformer);

        if(recruitsObject != null)
        {
            return recruitsObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "recruits"}).setValue(new HashSet<>());
            needToSave = true;
            return new HashSet<>();
        }
    }

    private FactionHome getFactionHome(String factionName)
    {
        Object homeObject = configNode.getNode(new Object[]{"factions", factionName, "home"}).getValue();

        if(homeObject != null)
        {
            if(String.valueOf(homeObject).equals(""))
            {
                return null;
            }
            else
            {
                return FactionHome.from(String.valueOf(homeObject));
            }

        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "home"}).setValue("");
            needToSave = true;
            return null;
        }
    }

    private Set<UUID> getFactionOfficers(String factionName)
    {
        Set<UUID> officersObject = configNode.getNode(new Object[]{"factions", factionName, "officers"}).getValue(objectToUUIDListTransformer);

        if(officersObject != null)
        {
            return officersObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "officers"}).setValue(new HashSet<>());
            needToSave = true;
            return new HashSet<>();
        }
    }

    @Nullable
    private UUID getFactionLeader(String factionName)
    {
        Object leaderObject = configNode.getNode(new Object[]{"factions", factionName, "leader"}).getValue();

        if(leaderObject != null && !leaderObject.equals(""))
        {
            return UUID.fromString(String.valueOf(leaderObject));
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "leader"}).setValue("");
            needToSave = true;
            return new UUID(0,0);
        }
    }

    private Text getFactionTag(String factionName)
    {
        Object tagObject = null;
        try
        {
            tagObject = configNode.getNode(new Object[]{"factions", factionName, "tag"}).getValue(TypeToken.of(Text.class));
        }
        catch(ObjectMappingException e)
        {
            e.printStackTrace();
        }

        if(tagObject != null)
        {
            return (Text) tagObject;
        }
        else
        {
            try
            {
                configNode.getNode(new Object[]{"factions", factionName, "tag"}).setValue(TypeToken.of(Text.class), Text.of(""));
            }
            catch(ObjectMappingException e)
            {
                e.printStackTrace();
            }
            needToSave = true;
            return Text.of("");
        }
    }

    private String getFactionDescription(final String factionName)
    {
        final Object leaderObject = configNode.getNode(new Object[]{"factions", factionName, "description"}).getValue();

        if(leaderObject != null)
        {
            return (String)leaderObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "description"}).setValue("");
            needToSave = true;
            return "";
        }
    }

    private String getFactionMessageOfTheDay(final String factionName)
    {
        final Object leaderObject = configNode.getNode(new Object[]{"factions", factionName, "motd"}).getValue();

        if(leaderObject != null)
        {
            return (String)leaderObject;
        }
        else
        {
            configNode.getNode(new Object[]{"factions", factionName, "motd"}).setValue("");
            needToSave = true;
            return "";
        }
    }

    @Override
    public Set<Faction> getFactions()
    {
        final Set<Faction> factions = new HashSet<>();
        final Set<Object> keySet = getStorage().getNode("factions").getChildrenMap().keySet();

        for(Object object : keySet)
        {
            if(object instanceof String)
            {
                Faction faction = createFactionObject(String.valueOf(object));
                factions.add(faction);
            }
        }

        return factions;
    }

    @Override
    public void load()
    {
        try
        {
            configNode = configLoader.load();
        }
        catch(IOException e)
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
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private CommentedConfigurationNode getStorage()
    {
        return configNode;
    }

    private Function<Object, Set<UUID>> objectToUUIDListTransformer = (Function<Object, Set<UUID>>) object ->
    {
        if(object instanceof List)
        {
            Set<UUID> uuidSet = new HashSet<>();
            List<String> list = (List<String>)object;

            for(String stringUUID : list)
            {
                String[] components = stringUUID.split("-");
                if(components.length == 5)
                {
                    uuidSet.add(UUID.fromString(stringUUID));
                }
            }

            return uuidSet;
        }
        return null;
    };

//    private Function<Object, FactionChest> objectToFactionChestTransformer = object ->
//    {
//        if(object instanceof List)
//        {
//            List<DataView> dataViewList = new ArrayList<>();
//            List<Object> objectList = (List<Object>)object;
//
//            for(Object dataViewObject : objectList)
//            {
//                try
//                {
//                    DataView dataView = DataFormats.HOCON.read(dataViewObject.toString());
//                    dataViewList.add(dataView);
//                }
//                catch(IOException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//
//            Inventory inventory = Inventory.builder().of(InventoryArchetypes.CHEST).build(EagleFactions.getPlugin());
//            InventorySerializer.deserializeInventory(dataViewList, inventory);
//            return FactionChest.fromInventory(,inventory);
//
////            return dataViewList;
//        }
//        return null;
//    };


//    private List<String> toListOfStrings(Collection<UUID> listOfUUIDs)
//    {
//        List<String> listOfStrings = new ArrayList<>();
//        for(UUID uuid : listOfUUIDs)
//        {
//            listOfStrings.add(uuid.toString());
//        }
//        return listOfStrings;
//    }
}
