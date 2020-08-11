package io.github.aquerr.eaglefactions.common.storage.file.hocon;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.common.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.common.storage.serializers.EFTypeSerializers;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigurateHelper
{
    public static boolean putFactionInNode(final ConfigurationNode configNode, final Faction faction)
    {
        try
        {
            configNode.getNode("name").setValue(faction.getName());
            configNode.getNode("tag").setValue(TypeTokens.TEXT_TOKEN, faction.getTag());
            configNode.getNode("leader").setValue(faction.getLeader().toString());
            configNode.getNode("description").setValue(faction.getDescription());
            configNode.getNode("motd").setValue(faction.getMessageOfTheDay());
            configNode.getNode("officers").setValue(new TypeToken<ArrayList<UUID>>() {}, new ArrayList<>(faction.getOfficers()));
            configNode.getNode("members").setValue(new TypeToken<ArrayList<UUID>>() {}, new ArrayList<>(faction.getMembers()));
            configNode.getNode("recruits").setValue(new TypeToken<ArrayList<UUID>>() {}, new ArrayList<>(faction.getRecruits()));
            configNode.getNode("truces").setValue(faction.getTruces());
            configNode.getNode("alliances").setValue(faction.getAlliances());
            configNode.getNode("enemies").setValue(faction.getEnemies());
            configNode.getNode("claims").setValue(EFTypeSerializers.CLAIM_SET_TYPE_TOKEN, faction.getClaims());
            configNode.getNode("last_online").setValue(faction.getLastOnline().toString());
            configNode.getNode("perms").setValue(faction.getPerms());
            configNode.getNode("chest").setValue(new TypeToken<List<FactionChest.SlotItem>>(){}, faction.getChest().getItems());
            configNode.getNode("isPublic").setValue(faction.isPublic());

            if(faction.getHome() == null)
            {
                configNode.getNode("home").setValue(faction.getHome());
            }
            else
            {
                configNode.getNode("home").setValue(faction.getHome().getWorldUUID().toString() + '|' + faction.getHome().getBlockPosition().toString());
            }
            return true;
        }
        catch(final Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    public static boolean putPlayerInNode(final ConfigurationNode configNode, final FactionPlayer factionPlayer)
    {
        try
        {
            configNode.getNode("faction").setValue(factionPlayer.getFactionName().orElse(""));
            configNode.getNode("faction-member-type").setValue(factionPlayer.getFactionRole().toString());
            configNode.getNode("name").setValue(factionPlayer.getName());
            configNode.getNode("power").setValue(factionPlayer.getPower());
            configNode.getNode("maxpower").setValue(factionPlayer.getMaxPower());
            configNode.getNode("death-in-warzone").setValue(factionPlayer.diedInWarZone());
            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    public static List<Faction> getFactionsFromNode(final ConfigurationNode configNode)
    {
        final List<Faction> factions = new ArrayList<>();

        final Set<Object> keySet = configNode.getChildrenMap().keySet();
        for(final Object object : keySet)
        {
            if(object instanceof String)
            {
                try
                {
                    final Faction faction = getFactionFromNode(configNode.getNode(object));
                    factions.add(faction);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return factions;
    }

    public static Faction getFactionFromNode(final ConfigurationNode configNode) throws ObjectMappingException
    {
        String factionName;
        factionName = configNode.getNode("name").getString();

        // Backwards compatibility
        //TODO: Remove in future release
        if (configNode.getKey() != null)
            factionName = (String) configNode.getKey();

        final Text tag = configNode.getNode("tag").getValue(TypeTokens.TEXT_TOKEN);
        final String description = configNode.getNode("description").getString();
        final String messageOfTheDay = configNode.getNode("motd").getString();

        //Backwards compatibility. We need to have a proper uuid stored in leader field.
        //TODO: Remove in future release
        final Object leaderValue = configNode.getNode("leader").getValue();
        if (leaderValue == null || (leaderValue instanceof String && ((String) leaderValue).trim().equals("")))
            configNode.getNode("leader").setValue(TypeTokens.UUID_TOKEN, new UUID(0, 0));
        //

        final UUID leader = configNode.getNode("leader").getValue(TypeToken.of(UUID.class), new UUID(0,0));
        final FactionHome home = FactionHome.from(String.valueOf(configNode.getNode("home").getValue("")));
        final Set<UUID> officers = configNode.getNode("officers").getValue(EFTypeSerializers.UUID_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Set<UUID> members = configNode.getNode("members").getValue(EFTypeSerializers.UUID_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Set<UUID> recruits = configNode.getNode("recruits").getValue(EFTypeSerializers.UUID_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Set<String> alliances = new HashSet<>(configNode.getNode("alliances").getList(TypeToken.of(String.class), Collections.EMPTY_LIST));
        final Set<String> enemies = new HashSet<>(configNode.getNode("enemies").getList(TypeToken.of(String.class), Collections.EMPTY_LIST));
        final Set<Claim> claims = configNode.getNode("claims").getValue(EFTypeSerializers.CLAIM_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Instant lastOnline = configNode.getNode("last_online").getValue() != null ? Instant.parse(configNode.getNode("last_online").getString()) : Instant.now();
        final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = getFactionPermsFromNode(configNode.getNode("perms"));
        List<FactionChest.SlotItem> slotItems = configNode.getNode("chest").getValue(new TypeToken<List<FactionChest.SlotItem>>() {});

        FactionChest chest;
        if (slotItems == null)
            chest = new FactionChestImpl(factionName);
        else
            chest = new FactionChestImpl(factionName, slotItems);
        final boolean isPublic = configNode.getNode("isPublic").getBoolean(false);

        return FactionImpl.builder(factionName, tag, leader)
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
    }

    public static FactionPlayer getPlayerFromFile(final File file)
    {
        HoconConfigurationLoader playerConfigLoader = HoconConfigurationLoader.builder().setFile(file).build();
        try
        {
            ConfigurationNode playerNode = playerConfigLoader.load();
            String playerName = playerNode.getNode("name").getString("");
            UUID playerUUID;
            try
            {
                playerUUID = UUID.fromString(file.getName().substring(0, file.getName().indexOf('.')));

            }
            catch(Exception exception)
            {
                exception.printStackTrace();
                Files.delete(file.toPath());
                return null;
            }
            String factionName = playerNode.getNode("faction").getString("");
            String factionMemberTypeString = playerNode.getNode("faction-member-type").getString("");
            float power = playerNode.getNode("power").getFloat(0.0f);
            float maxpower = playerNode.getNode("maxpower").getFloat(0.0f);
            boolean diedInWarZone = playerNode.getNode("death-in-warzone").getBoolean(false);
            FactionMemberType factionMemberType = null;

            if(!factionMemberTypeString.equals(""))
                factionMemberType = FactionMemberType.valueOf(factionMemberTypeString);

            return new FactionPlayerImpl(playerName, playerUUID, factionName, power, maxpower, factionMemberType, diedInWarZone);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<FactionMemberType, Map<FactionPermType, Boolean>> getFactionPermsFromNode(final ConfigurationNode factionNode)
    {
        Map<FactionMemberType, Map<FactionPermType, Boolean>> flagMap = new LinkedHashMap<>();
        Map<FactionPermType, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionPermType, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionPermType, Boolean> recruitMap = new LinkedHashMap<>();
        Map<FactionPermType, Boolean> allyMap = new LinkedHashMap<>();

        //Get officer perms
        boolean officerUSE = factionNode.getNode("OFFICER", "USE").getBoolean(true);
        boolean officerPLACE = factionNode.getNode("OFFICER", "PLACE").getBoolean(true);
        boolean officerDESTROY = factionNode.getNode("OFFICER", "DESTROY").getBoolean(true);
        boolean officerCLAIM = factionNode.getNode("OFFICER", "CLAIM").getBoolean(true);
        boolean officerATTACK = factionNode.getNode("LEADER", "ATTACK").getBoolean(true);
        boolean officerINVITE = factionNode.getNode("OFFICER", "INVITE").getBoolean(true);

        //Get member perms
        boolean memberUSE = factionNode.getNode("MEMBER", "USE").getBoolean(true);
        boolean memberPLACE = factionNode.getNode("MEMBER", "PLACE").getBoolean(true);
        boolean memberDESTROY = factionNode.getNode("MEMBER", "DESTROY").getBoolean(true);
        boolean memberCLAIM = factionNode.getNode("MEMBER", "CLAIM").getBoolean(false);
        boolean memberATTACK = factionNode.getNode("LEADER", "ATTACK").getBoolean(false);
        boolean memberINVITE = factionNode.getNode("MEMBER", "INVITE").getBoolean(true);

        //Get recruit perms
        boolean recruitUSE = factionNode.getNode("RECRUIT", "USE").getBoolean(true);
        boolean recruitPLACE = factionNode.getNode("RECRUIT", "PLACE").getBoolean(true);
        boolean recruitDESTROY = factionNode.getNode("RECRUIT", "DESTROY").getBoolean(true);
        boolean recruitCLAIM = factionNode.getNode("RECRUIT", "CLAIM").getBoolean(false);
        boolean recruitATTACK = factionNode.getNode("RECRUIT", "ATTACK").getBoolean(false);
        boolean recruitINVITE = factionNode.getNode("RECRUIT", "INVITE").getBoolean(false);

        //Get ally perms
        boolean allyUSE = factionNode.getNode("ALLY", "USE").getBoolean(true);
        boolean allyPLACE = factionNode.getNode( "ALLY", "PLACE").getBoolean(false);
        boolean allyDESTROY = factionNode.getNode("ALLY", "DESTROY").getBoolean(false);

        officerMap.put(FactionPermType.USE, officerUSE);
        officerMap.put(FactionPermType.PLACE, officerPLACE);
        officerMap.put(FactionPermType.DESTROY, officerDESTROY);
        officerMap.put(FactionPermType.CLAIM, officerCLAIM);
        officerMap.put(FactionPermType.ATTACK, officerATTACK);
        officerMap.put(FactionPermType.INVITE, officerINVITE);

        membersMap.put(FactionPermType.USE, memberUSE);
        membersMap.put(FactionPermType.PLACE, memberPLACE);
        membersMap.put(FactionPermType.DESTROY, memberDESTROY);
        membersMap.put(FactionPermType.CLAIM, memberCLAIM);
        membersMap.put(FactionPermType.ATTACK, memberATTACK);
        membersMap.put(FactionPermType.INVITE, memberINVITE);

        recruitMap.put(FactionPermType.USE, recruitUSE);
        recruitMap.put(FactionPermType.PLACE, recruitPLACE);
        recruitMap.put(FactionPermType.DESTROY, recruitDESTROY);
        recruitMap.put(FactionPermType.CLAIM, recruitCLAIM);
        recruitMap.put(FactionPermType.ATTACK, recruitATTACK);
        recruitMap.put(FactionPermType.INVITE, recruitINVITE);

        allyMap.put(FactionPermType.USE, allyUSE);
        allyMap.put(FactionPermType.PLACE, allyPLACE);
        allyMap.put(FactionPermType.DESTROY, allyDESTROY);

        flagMap.put(FactionMemberType.OFFICER, officerMap);
        flagMap.put(FactionMemberType.MEMBER, membersMap);
        flagMap.put(FactionMemberType.RECRUIT, recruitMap);
        flagMap.put(FactionMemberType.ALLY, allyMap);

        return flagMap;
    }

    public static ConfigurationOptions getDefaultOptions()
    {
        final ConfigurationOptions configurationOptions = ConfigurationOptions.defaults();
        return configurationOptions.setAcceptedTypes(ImmutableSet.of(Map.class, Set.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class,
                Short.class, Byte.class, Number.class));
    }
}
