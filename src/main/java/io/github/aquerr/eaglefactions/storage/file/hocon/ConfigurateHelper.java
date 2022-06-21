package io.github.aquerr.eaglefactions.storage.file.hocon;

import com.google.common.collect.ImmutableSet;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPermType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimSetTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.EFTypeTokens;
import io.github.aquerr.eaglefactions.storage.serializers.SlotItemListTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.SlotItemTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.Vector3iTypeSerializer;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigurateHelper
{
    private static final Logger LOGGER = LogManager.getLogger(ConfigurateHelper.class);

    public static boolean putFactionInNode(final ConfigurationNode configNode, final Faction faction)
    {
        try
        {
            configNode.node("name").set(faction.getName());
            configNode.node("tag").set(LegacyComponentSerializer.legacyAmpersand().serialize(faction.getTag()));
            configNode.node("leader").set(faction.getLeader().toString());
            configNode.node("description").set(faction.getDescription());
            configNode.node("motd").set(faction.getMessageOfTheDay());
            configNode.node("officers").setList(EFTypeTokens.UUID_TOKEN, new ArrayList<>(faction.getOfficers()));
            configNode.node("members").setList(EFTypeTokens.UUID_TOKEN, new ArrayList<>(faction.getMembers()));
            configNode.node("recruits").setList(EFTypeTokens.UUID_TOKEN, new ArrayList<>(faction.getRecruits()));
            configNode.node("truces").set(faction.getTruces());
            configNode.node("alliances").set(faction.getAlliances());
            configNode.node("enemies").set(faction.getEnemies());
            configNode.node("claims").set(EFTypeTokens.CLAIM_SET_TYPE_TOKEN, faction.getClaims());
            configNode.node("last_online").set(faction.getLastOnline().toString());
            configNode.node("perms").set(faction.getPerms());
            configNode.node("chest").set(EFTypeTokens.LIST_SLOT_ITEM_TYPE_TOKEN, faction.getChest().getItems());
            configNode.node("isPublic").set(faction.isPublic());

            if(faction.getHome() == null)
            {
                configNode.node("home").set(faction.getHome());
            }
            else
            {
                configNode.node("home").set(faction.getHome().getWorldUUID().toString() + '|' + faction.getHome().getBlockPosition().toString());
            }
            return true;
        }
        catch(final Exception exception)
        {
            LOGGER.error(PluginInfo.PLUGIN_PREFIX_PLAIN + "Error while putting faction '" + faction.getName() + "' in node.");
            exception.printStackTrace();
            return false;
        }
    }

    public static boolean putPlayerInNode(final ConfigurationNode configNode, final FactionPlayer factionPlayer)
    {
        try
        {
            configNode.node("faction").set(factionPlayer.getFactionName().orElse(""));
            configNode.node("name").set(factionPlayer.getName());
            configNode.node("power").set(factionPlayer.getPower());
            configNode.node("maxpower").set(factionPlayer.getMaxPower());
            configNode.node("death-in-warzone").set(factionPlayer.diedInWarZone());
            return true;
        }
        catch (Exception exception)
        {
            LOGGER.error(PluginInfo.PLUGIN_PREFIX_PLAIN + "Error while putting player'" + factionPlayer.getName() + "' in node.");
            exception.printStackTrace();
            return false;
        }
    }

    public static List<Faction> getFactionsFromNode(final ConfigurationNode configNode)
    {
        final List<Faction> factions = new ArrayList<>();

        final Set<Object> keySet = configNode.childrenMap().keySet();
        for(final Object object : keySet)
        {
            if(object instanceof String)
            {
                try
                {
                    final Faction faction = getFactionFromNode(configNode.node(object));
                    factions.add(faction);
                }
                catch (final Exception e)
                {
                    LOGGER.error(PluginInfo.PLUGIN_PREFIX_PLAIN + "Error while getting faction'" + object + "' from node.");
                    e.printStackTrace();
                }
            }
        }
        return factions;
    }

    public static Faction getFactionFromNode(final ConfigurationNode configNode) throws SerializationException
    {
        String factionName;
        factionName = configNode.node("name").getString();

        // Backwards compatibility
        //TODO: Remove in future release
        if (configNode.key() != null)
            factionName = (String) configNode.key();

        final TextComponent tag = LegacyComponentSerializer.legacyAmpersand().deserialize(configNode.node("tag").getString());
        final String description = configNode.node("description").getString();
        final String messageOfTheDay = configNode.node("motd").getString();

        //Backwards compatibility. We need to have a proper uuid stored in leader field.
        //TODO: Remove in future release
        final Object leaderValue = configNode.node("leader").key();
        if (leaderValue == null || (leaderValue instanceof String && ((String) leaderValue).trim().equals("")))
            configNode.node("leader").set(EFTypeTokens.UUID_TOKEN, new UUID(0, 0));
        //

        final UUID leader = configNode.node("leader").get(EFTypeTokens.UUID_TOKEN, new UUID(0,0));
        final FactionHome home = FactionHome.from(String.valueOf(configNode.node("home").getString("")));
        final Set<UUID> officers = configNode.node("officers").get(EFTypeTokens.UUID_SET_TYPE_TOKEN, Collections.emptySet());
        final Set<UUID> members = configNode.node("members").get(EFTypeTokens.UUID_SET_TYPE_TOKEN, Collections.emptySet());
        final Set<UUID> recruits = configNode.node("recruits").get(EFTypeTokens.UUID_SET_TYPE_TOKEN, Collections.emptySet());
        final Set<String> alliances = new HashSet<>(configNode.node("alliances").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        final Set<String> enemies = new HashSet<>(configNode.node("enemies").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        final Set<String> truces = new HashSet<>(configNode.node("truces").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        final Set<Claim> claims = configNode.node("claims").get(EFTypeTokens.CLAIM_SET_TYPE_TOKEN, Collections.emptySet());
        final Instant lastOnline = configNode.node("last_online").get(Instant.class) != null ? Instant.parse(configNode.node("last_online").getString()) : Instant.now();
        final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = getFactionPermsFromNode(configNode.node("perms"));
        final List<FactionChest.SlotItem> slotItems = configNode.node("chest").get(EFTypeTokens.LIST_SLOT_ITEM_TYPE_TOKEN);

        FactionChest chest;
        if (slotItems == null)
            chest = new FactionChestImpl(factionName);
        else
            chest = new FactionChestImpl(factionName, slotItems);
        final boolean isPublic = configNode.node("isPublic").getBoolean(false);

        return FactionImpl.builder(factionName, tag, leader)
                .setDescription(description)
                .setMessageOfTheDay(messageOfTheDay)
                .setHome(home)
                .setOfficers(officers)
                .setMembers(members)
                .setRecruits(recruits)
                .setAlliances(alliances)
                .setEnemies(enemies)
                .setTruces(truces)
                .setClaims(claims)
                .setLastOnline(lastOnline)
                .setPerms(perms)
                .setChest(chest)
                .setIsPublic(isPublic)
                .build();
    }

    public static FactionPlayer getPlayerFromFile(final File file)
    {
        HoconConfigurationLoader playerConfigLoader = HoconConfigurationLoader.builder().file(file).build();
        try
        {
            ConfigurationNode playerNode = playerConfigLoader.load();
            String playerName = playerNode.node("name").getString("");
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
            String factionName = playerNode.node("faction").getString("");
            float power = playerNode.node("power").getFloat(0.0f);
            float maxpower = playerNode.node("maxpower").getFloat(0.0f);
            boolean diedInWarZone = playerNode.node("death-in-warzone").getBoolean(false);

            return new FactionPlayerImpl(playerName, playerUUID, factionName, power, maxpower, diedInWarZone);
        }
        catch(IOException e)
        {
            LOGGER.error(PluginInfo.PLUGIN_PREFIX_PLAIN + "Error while opening the file " + file.getName());
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
        boolean officerUSE = factionNode.node("OFFICER", "USE").getBoolean(true);
        boolean officerPLACE = factionNode.node("OFFICER", "PLACE").getBoolean(true);
        boolean officerDESTROY = factionNode.node("OFFICER", "DESTROY").getBoolean(true);
        boolean officerCLAIM = factionNode.node("OFFICER", "CLAIM").getBoolean(true);
        boolean officerATTACK = factionNode.node("OFFICER", "ATTACK").getBoolean(true);
        boolean officerINVITE = factionNode.node("OFFICER", "INVITE").getBoolean(true);
        boolean officerCHEST = factionNode.node("OFFICER", "CHEST").getBoolean(true);

        //Get member perms
        boolean memberUSE = factionNode.node("MEMBER", "USE").getBoolean(true);
        boolean memberPLACE = factionNode.node("MEMBER", "PLACE").getBoolean(true);
        boolean memberDESTROY = factionNode.node("MEMBER", "DESTROY").getBoolean(true);
        boolean memberCLAIM = factionNode.node("MEMBER", "CLAIM").getBoolean(false);
        boolean memberATTACK = factionNode.node("MEMBER", "ATTACK").getBoolean(false);
        boolean memberINVITE = factionNode.node("MEMBER", "INVITE").getBoolean(true);
        boolean memberCHEST = factionNode.node("MEMBER", "CHEST").getBoolean(true);

        //Get recruit perms
        boolean recruitUSE = factionNode.node("RECRUIT", "USE").getBoolean(true);
        boolean recruitPLACE = factionNode.node("RECRUIT", "PLACE").getBoolean(true);
        boolean recruitDESTROY = factionNode.node("RECRUIT", "DESTROY").getBoolean(true);
        boolean recruitCLAIM = factionNode.node("RECRUIT", "CLAIM").getBoolean(false);
        boolean recruitATTACK = factionNode.node("RECRUIT", "ATTACK").getBoolean(false);
        boolean recruitINVITE = factionNode.node("RECRUIT", "INVITE").getBoolean(false);
        boolean recruitCHEST = factionNode.node("RECRUIT", "CHEST").getBoolean(false);

        //Get ally perms
        boolean allyUSE = factionNode.node("ALLY", "USE").getBoolean(true);
        boolean allyPLACE = factionNode.node( "ALLY", "PLACE").getBoolean(false);
        boolean allyDESTROY = factionNode.node("ALLY", "DESTROY").getBoolean(false);

        officerMap.put(FactionPermType.USE, officerUSE);
        officerMap.put(FactionPermType.PLACE, officerPLACE);
        officerMap.put(FactionPermType.DESTROY, officerDESTROY);
        officerMap.put(FactionPermType.CLAIM, officerCLAIM);
        officerMap.put(FactionPermType.ATTACK, officerATTACK);
        officerMap.put(FactionPermType.INVITE, officerINVITE);
        officerMap.put(FactionPermType.CHEST, officerCHEST);

        membersMap.put(FactionPermType.USE, memberUSE);
        membersMap.put(FactionPermType.PLACE, memberPLACE);
        membersMap.put(FactionPermType.DESTROY, memberDESTROY);
        membersMap.put(FactionPermType.CLAIM, memberCLAIM);
        membersMap.put(FactionPermType.ATTACK, memberATTACK);
        membersMap.put(FactionPermType.INVITE, memberINVITE);
        membersMap.put(FactionPermType.CHEST, memberCHEST);

        recruitMap.put(FactionPermType.USE, recruitUSE);
        recruitMap.put(FactionPermType.PLACE, recruitPLACE);
        recruitMap.put(FactionPermType.DESTROY, recruitDESTROY);
        recruitMap.put(FactionPermType.CLAIM, recruitCLAIM);
        recruitMap.put(FactionPermType.ATTACK, recruitATTACK);
        recruitMap.put(FactionPermType.INVITE, recruitINVITE);
        recruitMap.put(FactionPermType.CHEST, recruitCHEST);

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
        TypeSerializerCollection collection = TypeSerializerCollection.builder()
                .registerAll(TypeSerializerCollection.defaults())
                .register(TypeToken.get(Claim.class), new ClaimTypeSerializer())
                .register(new TypeToken<Set<Claim>>() {}, new ClaimSetTypeSerializer())
                .register(EFTypeTokens.LIST_SLOT_ITEM_TYPE_TOKEN, new SlotItemListTypeSerializer())
                .register(EFTypeTokens.SLOT_ITEM_TYPE_TOKEN, new SlotItemTypeSerializer())
                .register(EFTypeTokens.VECTOR_3I_TOKEN, new Vector3iTypeSerializer())
                .build();

        final ConfigurationOptions configurationOptions = ConfigurationOptions.defaults()
                .serializers(collection);
        return configurationOptions.nativeTypes(ImmutableSet.of(Map.class, Set.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class,
                Short.class, Byte.class, Number.class));
    }
}
