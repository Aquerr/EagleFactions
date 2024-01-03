package io.github.aquerr.eaglefactions.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionType;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.github.aquerr.eaglefactions.PluginPermissions.SAFE_ZONE_BUILD;
import static io.github.aquerr.eaglefactions.PluginPermissions.SAFE_ZONE_INTERACT;
import static io.github.aquerr.eaglefactions.PluginPermissions.WAR_ZONE_BUILD;
import static io.github.aquerr.eaglefactions.PluginPermissions.WAR_ZONE_INTERACT;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.*;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.forbidden;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.forbiddenSafeZone;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.forbiddenWarZone;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.okEagleFeather;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.okSafeZone;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.okWarZone;
import static io.github.aquerr.eaglefactions.util.WorldUtil.getPlainWorldName;
import static java.util.Optional.ofNullable;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

@Singleton
public class ProtectionManagerImpl implements ProtectionManager
{
    private static final String ITEM_IT_AND_FACTION_TYPE_MUST_BE_PROVIDED = "Item id and faction type must be provided";
    private final FactionLogic factionLogic;
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final MessageService messageService;
    private final ProtectionConfig protectionConfig;
    private final ChatConfig chatConfig;
    private final FactionsConfig factionsConfig;

    public ProtectionManagerImpl(final FactionLogic factionLogic,
                                 final PermsManager permsManager,
                                 final PlayerManager playerManager,
                                 final MessageService messageService,
                                 final ProtectionConfig protectionConfig,
                                 final ChatConfig chatConfig,
                                 final FactionsConfig factionsConfig)
    {
        this.protectionConfig = protectionConfig;
        this.chatConfig = chatConfig;
        this.factionsConfig = factionsConfig;
        this.factionLogic = factionLogic;
        this.permsManager = permsManager;
        this.playerManager = playerManager;
        this.messageService = messageService;
    }

    @Override
    public ProtectionResult canInteractWithBlock(final ServerLocation location, final User user, final boolean shouldNotify)
    {
        final ProtectionResult canInteract = canInteractWithBlock(location, user);
        if (shouldNotify && !canInteract.hasAccess())
            notifyServerPlayer(user);
        return canInteract;
    }

    private ProtectionResult canInteractWithBlock(final ServerLocation location, final User user)
    {
        String blockId = location.blockType().key(RegistryTypes.BLOCK_TYPE).asString();
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("BlockInteract:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + location.blockPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(blockId)));
            }
        }

        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ok();

        if (this.playerManager.hasAdminMode(user))
            return okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        final boolean isBlockCarrierAtLocation = location.blockEntity().isPresent() && location.blockEntity().get() instanceof BlockCarrier;

