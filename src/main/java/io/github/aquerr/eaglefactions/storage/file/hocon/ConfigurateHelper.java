package io.github.aquerr.eaglefactions.storage.file.hocon;

import com.google.common.collect.ImmutableSet;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimSetTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.EFTypeSerializers;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.util.Strings;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

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
            configNode.node("name").set(faction.getName());
            configNode.node("tag").set(PlainTextComponentSerializer.plainText().serialize(faction.getTag()));
            configNode.node("leader").set(faction.getLeader().toString());
            configNode.node("description").set(faction.getDescription());
            configNode.node("motd").set(faction.getMessageOfTheDay());
            configNode.node("officers").set(new TypeToken<ArrayList<UUID>>() {}, new ArrayList<>(faction.getOfficers()));
            configNode.node("members").set(new TypeToken<ArrayList<UUID>>() {}, new ArrayList<>(faction.getMembers()));
            configNode.node("recruits").set(new TypeToken<ArrayList<UUID>>() {}, new ArrayList<>(faction.getRecruits()));
            configNode.node("truces").set(faction.getTruces());
            configNode.node("alliances").set(faction.getAlliances());
            configNode.node("enemies").set(faction.getEnemies());
            configNode.node("claims").set(EFTypeSerializers.CLAIM_SET_TYPE_TOKEN, faction.getClaims());
            configNode.node("last_online").set(faction.getLastOnline().toString());
            configNode.node("perms").set(faction.getPerms());
            configNode.node("chest").set(new TypeToken<List<FactionChest.SlotItem>>(){}, faction.getChest().getItems());
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
            exception.printStackTrace();
            return false;
        }
    }

    public static boolean putPlayerInNode(final ConfigurationNode configNode, final FactionPlayer factionPlayer)
    {
        try
        {
            configNode.node("faction").set(factionPlayer.getFactionName().orElse(""));
            configNode.node("faction-member-type").set(factionPlayer.getFactionRole().toString());
            configNode.node("name").set(factionPlayer.getName());
            configNode.node("power").set(factionPlayer.getPower());
            configNode.node("maxpower").set(factionPlayer.getMaxPower());
            configNode.node("death-in-warzone").set(factionPlayer.diedInWarZone());
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

        final TextComponent tag = PlainTextComponentSerializer.plainText().deserialize(configNode.node("tag").getString(""));
        final String description = configNode.node("description").getString();
        final String messageOfTheDay = configNode.node("motd").getString();

        final UUID leader = configNode.node("leader").get(TypeToken.get(UUID.class), new UUID(0,0));
        final FactionHome home = FactionHome.from(String.valueOf(configNode.node("home").getString("")));
        final Set<UUID> officers = configNode.node("officers").get(EFTypeSerializers.UUID_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Set<UUID> members = configNode.node("members").get(EFTypeSerializers.UUID_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Set<UUID> recruits = configNode.node("recruits").get(EFTypeSerializers.UUID_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Set<String> alliances = new HashSet<>(configNode.node("alliances").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(Strings::isNotBlank).collect(Collectors.toSet());
        final Set<String> enemies = new HashSet<>(configNode.node("enemies").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(Strings::isNotBlank).collect(Collectors.toSet());
        final Set<String> truces = new HashSet<>(configNode.node("truces").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(Strings::isNotBlank).collect(Collectors.toSet());
        final Set<Claim> claims = configNode.node("claims").get(EFTypeSerializers.CLAIM_SET_TYPE_TOKEN, Collections.EMPTY_SET);
        final Instant lastOnline = configNode.node("last_online").getString() != null ? Instant.parse(configNode.node("last_online").getString()) : Instant.now();
        final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = getFactionPermsFromNode(configNode.node("perms"));
        List<FactionChest.SlotItem> slotItems = configNode.node("chest").get(new TypeToken<List<FactionChest.SlotItem>>() {});

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
            //TODO: What about factionMemeberType???
            String factionMemberTypeString = playerNode.node("faction-member-type").getString("");
            float power = playerNode.node("power").getFloat(0.0f);
            float maxpower = playerNode.node("maxpower").getFloat(0.0f);
            boolean diedInWarZone = playerNode.node("death-in-warzone").getBoolean(false);

            return new FactionPlayerImpl(playerName, playerUUID, factionName, power, maxpower, diedInWarZone);
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
        boolean officerUSE = factionNode.node("OFFICER", "USE").getBoolean(true);
        boolean officerPLACE = factionNode.node("OFFICER", "PLACE").getBoolean(true);
        boolean officerDESTROY = factionNode.node("OFFICER", "DESTROY").getBoolean(true);
        boolean officerCLAIM = factionNode.node("OFFICER", "CLAIM").getBoolean(true);
        boolean officerATTACK = factionNode.node("LEADER", "ATTACK").getBoolean(true);
        boolean officerINVITE = factionNode.node("OFFICER", "INVITE").getBoolean(true);

        //Get member perms
        boolean memberUSE = factionNode.node("MEMBER", "USE").getBoolean(true);
        boolean memberPLACE = factionNode.node("MEMBER", "PLACE").getBoolean(true);
        boolean memberDESTROY = factionNode.node("MEMBER", "DESTROY").getBoolean(true);
        boolean memberCLAIM = factionNode.node("MEMBER", "CLAIM").getBoolean(false);
        boolean memberATTACK = factionNode.node("LEADER", "ATTACK").getBoolean(false);
        boolean memberINVITE = factionNode.node("MEMBER", "INVITE").getBoolean(true);

        //Get recruit perms
        boolean recruitUSE = factionNode.node("RECRUIT", "USE").getBoolean(true);
        boolean recruitPLACE = factionNode.node("RECRUIT", "PLACE").getBoolean(true);
        boolean recruitDESTROY = factionNode.node("RECRUIT", "DESTROY").getBoolean(true);
        boolean recruitCLAIM = factionNode.node("RECRUIT", "CLAIM").getBoolean(false);
        boolean recruitATTACK = factionNode.node("RECRUIT", "ATTACK").getBoolean(false);
        boolean recruitINVITE = factionNode.node("RECRUIT", "INVITE").getBoolean(false);

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
        TypeSerializerCollection typeSerializerCollection = TypeSerializerCollection.defaults().childBuilder()
                .register(Claim.class, new ClaimTypeSerializer())
                .register(new TypeToken<Set<Claim>>(){}, new ClaimSetTypeSerializer())
                .build();

        return ConfigurationOptions.defaults()
                .serializers(typeSerializerCollection)
                .nativeTypes(ImmutableSet.of(Map.class, Set.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class,
                Short.class, Byte.class, Number.class));
    }
}
