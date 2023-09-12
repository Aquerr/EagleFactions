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
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.github.aquerr.eaglefactions.PluginPermissions.SAFE_ZONE_BUILD;
import static io.github.aquerr.eaglefactions.PluginPermissions.SAFE_ZONE_INTERACT;
import static io.github.aquerr.eaglefactions.PluginPermissions.WAR_ZONE_BUILD;
import static io.github.aquerr.eaglefactions.PluginPermissions.WAR_ZONE_INTERACT;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.forbiddenSafeZone;
import static io.github.aquerr.eaglefactions.api.managers.ProtectionResult.forbiddenWarZone;
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
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("BlockInteract:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + location.blockPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(location.block().toString())));
            }
        }

        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ProtectionResult.ok();

        if (this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        final boolean isBlockCarrierAtLocation = location.blockEntity().isPresent() && location.blockEntity().get() instanceof BlockCarrier;

        if (safeZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForInteraction(location.blockType().toString(), FactionType.SAFE_ZONE))
                return ProtectionResult.builder().hasAccess(true).isSafeZone(true).build();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return ProtectionResult.builder().hasAccess(true).isSafeZone(true).build();
            else return forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForInteraction(location.blockType().toString(), FactionType.WAR_ZONE))
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
                return ProtectionResult.ok();
            else
            {
                if (isBlockWhitelistedForInteraction(location.blockType().toString(), FactionType.WILDERNESS))
                    return ProtectionResult.ok();
                return ProtectionResult.forbidden();
            }
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isBlockWhitelistedForInteraction(location.blockType().toString(), FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (chunkFaction.isWarZone())
        {
            if (isBlockWhitelistedForInteraction(location.blockType().toString(), FactionType.WAR_ZONE))
                return okWarZone();
            if(user.hasPermission(WAR_ZONE_INTERACT))
                return okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return ProtectionResult.okEagleFeather();
            return forbiddenWarZone();
        }

        if (isBlockWhitelistedForInteraction(location.blockType().toString(), FactionType.FACTION))
            return ProtectionResult.ok();

        //If player is not in a faction but there is a faction at chunk
        if(!optionalServerPlayerFaction.isPresent())
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return ProtectionResult.okEagleFeather();
            return ProtectionResult.forbidden();
        }

        final Faction playerFaction = optionalServerPlayerFaction.get();
        if (this.permsManager.canInteract(user.uniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition()).get()))
            return ProtectionResult.okFactionPerm();
        else
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return ProtectionResult.okEagleFeather();
            return ProtectionResult.forbidden();
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
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("ItemUsage:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + location.blockPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(location.block().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Used item: ", GOLD)).append(text(usedItem.type().toString())));
            }
        }

        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ProtectionResult.ok();

        if (this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        final boolean isBlockCarrierAtLocation = location.blockEntity().isPresent() && location.blockEntity().get() instanceof BlockCarrier;

        if (safeZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isItemWhitelisted(usedItem.type().toString(), FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isItemWhitelisted(usedItem.type().toString(), FactionType.WAR_ZONE))
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
                return ProtectionResult.ok();
            else
            {
                if (isItemWhitelisted(usedItem.type().toString(), FactionType.WILDERNESS))
                    return ProtectionResult.ok();
                return ProtectionResult.forbidden();
            }
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isItemWhitelisted(usedItem.type().toString(), FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_INTERACT))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (chunkFaction.isWarZone())
        {
            if (isItemWhitelisted(usedItem.type().toString(), FactionType.WAR_ZONE))
                return okWarZone();
            if (user.hasPermission(WAR_ZONE_INTERACT))
                return okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return ProtectionResult.okEagleFeather();
            else return forbiddenWarZone();
        }

        if (isItemWhitelisted(usedItem.type().toString(), FactionType.FACTION))
            return ProtectionResult.ok();

        //If player is not in a faction but there is a faction at chunk
        if(!optionalServerPlayerFaction.isPresent())
        {
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return ProtectionResult.okEagleFeather();
            return ProtectionResult.forbidden();
        }

        Faction playerFaction = optionalServerPlayerFaction.get();
        if(this.permsManager.canInteract(user.uniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition()).get()))
            return ProtectionResult.okFactionPerm();
        else
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
                return ProtectionResult.okEagleFeather();
            else return ProtectionResult.forbidden();
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
        final ServerLocation location = blockSnapshot.location().orElse(null);
        if (location == null)
        {
            EagleFactionsPlugin.getPlugin().printInfo("Broken BlockSnapshot does not contain a location. This is not normal.");
            return ProtectionResult.forbidden();
        }

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("BlockBreak:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + blockSnapshot.position().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(blockSnapshot.state().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block id: ", GOLD)).append(text(blockSnapshot.state().toString())));
            }
        }

        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ProtectionResult.ok();

        if(this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.state().type().toString(), FactionType.SAFE_ZONE))
                return okSafeZone();
            if (user.hasPermission(SAFE_ZONE_BUILD))
                return okSafeZone();
            else return forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.state().type().toString(), FactionType.WAR_ZONE))
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
                    if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.state().type().toString(), FactionType.SAFE_ZONE))
                        return okSafeZone();
                    if (user.hasPermission(SAFE_ZONE_BUILD))
                        return okSafeZone();
                    else return forbiddenSafeZone();
                }
                else //WarZone
                {
                    if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.state().type().toString(), FactionType.WAR_ZONE))
                        return okWarZone();
                    if (user.hasPermission(WAR_ZONE_BUILD))
                        return okWarZone();
                    else return forbiddenWarZone();
                }
            }

            if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.state().type().toString(), FactionType.FACTION))
                return ProtectionResult.ok();

            final Faction chunkFaction = optionalChunkFaction.get();
            final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition());

            if (optionalServerPlayerFaction.filter(faction -> this.permsManager.canBreakBlock(user.uniqueId(), faction, optionalChunkFaction.get(), optionalClaim.get())).isPresent())
                return ProtectionResult.okFactionPerm();
            else return ProtectionResult.forbidden();
        }
        else
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ProtectionResult.ok();
            else
            {
                if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.state().type().toString(), FactionType.WILDERNESS))
                    return ProtectionResult.ok();
                return ProtectionResult.forbidden();
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
            return ProtectionResult.forbidden();
        }

        final ServerWorld world = location.world();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ProtectionResult.ok();

        if(this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(world)))
        {
            if (isBlockWhitelistedForPlaceDestroy(location.block().toString(), FactionType.SAFE_ZONE))
                return okSafeZone();
            else return forbiddenSafeZone();
        }

        boolean shouldProtectWarZoneFromMobGrief = !this.factionLogic.getFactionByName(EagleFactionsPlugin.WAR_ZONE_NAME).getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF);
        if(this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(world)) && shouldProtectWarZoneFromMobGrief)
        {
            //Not sure if we should use white-list for mobs...
            if (isBlockWhitelistedForPlaceDestroy(location.block().toString(), FactionType.WAR_ZONE))
                return okWarZone();
            else return forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition());
        if(!optionalChunkFaction.isPresent())
            return ProtectionResult.ok();

        if(optionalChunkFaction.get().isSafeZone())
        {
            if(isBlockWhitelistedForPlaceDestroy(location.block().toString(), FactionType.SAFE_ZONE))
                return okSafeZone();
            if (optionalChunkFaction.get().getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                return okSafeZone();
            else return forbiddenSafeZone();
        }

        if(optionalChunkFaction.get().isWarZone() && shouldProtectWarZoneFromMobGrief)
        {
            if (isBlockWhitelistedForPlaceDestroy(location.block().toString(), FactionType.WAR_ZONE))
                return okWarZone();
            else return forbiddenWarZone();
        }

        if(!optionalChunkFaction.get().getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
        {
            if (isBlockWhitelistedForPlaceDestroy(location.block().toString(), FactionType.FACTION))
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
        }
        return ProtectionResult.ok();
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
            return ProtectionResult.forbidden();
        }

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            if(user instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("BlockPlace:", GOLD)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Location: ", GOLD)).append(text(getPlainWorldName(location.world()) + " " + blockSnapshot.position().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(user.name())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(blockSnapshot.state().type().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Item in hand: ", GOLD)).append(text(ofNullable(user.itemInHand(HandTypes.MAIN_HAND))
                        .map(ItemStack::type)
                        .map(Object::toString)
                        .orElse(""))));
            }
        }

        ServerWorld world = location.world();
        final String itemId = user.itemInHand(HandTypes.MAIN_HAND) != null ? user.itemInHand(HandTypes.MAIN_HAND).type().toString() : "";

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ProtectionResult.ok();

        if(this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

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

            if (isBlockWhitelistedForPlaceDestroy(location.block().toString(), FactionType.FACTION))
                return ProtectionResult.ok();

            final Faction chunkFaction = optionalChunkFaction.get();
            if (optionalServerPlayerFaction.filter(faction -> this.permsManager.canPlaceBlock(user.uniqueId(), faction, chunkFaction, chunkFaction.getClaimAt(world.uniqueId(), location.chunkPosition()).get())).isPresent())
                return ProtectionResult.okFactionPerm();
            else return ProtectionResult.forbidden();
        }
        else
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ProtectionResult.ok();
            else
            {
                if (isBlockWhitelistedForPlaceDestroy(location.block().toString(), FactionType.WILDERNESS))
                    return ProtectionResult.ok();
                return ProtectionResult.forbidden();
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
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Block at location: ", GOLD)).append(text(location.block().toString())));
            }
        }

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(world)))
            return ProtectionResult.ok();

        //Check if admin
        if(this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

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
                return ProtectionResult.ok();
            return ProtectionResult.forbidden();
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
                    return ProtectionResult.okFactionPerm();
                else return ProtectionResult.forbidden();
            }
        }

        boolean allowExplosionsByOtherServerPlayersInClaims = this.protectionConfig.shouldAllowExplosionsByOtherPlayersInClaims();
        if (allowExplosionsByOtherServerPlayersInClaims)
            return ProtectionResult.ok();
        else return ProtectionResult.forbidden();
    }

    @Override
    public ProtectionResult canExplode(final ServerLocation location)
    {
        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(location.world())))
            return ProtectionResult.ok();

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
            return ProtectionResult.ok();

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
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
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
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Entity at location: ", GOLD)).append(text(attackedEntity.type().toString())));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("User: ", GOLD)).append(text(player.name())));
        }


        if (this.playerManager.hasAdminMode(player.user()))
            return ProtectionResult.okAdmin();

        final boolean isServerPlayer = attackedEntity instanceof ServerPlayer;
        final boolean isMob = !isServerPlayer && (attackedEntity instanceof Living && !(attackedEntity instanceof ArmorStand));
        if (isMob)
            return ProtectionResult.ok();

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
                return ProtectionResult.ok();
            if (optionalSourceChunkFaction.isPresent() && optionalSourceChunkFaction.get().isSafeZone())
                return forbiddenSafeZone();
            if (!optionalAttackerServerPlayerFaction.isPresent())
                return ProtectionResult.ok();
            if (!optionalAttackedServerPlayerFaction.isPresent())
                return ProtectionResult.ok();
            final Faction attackedFaction = optionalAttackedServerPlayerFaction.get();
            final Faction attackerFaction = optionalAttackerServerPlayerFaction.get();
            if (!attackerFaction.getName().equals(attackedFaction.getName()))
            {
                if (attackerFaction.isAlly(attackedFaction) && !this.factionsConfig.isAllianceFriendlyFire())
                    return ProtectionResult.forbidden();
                else if (attackerFaction.isTruce(attackedFaction) && !this.factionsConfig.isTruceFriendlyFire())
                    return ProtectionResult.forbidden();
            }
            else
            {
                if (this.factionsConfig.isFactionFriendlyFire())
                    return ProtectionResult.ok();
                else return ProtectionResult.forbidden();
            }
        }
        else //Item Frame, Minecraft, Painting etc.
        {
            //Not claimable worlds should be always ignored by protection system.
            if (notClaimableWorld)
                return ProtectionResult.ok();
            if (isSafeZoneWorld)
                return forbiddenSafeZone();
            if (isWarZoneWorld)
            {
                if (!this.protectionConfig.shouldProtectWarzoneFromPlayers())
                    return okWarZone();
                else return forbiddenWarZone();
            }
            if (!optionalChunkFaction.isPresent())
                return ProtectionResult.ok();
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
                return ProtectionResult.forbidden();
            final Faction attackerFaction = optionalAttackerServerPlayerFaction.get();
            if (this.permsManager.canBreakBlock(player.uniqueId(), attackerFaction, chunkFaction, chunkFaction.getClaimAt(entityLocation.world().uniqueId(), entityLocation.chunkPosition()).get()))
                return ProtectionResult.okFactionPerm();
            return ProtectionResult.forbidden();
        }
        return ProtectionResult.ok();
    }

    @Override
    public ProtectionResult canNotifyBlock(final ServerLocation notifier, final ServerLocation notifiedLocation)
    {
        //First, let's check the world.
        //TODO: Maybe we should check notifier's world as well?
        final boolean isSafeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(notifiedLocation.world()));
        final boolean isWarZoneWorld = !isSafeZoneWorld && this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(notifiedLocation.world()));
        final boolean notClaimableWorld = !isSafeZoneWorld && !isWarZoneWorld && this.protectionConfig.getNotClaimableWorldNames().contains(getPlainWorldName(notifiedLocation.world()));

        //Entire world is one claim type thus we should allow the notification.
        if (isSafeZoneWorld || isWarZoneWorld || notClaimableWorld)
            return ProtectionResult.ok();

        final Optional<Faction> notifierFaction = this.factionLogic.getFactionByChunk(notifier.world().uniqueId(), notifier.chunkPosition());
        final Optional<Faction> notifiedFaction = this.factionLogic.getFactionByChunk(notifiedLocation.world().uniqueId(), notifiedLocation.chunkPosition());

        // Factions can notify wilderness but wilderness cannot notify factions.
        // Wilderness can only notify other factions if mob-gref is set to true.

        //Source is wilderness.
        if (!notifierFaction.isPresent())
        {
            //Both wilderness
            if (!notifiedFaction.isPresent())
                return ProtectionResult.ok();
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
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.block().toString(), FactionType.FACTION))
                    return ProtectionResult.ok();
                if(faction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF)) //Notified Regular faction
                    return ProtectionResult.ok();
                else return ProtectionResult.forbidden();
            }

        }

        final Faction sourceFaction = notifierFaction.get();

        //Regular factions can notify locations in wilderness.
        if (!notifiedFaction.isPresent())
            return ProtectionResult.ok();

        final Faction targetFaction = notifiedFaction.get();

        //Check if factions are equal.
        if(targetFaction.equals(sourceFaction))
            return ProtectionResult.ok();

        if (sourceFaction.isSafeZone())
        {
            if(targetFaction.isWarZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.block().toString(), FactionType.WAR_ZONE))
                    return okWarZone();
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return okWarZone();
                else return forbiddenWarZone();
            }
            else
            {
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return ProtectionResult.ok();
                else return ProtectionResult.forbidden();
            }
        }
        else if (sourceFaction.isWarZone())
        {
             if(targetFaction.isSafeZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.block().toString(), FactionType.SAFE_ZONE))
                    return okSafeZone();
                return forbiddenSafeZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.block().toString(), FactionType.FACTION))
                    return ProtectionResult.ok();
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return ProtectionResult.ok();
                else return ProtectionResult.forbidden();
            }
        }
        else
        {
            if(targetFaction.isSafeZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.block().toString(), FactionType.SAFE_ZONE))
                    return okSafeZone();
                return forbiddenSafeZone();
            }
            else if(targetFaction.isWarZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.block().toString(), FactionType.WAR_ZONE))
                    return okWarZone();
                if(targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return okWarZone();
                else return forbiddenWarZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.block().toString(), FactionType.FACTION))
                    return ProtectionResult.ok();
                if (targetFaction.getProtectionFlagValue(ProtectionFlagType.MOB_GRIEF))
                    return ProtectionResult.ok();
                else return ProtectionResult.forbidden();
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
