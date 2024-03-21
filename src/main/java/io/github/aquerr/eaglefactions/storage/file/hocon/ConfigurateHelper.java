package io.github.aquerr.eaglefactions.storage.file.hocon;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.logic.FactionLogicImpl;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimSetTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.EFTypeTokens;
import io.github.aquerr.eaglefactions.storage.serializers.FactionMemberTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.ProtectionFlagTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.RankTypeSerializer;
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
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            configNode.node("leader").set(faction.getLeader()
                    .map(FactionMember::getUniqueId)
                    .map(UUID::toString)
                    .orElse(null));
            configNode.node("description").set(faction.getDescription());
            configNode.node("motd").set(faction.getMessageOfTheDay());
            configNode.node("members").setList(EFTypeTokens.FACTION_MEMBER_TYPE_TOKEN, new ArrayList<>(faction.getMembers()));
            configNode.node("truces").set(faction.getTruces());
            configNode.node("alliances").set(faction.getAlliances());
            configNode.node("enemies").set(faction.getEnemies());

            configNode.node("relations").set(buildRelationsObject(faction));

            configNode.node("truces-permissions").set(faction.getRelationPermissions(RelationType.TRUCE).stream().map(Enum::name).collect(Collectors.toSet()));
            configNode.node("alliances-permissions").set(faction.getRelationPermissions(RelationType.ALLIANCE).stream().map(Enum::name).collect(Collectors.toSet()));
            configNode.node("claims").set(EFTypeTokens.CLAIM_SET_TYPE_TOKEN, faction.getClaims());
            configNode.node("last-online").set(faction.getLastOnline().toString());
            configNode.node("created_date").set(String.valueOf(faction.getCreatedDate()));
            configNode.node("ranks").setList(EFTypeTokens.RANK_TYPE_TOKEN, faction.getRanks());
            configNode.node("chest").set(EFTypeTokens.LIST_SLOT_ITEM_TYPE_TOKEN, faction.getChest().getItems());
            configNode.node("is-public").set(faction.isPublic());
            configNode.node("protection-flags").set(EFTypeTokens.PROTECTION_FLAGS_SET_TYPE_TOKEN, faction.getProtectionFlags());

            if(faction.getHome().isEmpty())
            {
                configNode.node("home").set(faction.getHome());
            }
            else
            {
                configNode.node("home").set(faction.getHome().get().toString());
            }
            return true;
        }
        catch(final Exception exception)
        {
            LOGGER.error(PluginInfo.PLUGIN_PREFIX_PLAIN + "Error while putting faction '" + faction.getName() + "' in node.", exception);
            return false;
        }
    }

    private static Relations buildRelationsObject(Faction faction)
    {
        Relations relations = new Relations();
        relations.setRelations(Map.of(
                RelationType.ALLIANCE, new Relations.Relation(faction.getAlliances(), faction.getRelationPermissions(RelationType.ALLIANCE).stream()
                        .map(Enum::name).collect(Collectors.toSet())),
                RelationType.TRUCE, new Relations.Relation(faction.getTruces(), faction.getRelationPermissions(RelationType.TRUCE).stream()
                        .map(Enum::name).collect(Collectors.toSet())),
                RelationType.ENEMY, new Relations.Relation(faction.getEnemies(), Set.of())
        ));
        return relations;
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
            LOGGER.error("{}Error while putting player '{}' in node.", PluginInfo.PLUGIN_PREFIX_PLAIN, factionPlayer.getName(), exception);
            return false;
        }
    }

    public static Faction getFactionFromNode(final ConfigurationNode configNode) throws SerializationException
    {
        String factionName;
        factionName = configNode.node("name").getString();

        final TextComponent tag = LegacyComponentSerializer.legacyAmpersand().deserialize(configNode.node("tag").getString());
        final String description = configNode.node("description").getString();
        final String messageOfTheDay = configNode.node("motd").getString();
        final UUID leader = configNode.node("leader").get(EFTypeTokens.UUID_TOKEN, (UUID) null);
        final FactionHome home = FactionHome.from(String.valueOf(configNode.node("home").getString("")));
        final Set<FactionMember> members = new HashSet<>(configNode.node("members").getList(EFTypeTokens.FACTION_MEMBER_TYPE_TOKEN, Collections.emptyList()));
        final Set<String> alliances = new HashSet<>(configNode.node("alliances").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        final Set<String> enemies = new HashSet<>(configNode.node("enemies").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        final Set<String> truces = new HashSet<>(configNode.node("truces").getList(TypeToken.get(String.class), Collections.emptyList())).stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        final Set<FactionPermission> trucesPermissions = configNode.node("truces-permissions").getList(TypeToken.get(String.class), Collections.emptyList()).stream()
                .filter(StringUtils::isNotBlank)
                .map(FactionPermission::valueOf)
                .collect(Collectors.toSet());
        final Set<FactionPermission> alliancesPermissions = configNode.node("alliances-permissions").getList(TypeToken.get(String.class), Collections.emptyList()).stream()
                .filter(StringUtils::isNotBlank)
                .map(FactionPermission::valueOf)
                .collect(Collectors.toSet());
        final Set<Claim> claims = configNode.node("claims").get(EFTypeTokens.CLAIM_SET_TYPE_TOKEN, Collections.emptySet());
        final Instant lastOnline = configNode.node("last-online").get(Instant.class) != null ? Instant.parse(configNode.node("last-online").getString(Instant.now().toString())) : Instant.now();
        final List<Rank> ranks = new ArrayList<>(configNode.node("ranks").getList(EFTypeTokens.RANK_TYPE_TOKEN, Collections.emptyList()));
        final List<FactionChest.SlotItem> slotItems = configNode.node("chest").get(EFTypeTokens.LIST_SLOT_ITEM_TYPE_TOKEN);
        final Set<ProtectionFlag> protectionFlags = configNode.node("protection-flags").get(EFTypeTokens.PROTECTION_FLAGS_SET_TYPE_TOKEN, Collections.emptySet());

        FactionChest chest;
        if (slotItems == null)
            chest = new FactionChestImpl(factionName);
        else
            chest = new FactionChestImpl(factionName, slotItems);
        final boolean isPublic = configNode.node("is-public").getBoolean(false);

        return FactionImpl.builder(factionName, tag)
                .leader(leader)
                .description(description)
                .messageOfTheDay(messageOfTheDay)
                .home(home)
                .members(members)
                .alliances(alliances)
                .enemies(enemies)
                .truces(truces)
                .claims(claims)
                .lastOnline(lastOnline)
                .ranks(ranks)
                .chest(chest)
                .isPublic(isPublic)
                .protectionFlags(protectionFlags)
                .trucePermissions(trucesPermissions)
                .alliancePermissions(alliancesPermissions)
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
            LOGGER.error(PluginInfo.PLUGIN_PREFIX_PLAIN + "Error while opening the file " + file.getName(), e);
            return null;
        }
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
                .register(EFTypeTokens.PROTECTION_FLAG_TYPE_TOKEN, new ProtectionFlagTypeSerializer())
                .register(EFTypeTokens.RANK_TYPE_TOKEN, new RankTypeSerializer())
                .register(EFTypeTokens.FACTION_MEMBER_TYPE_TOKEN, new FactionMemberTypeSerializer())
                .build();

        final ConfigurationOptions configurationOptions = ConfigurationOptions.defaults()
                .serializers(collection);
        return configurationOptions.nativeTypes(Set.of(Map.class, Set.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class,
                Short.class, Byte.class, Number.class));
    }

    private ConfigurateHelper()
    {
        throw new IllegalStateException("This class should not be instantiated!");
    }

    @ConfigSerializable
    private static class Relations
    {
        @Setting(value = "relations", nodeFromParent = true)
        private Map<RelationType, Relation> relations;

        public Map<RelationType, Relation> getRelations()
        {
            return relations;
        }

        public void setRelations(Map<RelationType, Relation> relations)
        {
            this.relations = relations;
        }

        @ConfigSerializable
        private static class Relation
        {
            @Setting("factions")
            private Set<String> factions;
            @Setting("permissions")
            private Set<String> permissions;

            public Relation()
            {

            }

            public Relation(Set<String> factions, Set<String> permissions)
            {
                this.factions = factions;
                this.permissions = permissions;
            }

            public Set<String> getFactions()
            {
                return factions;
            }

            public Set<String> getPermissions()
            {
                return permissions;
            }

            public void setFactions(Set<String> factions)
            {
                this.factions = factions;
            }

            public void setPermissions(Set<String> permissions)
            {
                this.permissions = permissions;
            }
        }
    }
}
