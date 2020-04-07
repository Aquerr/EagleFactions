package io.github.aquerr.eaglefactions.common.storage.file.hocon;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.common.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.storage.FactionStorage;
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

public class HOCONFactionStorage implements FactionStorage
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
                preCreate();
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

    private void preCreate()
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
        final boolean didSucceed = ConfigurateHelper.putFactionInNode(configNode.getNode("factions"), faction);

        if (didSucceed)
            return saveChanges();
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
    public void deleteFactions()
    {
        this.configNode.getNode("factions").setValue(null);
        saveChanges();
    }

    @Override
    public @Nullable
    Faction getFaction(String factionName)
    {
            if(configNode.getNode("factions", factionName).getValue() == null)
                return null;

            return getFactionFromFile(factionName);
    }

    private Faction getFactionFromFile(String factionName)
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
        final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = ConfigurateHelper.getFactionPermsFromNode(configNode.getNode("factions", factionName, "perms"));
        final FactionChest chest = getFactionChest(factionName);
        final boolean isPublic = getFactionIsPublic(factionName);

        final Faction faction = FactionImpl.builder(factionName, tag, leader)
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
                .setPerms(perms)
                .setChest(chest)
                .setIsPublic(isPublic)
                .build();

        if(needToSave)
        {
            saveChanges();
        }

        return faction;
    }

    private FactionChest getFactionChest(String factionName)
    {
        List<FactionChest.SlotItem> slotItems = null;
        try
        {
            slotItems = configNode.getNode("factions", factionName, "chest").getValue(new TypeToken<List<FactionChest.SlotItem>>() {});
        }
        catch (ObjectMappingException e)
        {
            e.printStackTrace();
            return new FactionChestImpl(factionName);
        }
        if(slotItems != null)
        {
            return new FactionChestImpl(factionName, slotItems);
        }
        else
        {
            configNode.getNode("factions", factionName, "chest").setValue(new ArrayList<FactionChest.SlotItem>());
            needToSave = true;
            return new FactionChestImpl(factionName);
        }
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

    private boolean getFactionIsPublic(String factionName)
    {
        final Object isPublicObject = configNode.getNode("factions", factionName, "isPublic").getValue();
        if(isPublicObject != null)
            return (boolean)isPublicObject;
        else
        {
            configNode.getNode("factions", factionName, "isPublic").setValue(false);
            needToSave = true;
            return false;
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
                Faction faction = getFactionFromFile(String.valueOf(object));
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
//            Inventory inventory = Inventory.builder().of(InventoryArchetypes.CHEST).build(EagleFactionsPlugin.getPlugin());
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