        if (safeZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForInteraction(blockId, FactionType.SAFE_ZONE))
                return builder().hasAccess(true).isSafeZone(true).build();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return builder().hasAccess(true).isSafeZone(true).build();
            else return forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForInteraction(blockId, FactionType.WAR_ZONE))
                return okWarZone();
            if (user.hasPermission(WAR_ZONE_INTERACT))
                return okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okWarZone();
            return forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition());
        final Optional<Faction> optionalServerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.uniqueId());
        if (!optionalChunkFaction.isPresent())
        {
            if(!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ok();
            else
            {
                if (isBlockWhitelistedForInteraction(blockId, FactionType.WILDERNESS))
                    return ok();
                return forbidden();
            }
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isBlockWhitelistedForInteraction(blockId, FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (chunkFaction.isWarZone())
        {
            if (isBlockWhitelistedForInteraction(blockId, FactionType.WAR_ZONE))
                return okWarZone();
            if(user.hasPermission(WAR_ZONE_INTERACT))
                return okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okEagleFeather();
            return forbiddenWarZone();
        }

        if (isBlockWhitelistedForInteraction(blockId, FactionType.FACTION))
            return ok();

        //If player is not in a faction but there is a faction at chunk
        if(!optionalServerPlayerFaction.isPresent())
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okEagleFeather();
            return forbidden();
        }

        final Faction playerFaction = optionalServerPlayerFaction.get();
        if (this.permsManager.canInteract(user.uniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition()).get()))
            return okFactionPerm();
        else
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okEagleFeather();
            return forbidden();
        }
    }

    @Override
    public ProtectionResult canUseItem(final ServerLocation location, final User user, final ItemStackSnapshot usedItem, final boolean shouldNotify)
    {
        final ProtectionResult canUseItem = canUseItem(location, user, usedItem);
        if (shouldNotify && !canUseItem.hasAccess())
            notifyServerPlayer(user);
        return canUseItem;
    }

    @Override
    public boolean isSafeZone(ServerLocation location)
    {
        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        if (safeZoneWorlds.contains(getPlainWorldName(location.world())))
            return true;

        final Optional<Faction> faction = this.factionLogic.getFactionByChunk(location.world().uniqueId(), location.chunkPosition());
        return faction.map(Faction::isSafeZone).orElse(false);
    }

    private ProtectionResult canUseItem(final ServerLocation location, final User user, final ItemStackSnapshot usedItem)
    {
        String itemId = usedItem.type().key(RegistryTypes.ITEM_TYPE).asString();

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("ItemUsage:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + location.blockPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(location.block().type().key(RegistryTypes.BLOCK_TYPE).asString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Used item: ", GOLD)).append(text(usedItem.type().key(RegistryTypes.ITEM_TYPE).asString())));
            }
        }

        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ok();

        if (this.playerManager.hasAdminMode(user))
            return okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        final boolean isBlockCarrierAtLocation = location.blockEntity().isPresent() && location.blockEntity().get() instanceof BlockCarrier;

        if (safeZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isItemWhitelisted(itemId, FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isItemWhitelisted(itemId, FactionType.WAR_ZONE))
                return okWarZone();
            if (user.hasPermission(WAR_ZONE_INTERACT))
                return okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okWarZone();
            else return forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition());
        final Optional<Faction> optionalServerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.uniqueId());
        if (!optionalChunkFaction.isPresent())
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ok();
            else
            {
                if (isItemWhitelisted(itemId, FactionType.WILDERNESS))
                    return ok();
                return forbidden();
            }
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isItemWhitelisted(itemId, FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (chunkFaction.isWarZone())
        {
            if (isItemWhitelisted(itemId, FactionType.WAR_ZONE))
                return okWarZone();
            if (user.hasPermission(WAR_ZONE_INTERACT))
                return okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okEagleFeather();
            else return forbiddenWarZone();
        }

        if (isItemWhitelisted(itemId, FactionType.FACTION))
            return ok();

        //If player is not in a faction but there is a faction at chunk
        if(!optionalServerPlayerFaction.isPresent())
        {
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okEagleFeather();
            return forbidden();
        }

        Faction playerFaction = optionalServerPlayerFaction.get();
        if(this.permsManager.canInteract(user.uniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition()).get()))
            return okFactionPerm();
        else
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return okEagleFeather();
            else return forbidden();
        }
    }

    @Override
    public ProtectionResult canBreak(final BlockSnapshot blockSnapshot, User user, boolean shouldNotify)
    {
        final ProtectionResult canBreak = canBreak(blockSnapshot, user);
        if (shouldNotify && !canBreak.hasAccess())
            notifyServerPlayer(user);
        return canBreak;
    }

    private ProtectionResult canBreak(final BlockSnapshot blockSnapshot, final User user)
    {
        final String blockId = blockSnapshot.state().type().key(RegistryTypes.BLOCK_TYPE).asString();
        final ServerLocation location = blockSnapshot.location().orElse(null);
        if (location == null)
        {
            EagleFactionsPlugin.getPlugin().printInfo("Broken BlockSnapshot does not contain a location. This is not normal.");
            return forbidden();
        }

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("BlockBreak:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + blockSnapshot.position().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block id: ", GOLD)).append(text(blockId)));
            }
        }

        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ok();

        if(this.playerManager.hasAdminMode(user))
            return okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_BUILD))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.WAR_ZONE))
                return okWarZone();
            if (user.hasPermission(WAR_ZONE_BUILD))
                return okWarZone();
            else return forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition());
        final Optional<Faction> optionalServerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.uniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            {
                if(optionalChunkFaction.get().isSafeZone())
                {
                    if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.SAFE_ZONE))
                        return okSafeZone();
                    if (user.hasPermission(SAFE_ZONE_BUILD))
                        return okSafeZone();
                    else return forbiddenSafeZone();
                }
                else //WarZone
                {
                    if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.WAR_ZONE))
                        return okWarZone();
                    if (user.hasPermission(WAR_ZONE_BUILD))
                        return okWarZone();
                    else return forbiddenWarZone();
                }
            }

            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.FACTION))
                return ok();

            final Faction chunkFaction = optionalChunkFaction.get();
            final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition());

            if (optionalServerPlayerFaction.filter(faction -> this.permsManager.canBreakBlock(user.uniqueId(), faction, optionalChunkFaction.get(), optionalClaim.get())).isPresent())
                return okFactionPerm();
            else return forbidden();
        }
        else
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ok();
            else
            {
                if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.WILDERNESS))
                    return ok();
                return forbidden();
            }
        }
    }

    @Override
    public ProtectionResult canBreak(final BlockSnapshot blockSnapshot)
    {
        final ServerLocation location = blockSnapshot.location().orElse(null);
        if (location == null)
        {
            EagleFactionsPlugin.getPlugin().printInfo("Broken BlockSnapshot does not contain a location. This is not normal.");
            return forbidden();
        }

        final String blockId = blockSnapshot.state().type().key(RegistryTypes.BLOCK_TYPE).asString();
        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ok();

        if(this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.SAFE_ZONE))
                return okSafeZone();
            else return forbiddenSafeZone();
        }

        boolean shouldProtectWarZoneFromMobGrief = !this.factionLogic.getFactionByName(EagleFactionsPlugin.WAR_ZONE_NAME).getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF);
        if(this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(world)) && shouldProtectWarZoneFromMobGrief)
        {
            //Not sure if we should use white-list for mobs...
            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.WAR_ZONE))
                return okWarZone();
            else return forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition());
        if(!optionalChunkFaction.isPresent())
            return ok();

        if(optionalChunkFaction.get().isSafeZone())
        {
            if(isBlockWhitelistedForPlaceDestroy(blockId, FactionType.SAFE_ZONE))
                return okSafeZone();
            if (optionalChunkFaction.get().getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                return okSafeZone();
            else return forbiddenSafeZone();
        }

        if(optionalChunkFaction.get().isWarZone() && shouldProtectWarZoneFromMobGrief)
        {
            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.WAR_ZONE))
                return okWarZone();
            else return forbiddenWarZone();
        }

        if(!optionalChunkFaction.get().getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.FACTION))
                return ok();
            else return forbidden();
        }
        return ok();
    }

    @Override
    public ProtectionResult canPlace(BlockSnapshot blockSnapshot, User player, boolean shouldNotify)
    {
        final ProtectionResult canPlace = canPlace(blockSnapshot, player);
        if (shouldNotify && !canPlace.hasAccess())
            notifyServerPlayer(player);
        return canPlace;
    }

    private ProtectionResult canPlace(final BlockSnapshot blockSnapshot, final User user)
    {
        final ServerLocation location = blockSnapshot.location().orElse(null);
        if (location == null)
        {
            EagleFactionsPlugin.getPlugin().printInfo("Placed BlockSnapshot does not contain a location. This is not normal.");
            return forbidden();
        }

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("BlockPlace:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + blockSnapshot.position().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(blockSnapshot.state().type().key(RegistryTypes.BLOCK_TYPE).asString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Item in hand: ", GOLD)).append(text(ofNullable(user.itemInHand(HandTypes.MAIN_HAND))
                        .map(ItemStack::type)
                        .map(itemType -> itemType.key(RegistryTypes.ITEM_TYPE))
                        .map(ResourceKey::asString)
                        .orElse(""))));
            }
        }

        final String blockId = blockSnapshot.state().type().key(RegistryTypes.BLOCK_TYPE).asString();
        ServerWorld world = location.world();
        final String itemId = user.itemInHand(HandTypes.MAIN_HAND) != null ?
                user.itemInHand(HandTypes.MAIN_HAND).type().key(RegistryTypes.ITEM_TYPE).asString() : "";

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ok();

        if(this.playerManager.hasAdminMode(user))
            return okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_BUILD))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.WAR_ZONE))
                return okWarZone();
            if (user.hasPermission(WAR_ZONE_BUILD))
                return okWarZone();
            else return forbiddenWarZone();
        }

        Optional<Faction> optionalServerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.uniqueId());
        Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            {
                if(optionalChunkFaction.get().isSafeZone())
                {
                    if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.SAFE_ZONE))
                        return okSafeZone();
                    if (user.hasPermission(SAFE_ZONE_BUILD))
                        return okSafeZone();
                    else return forbiddenSafeZone();
                }
                else //WarZone
                {
                    if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.WAR_ZONE))
                        return okWarZone();
                    if (user.hasPermission(WAR_ZONE_BUILD))
                        return okWarZone();
                    else return forbiddenWarZone();
                }
            }

            if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.FACTION))
                return ok();

            final Faction chunkFaction = optionalChunkFaction.get();
            if (optionalServerPlayerFaction.filter(faction -> this.permsManager.canPlaceBlock(user.uniqueId(), faction, chunkFaction, chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition()).get())).isPresent())
                return okFactionPerm();
            else return forbidden();
        }
        else
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ok();
            else
            {
                if (isBlockWhitelistedForPlaceDestroy(blockId, FactionType.WILDERNESS))
                    return ok();
                return forbidden();
            }
        }
    }

    @Override
    public ProtectionResult canExplode(final ServerLocation location, final User user, final boolean shouldNotify)
    {
        final ProtectionResult canExplode = canExplode(location, user);
        if (shouldNotify && !canExplode.hasAccess())
            notifyServerPlayer(user);
        return canExplode;
    }

    private ProtectionResult canExplode(final ServerLocation location, final User user)
    {
        final ServerWorld world = location.world();

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                final ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Explosion:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(location.block().type().key(RegistryTypes.BLOCK_TYPE).asString())));
            }
        }

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ok();

        //Check if admin
        if(this.playerManager.hasAdminMode(user))
            return okAdmin();

        //Check world
        if (this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(world)))
            return forbiddenSafeZone();
        else if (this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(world)))
        {
            boolean allowExplosionsFromPlayersInWarZone = ofNullable(this.factionLogic.getFactionByName(EagleFactionsPlugin.WAR_ZONE_NAME))
                    .map(faction -> faction.getProtectionFlagValue(ProtectionFlagType.ALLOW_EXPLOSION))
                    .orElse(false);
            if (allowExplosionsFromPlayersInWarZone)
                return okWarZone();
            return forbiddenWarZone();
        }

        //If no faction
        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition());
        if (!optionalChunkFaction.isPresent())
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ok();
            return forbidden();
        }

        //If SafeZone or WarZone
        final Faction chunkFaction = optionalChunkFaction.get();
        if(chunkFaction.isSafeZone() || chunkFaction.isWarZone())
        {
            if(chunkFaction.isSafeZone())
            {
                if (user.hasPermission(SAFE_ZONE_BUILD))
                    return okSafeZone();
                if (chunkFaction.getProtectionFlagValue(ProtectionFlagType.ALLOW_EXPLOSION))
                    return okSafeZone();
                else return forbiddenSafeZone();
            }
            else
            {
                if (chunkFaction.isWarZone() && user.hasPermission(WAR_ZONE_BUILD))
                    return okWarZone();
                if (chunkFaction.getProtectionFlagValue(ProtectionFlagType.ALLOW_EXPLOSION))
                    return okWarZone();
                else return forbiddenWarZone();
            }
        }

        //If player is in faction
        final Optional<Faction> optionalServerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.uniqueId());
        if(optionalServerPlayerFaction.isPresent())
        {
            final Faction playerFaction = optionalServerPlayerFaction.get();
            if (chunkFaction.getName().equalsIgnoreCase(playerFaction.getName()))
            {
                if (this.permsManager.canPlaceBlock(user.uniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition()).get()))
                    return okFactionPerm();
                else return forbidden();
            }
        }

        boolean allowExplosionsByOtherServerPlayersInClaims = this.protectionConfig.shouldAllowExplosionsByOtherPlayersInClaims();
        if (allowExplosionsByOtherServerPlayersInClaims)
            return ok();
        else return forbidden();
    }

    @Override
    public ProtectionResult canExplode(final ServerLocation location)
    {
        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(location.world())))
            return ok();

        boolean shouldProtectWarZoneFromMobGrief = !this.factionLogic.getFactionByName(EagleFactionsPlugin.WAR_ZONE_NAME).getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF);

        //Check world
        if (this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(location.world())))
            return forbiddenSafeZone();

        if (this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(location.world())))
        {
            if (!shouldProtectWarZoneFromMobGrief)
                return okWarZone();
            else return forbiddenWarZone();
        }

        Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(location.world().uniqueId(), location.chunkPosition());
        if (!optionalChunkFaction.isPresent())
            return ok();

        Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
            return forbiddenSafeZone();
        else if (chunkFaction.isWarZone() && shouldProtectWarZoneFromMobGrief)
            return forbiddenWarZone();
        else
        {
            if (!this.factionLogic.getFactionByChunk(location.world().uniqueId(), location.chunkPosition())
                    .map(faction -> faction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    .orElse(false))
                return ok();
            else return forbidden();
        }
    }

    @Override
    public ProtectionResult canHitEntity(final Entity attackedEntity, final ServerPlayer player, final boolean shouldNotify)
    {
        final ProtectionResult canAttack = canAttackEntity(attackedEntity, player);
        if (shouldNotify && !canAttack.hasAccess())
            notifyServerPlayer(player.user());
        return canAttack;
    }

    private ProtectionResult canAttackEntity(final Entity attackedEntity, final ServerPlayer player)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(player.uniqueId()))
        {
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("EntityAttack:", GOLD)));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(attackedEntity.serverLocation().world()) + " " + attackedEntity.serverLocation().blockPosition().toString())));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Entity at location: ", GOLD)).append(text(attackedEntity.type().key(RegistryTypes.ENTITY_TYPE).asString())));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(player.name())));
        }


        if (this.playerManager.hasAdminMode(player.user()))
            return okAdmin();

        final boolean isServerPlayer = attackedEntity instanceof ServerPlayer;
        final boolean isMob = !isServerPlayer && (attackedEntity instanceof Living && !(attackedEntity instanceof ArmorStand));
        if (isMob)
            return ok();

        final ServerLocation entityLocation = attackedEntity.serverLocation();
        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(entityLocation.world().uniqueId(), entityLocation.chunkPosition());
        final Optional<Faction> optionalAttackerServerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(player.uniqueId());
        final Optional<Faction> optionalSourceChunkFaction = this.factionLogic.getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition());
        final boolean isSafeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(entityLocation.world()));
        final boolean isWarZoneWorld = !isSafeZoneWorld && this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(entityLocation.world()));
        final boolean notClaimableWorld = !isSafeZoneWorld && !isWarZoneWorld && this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(entityLocation.world()));

        if (isServerPlayer)
        {
            final ServerPlayer attackedServerPlayer = (ServerPlayer) attackedEntity;
            final Optional<Faction> optionalAttackedServerPlayerFaction = this.factionLogic
                    .getFactionByPlayerUUID(attackedServerPlayer.uniqueId());
            if (isSafeZoneWorld)
                return forbiddenSafeZone();
            if (optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
                return forbiddenSafeZone();
            if (attackedEntity.equals(player))
                return ok();
            if (optionalSourceChunkFaction.isPresent() && optionalSourceChunkFaction.get().isSafeZone())
                return forbiddenSafeZone();
            if (!optionalAttackerServerPlayerFaction.isPresent())
                return ok();
            if (!optionalAttackedServerPlayerFaction.isPresent())
                return ok();
            final Faction attackedFaction = optionalAttackedServerPlayerFaction.get();
            final Faction attackerFaction = optionalAttackerServerPlayerFaction.get();
            if (!attackerFaction.getName().equals(attackedFaction.getName()))
            {
                if (attackerFaction.isAlly(attackedFaction) && !this.factionsConfig.isAllianceFriendlyFire())
                    return forbidden();
                else if (attackerFaction.isTruce(attackedFaction) && !this.factionsConfig.isTruceFriendlyFire())
                    return forbidden();
            }
            else
            {
                if (this.factionsConfig.isFactionFriendlyFire())
                    return ok();
                else return forbidden();
            }
        }
        else //Item Frame, Minecraft, Painting etc.
        {
            //Not claimable worlds should be always ignored by protection system.
            if (notClaimableWorld)
                return ok();
            if (isSafeZoneWorld)
                return forbiddenSafeZone();
            if (isWarZoneWorld)
            {
                if (!this.protectionConfig.shouldProtectWarzoneFromPlayers())
                    return okWarZone();
                else return forbiddenWarZone();
            }
            if (!optionalChunkFaction.isPresent())
                return ok();
            final Faction chunkFaction = optionalChunkFaction.get();
            if (chunkFaction.isSafeZone())
                return forbiddenSafeZone();
            else if (chunkFaction.isWarZone())
            {
                if(!this.protectionConfig.shouldProtectWarzoneFromPlayers())
                    return okWarZone();
                else return forbiddenWarZone();
            }
            if (!optionalAttackerServerPlayerFaction.isPresent())
                return forbidden();
            final Faction attackerFaction = optionalAttackerServerPlayerFaction.get();
            if (this.permsManager.canBreakBlock(player.uniqueId(), attackerFaction, chunkFaction, chunkFaction.getClaimAt(entityLocation.world().uniqueId(), entityLocation.chunkPosition()).get()))
                return okFactionPerm();
            return forbidden();
        }
        return ok();
    }

    @Override
    public ProtectionResult canNotifyBlock(final ServerLocation notifier, final ServerLocation notifiedLocation)
    {
        final String notifiedBlockId = notifiedLocation.block().type().key(RegistryTypes.BLOCK_TYPE).asString();
        //First, let's check the world.
        //TODO: Maybe we should check notifier's world as well?
        final boolean isSafeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(notifiedLocation.world()));
        final boolean isWarZoneWorld = !isSafeZoneWorld && this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(notifiedLocation.world()));
        final boolean notClaimableWorld = !isSafeZoneWorld && !isWarZoneWorld && this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(notifiedLocation.world()));

        //Entire world is one claim type thus we should allow the notification.
        if (isSafeZoneWorld || isWarZoneWorld || notClaimableWorld)
            return ok();

        final Optional<Faction> notifierFaction = this.factionLogic.getFactionByChunk(notifier.world().uniqueId(), notifier.chunkPosition());
        final Optional<Faction> notifiedFaction = this.factionLogic.getFactionByChunk(notifiedLocation.world().uniqueId(), notifiedLocation.chunkPosition());

        // Factions can notify wilderness but wilderness cannot notify factions.
        // Wilderness can only notify other factions if mob-gref is set to true.

        //Source is wilderness.
        if (!notifierFaction.isPresent())
        {
            //Both wilderness
            if (!notifiedFaction.isPresent())
                return ok();
            final Faction faction = notifiedFaction.get();
            if (faction.isSafeZone()) //Notified SafeZone
                return forbiddenSafeZone();
            else if(faction.isWarZone()) //Notified WarZone
            {
                if (faction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return okWarZone();
                else return forbiddenWarZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedBlockId, FactionType.FACTION))
                    return ok();
                if(faction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF)) //Notified Regular faction
                    return ok();
                else return forbidden();
            }

        }

        final Faction sourceFaction = notifierFaction.get();

        //Regular factions can notify locations in wilderness.
        if (!notifiedFaction.isPresent())
            return ok();

        final Faction targetFaction = notifiedFaction.get();

        //Check if factions are equal.
        if(targetFaction.equals(sourceFaction))
            return ok();

        if (sourceFaction.isSafeZone())
        {
            if(targetFaction.isWarZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedBlockId, FactionType.WAR_ZONE))
                    return okWarZone();
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return okWarZone();
                else return forbiddenWarZone();
            }
            else
            {
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return ok();
                else return forbidden();
            }
        }
        else if (sourceFaction.isWarZone())
        {
             if(targetFaction.isSafeZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedBlockId, FactionType.SAFE_ZONE))
                    return okSafeZone();
                return forbiddenSafeZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedBlockId, FactionType.FACTION))
                    return ok();
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return ok();
                else return forbidden();
            }
        }
        else
        {
            if(targetFaction.isSafeZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedBlockId, FactionType.SAFE_ZONE))
                    return okSafeZone();
                return forbiddenSafeZone();
            }
            else if(targetFaction.isWarZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedBlockId, FactionType.WAR_ZONE))
                    return okWarZone();
                if(targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return okWarZone();
                else return forbiddenWarZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedBlockId, FactionType.FACTION))
                    return ok();
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return ok();
                else return forbidden();
            }
        }
    }

    @Override
    public boolean isItemWhitelisted(final String itemId, final FactionType factionType)
    {
        if (StringUtils.isBlank(itemId) || Objects.isNull(factionType))
            throw new IllegalArgumentException(ITEM_IT_AND_FACTION_TYPE_MUST_BE_PROVIDED);

        switch (factionType)
        {
            case FACTION:
                return this.protectionConfig.getFactionWhitelists().isItemWhiteListed(itemId);
            case WAR_ZONE:
                return this.protectionConfig.getWarZoneWhitelists().isItemWhiteListed(itemId);
            case SAFE_ZONE:
                return this.protectionConfig.getSafeZoneWhitelists().isItemWhiteListed(itemId);
            case WILDERNESS:
                return this.protectionConfig.getWildernessWhitelists().isItemWhiteListed(itemId);
            default:
                return false;
        }
    }

    @Override
    public boolean isBlockWhitelistedForInteraction(final String blockId, final FactionType factionType)
    {
        if (StringUtils.isBlank(blockId) || Objects.isNull(factionType))
            throw new IllegalArgumentException(ITEM_IT_AND_FACTION_TYPE_MUST_BE_PROVIDED);

        switch (factionType)
        {
            case FACTION:
                return this.protectionConfig.getFactionWhitelists().isBlockWhiteListedForInteraction(blockId);
            case WAR_ZONE:
                return this.protectionConfig.getWarZoneWhitelists().isBlockWhiteListedForInteraction(blockId);
            case SAFE_ZONE:
                return this.protectionConfig.getSafeZoneWhitelists().isBlockWhiteListedForInteraction(blockId);
            case WILDERNESS:
                return this.protectionConfig.getWildernessWhitelists().isBlockWhiteListedForInteraction(blockId);
            default:
                return false;
        }
    }

    @Override
    public boolean isBlockWhitelistedForPlaceDestroy(final String blockOrItemId, final FactionType factionType)
    {
        if (StringUtils.isBlank(blockOrItemId) || Objects.isNull(factionType))
            throw new IllegalArgumentException(ITEM_IT_AND_FACTION_TYPE_MUST_BE_PROVIDED);

        switch (factionType)
        {
            case FACTION:
                return this.protectionConfig.getFactionWhitelists().isBlockWhitelistedForPlaceDestroy(blockOrItemId);
            case WAR_ZONE:
                return this.protectionConfig.getWarZoneWhitelists().isBlockWhitelistedForPlaceDestroy(blockOrItemId);
            case SAFE_ZONE:
                return this.protectionConfig.getSafeZoneWhitelists().isBlockWhitelistedForPlaceDestroy(blockOrItemId);
            case WILDERNESS:
                return this.protectionConfig.getWildernessWhitelists().isBlockWhitelistedForPlaceDestroy(blockOrItemId);
            default:
                return false;
        }
    }

    private boolean isHoldingEagleFeather(final User user)
    {
        return ofNullable(user.itemInHand(HandTypes.MAIN_HAND))
                .filter(itemStack -> ItemTypes.FEATHER.find().orElse(null) == itemStack.type())
                .map(itemStack -> itemStack.get(EagleFactionsPlugin.IS_EAGLE_FEATHER_KEY)
                        .orElse(false))
                .orElse(false);
    }

    private void notifyServerPlayer(final User user)
    {
        if (this.chatConfig.shouldDisplayProtectionSystemMessages())
        {
            user.player().ifPresent(x->x.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS))));
        }
    }
}
