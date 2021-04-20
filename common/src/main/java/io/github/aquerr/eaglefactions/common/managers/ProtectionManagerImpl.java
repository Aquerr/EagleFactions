package io.github.aquerr.eaglefactions.common.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.EagleFeather;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ProtectionManagerImpl implements ProtectionManager
{
    private static final String ITEM_IT_AND_FACTION_TYPE_MUST_BE_PROVIDED = "Item id and faction type must be provided";
    private final FactionLogic factionLogic;
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final ProtectionConfig protectionConfig;
    private final ChatConfig chatConfig;
    private final FactionsConfig factionsConfig;

    public ProtectionManagerImpl(final FactionLogic factionLogic, final PermsManager permsManager, final PlayerManager playerManager, final ProtectionConfig protectionConfig, final ChatConfig chatConfig, final FactionsConfig factionsConfig)
    {
        this.protectionConfig = protectionConfig;
        this.chatConfig = chatConfig;
        this.factionsConfig = factionsConfig;
        this.factionLogic = factionLogic;
        this.permsManager = permsManager;
        this.playerManager = playerManager;
    }

    @Override
    public ProtectionResult canInteractWithBlock(final Location<World> location, final User user, final boolean shouldNotify)
    {
        final ProtectionResult canInteract = canInteractWithBlock(location, user);
        if (shouldNotify && !canInteract.hasAccess())
            notifyPlayer(user);
        return canInteract;
    }

    private ProtectionResult canInteractWithBlock(final Location<World> location, final User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "BlockInteract:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Location: ", TextColors.RESET, location.getExtent().getName() + " " + location.getBlockPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "User: ", TextColors.RESET, user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block at location: ", TextColors.RESET, location.getBlockType().getName())));
            }
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return ProtectionResult.ok();

        if (this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        final boolean isBlockCarrierAtLocation = location.getTileEntity().isPresent() && location.getTileEntity().get() instanceof BlockCarrier;

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                return ProtectionResult.builder().hasAccess(true).isSafeZone(true).build();
            if (user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return ProtectionResult.builder().hasAccess(true).isSafeZone(true).build();
            else return ProtectionResult.forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            if (user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return ProtectionResult.okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                return ProtectionResult.okWarZone();
            }
            return ProtectionResult.forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
        {
            if(!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                return ProtectionResult.okSafeZone();
            if (user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return ProtectionResult.okSafeZone();
            else return ProtectionResult.forbiddenSafeZone();
        }
        if (chunkFaction.isWarZone())
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            if(user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return ProtectionResult.okWarZone();
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                return ProtectionResult.okEagleFeather();
            }
            return ProtectionResult.forbiddenWarZone();
        }

        if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.FACTION))
            return ProtectionResult.ok();

        //If player is not in a faction but there is a faction at chunk
        if(!optionalPlayerFaction.isPresent())
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                return ProtectionResult.okEagleFeather();
            }
            return ProtectionResult.forbidden();
        }

        final Faction playerFaction = optionalPlayerFaction.get();
        if (this.permsManager.canInteract(user.getUniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.getUniqueId(), location.getChunkPosition()).get()))
            return ProtectionResult.okFactionPerm();
        else
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                return ProtectionResult.okEagleFeather();
            }
            return ProtectionResult.forbidden();
        }
    }

    @Override
    public ProtectionResult canUseItem(final Location<World> location, final User user, final ItemStackSnapshot usedItem, final boolean shouldNotify)
    {
        final ProtectionResult canUseItem = canUseItem(location, user, usedItem);
        if (shouldNotify && !canUseItem.hasAccess())
            notifyPlayer(user);
        return canUseItem;
    }

    private ProtectionResult canUseItem(final Location<World> location, final User user, final ItemStackSnapshot usedItem)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "ItemUsage:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Location: ", TextColors.RESET, location.getExtent().getName() + " " + location.getBlockPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "User: ", TextColors.RESET, user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block at location: ", TextColors.RESET, location.getBlockType().getId())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Used item: ", TextColors.RESET, usedItem.getType().getId())));
            }
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return ProtectionResult.ok();

        if (this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.SAFE_ZONE))
                return ProtectionResult.okSafeZone();
            if (user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return ProtectionResult.okSafeZone();
            else return ProtectionResult.forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            if (user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return ProtectionResult.okWarZone();
            else return ProtectionResult.forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.SAFE_ZONE))
                return ProtectionResult.okSafeZone();
            if (user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return ProtectionResult.okSafeZone();
            else return ProtectionResult.forbiddenSafeZone();
        }
        if (chunkFaction.isWarZone())
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            if (user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return ProtectionResult.okWarZone();
            else return ProtectionResult.forbiddenWarZone();
        }

        if (isItemWhitelisted(usedItem.getType().getId(), FactionType.FACTION))
            return ProtectionResult.ok();

        //If player is not in a faction but there is a faction at chunk
        if(!optionalPlayerFaction.isPresent())
            return ProtectionResult.forbidden();

        Faction playerFaction = optionalPlayerFaction.get();
        if(this.permsManager.canInteract(user.getUniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.getUniqueId(), location.getChunkPosition()).get()))
            return ProtectionResult.okFactionPerm();
        else return ProtectionResult.forbidden();
    }

    @Override
    public ProtectionResult canBreak(final BlockSnapshot blockSnapshot, User user, boolean shouldNotify)
    {
        final ProtectionResult canBreak = canBreak(blockSnapshot, user);
        if (shouldNotify && !canBreak.hasAccess())
            notifyPlayer(user);
        return canBreak;
    }

    private ProtectionResult canBreak(final BlockSnapshot blockSnapshot, final User user)
    {
        final Location<World> location = blockSnapshot.getLocation().orElse(null);
        if (location == null)
        {
            EagleFactionsPlugin.getPlugin().printInfo("Broken BlockSnapshot does not contain a location. This is not normal.");
            return ProtectionResult.forbidden();
        }

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "BlockBreak:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Location: ", TextColors.RESET, location.getExtent().getName() + " " + blockSnapshot.getPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "User: ", TextColors.RESET, user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block at location: ", TextColors.RESET, blockSnapshot.getState().getType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block id: ", TextColors.RESET, blockSnapshot.getState().getType().getId())));
            }
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return ProtectionResult.ok();

        if(this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.getState().getType().getId(), FactionType.SAFE_ZONE))
                return ProtectionResult.okSafeZone();
            if (user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                return ProtectionResult.okSafeZone();
            else return ProtectionResult.forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.getState().getType().getId(), FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            if (user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                return ProtectionResult.okWarZone();
            else return ProtectionResult.forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            {
                if(optionalChunkFaction.get().isSafeZone())
                {
                    if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.getState().getType().getId(), FactionType.SAFE_ZONE))
                        return ProtectionResult.okSafeZone();
                    if (user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                        return ProtectionResult.okSafeZone();
                    else return ProtectionResult.forbiddenSafeZone();
                }
                else //WarZone
                {
                    if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.getState().getType().getId(), FactionType.WAR_ZONE))
                        return ProtectionResult.okWarZone();
                    if (user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                        return ProtectionResult.okWarZone();
                    else return ProtectionResult.forbiddenWarZone();
                }
            }

            if (isBlockWhitelistedForPlaceDestroy(blockSnapshot.getState().getType().getId(), FactionType.FACTION))
                return ProtectionResult.ok();

            final Faction chunkFaction = optionalChunkFaction.get();
            final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(world.getUniqueId(), location.getChunkPosition());

            if (optionalPlayerFaction.filter(faction -> this.permsManager.canBreakBlock(user.getUniqueId(), faction, optionalChunkFaction.get(), optionalClaim.get())).isPresent())
                return ProtectionResult.okFactionPerm();
            else return ProtectionResult.forbidden();
        }
        else
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
        }
    }

    @Override
    public ProtectionResult canBreak(final BlockSnapshot blockSnapshot)
    {
        final Location<World> location = blockSnapshot.getLocation().orElse(null);
        if (location == null)
        {
            EagleFactionsPlugin.getPlugin().printInfo("Broken BlockSnapshot does not contain a location. This is not normal.");
            return ProtectionResult.forbidden();
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return ProtectionResult.ok();

        if(this.protectionConfig.getSafeZoneWorldNames().contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                return ProtectionResult.okSafeZone();
            else return ProtectionResult.forbiddenSafeZone();
        }

        if(this.protectionConfig.getWarZoneWorldNames().contains(world.getName()) && this.protectionConfig.shouldProtectWarZoneFromMobGrief())
        {
            //Not sure if we should use white-list for mobs...
            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            else return ProtectionResult.forbiddenWarZone();
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(!optionalChunkFaction.isPresent())
            return ProtectionResult.ok();

        if(optionalChunkFaction.get().isSafeZone())
        {
            if(isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                return ProtectionResult.okSafeZone();
            else return ProtectionResult.forbiddenSafeZone();
        }

        if(optionalChunkFaction.get().isWarZone() && this.protectionConfig.shouldProtectWarZoneFromMobGrief())
        {
            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            else return ProtectionResult.forbiddenWarZone();
        }

        if(this.protectionConfig.shouldProtectClaimFromMobGrief())
        {
            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.FACTION))
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
            notifyPlayer(player);
        return canPlace;
    }

    private ProtectionResult canPlace(final BlockSnapshot blockSnapshot, final User user)
    {
        final Location<World> location = blockSnapshot.getLocation().orElse(null);
        if (location == null)
        {
            EagleFactionsPlugin.getPlugin().printInfo("Placed BlockSnapshot does not contain a location. This is not normal.");
            return ProtectionResult.forbidden();
        }

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "BlockPlace:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Location: ", TextColors.RESET, location.getExtent().getName() + " " + blockSnapshot.getPosition().toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "User: ", TextColors.RESET, user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block at location: ", TextColors.RESET, blockSnapshot.getState().getType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Item in hand: ", TextColors.RESET, (user.getItemInHand(HandTypes.MAIN_HAND).isPresent() ? user.getItemInHand(HandTypes.MAIN_HAND).get().getType().getName() : ""))));
            }
        }

        World world = location.getExtent();
        final String itemId = user.getItemInHand(HandTypes.MAIN_HAND).isPresent() ? user.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId() : "";

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return ProtectionResult.ok();

        if(this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.SAFE_ZONE))
                return ProtectionResult.okSafeZone();
            if (user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                return ProtectionResult.okSafeZone();
            else return ProtectionResult.forbiddenSafeZone();
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.WAR_ZONE))
                return ProtectionResult.okWarZone();
            if (user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                return ProtectionResult.okWarZone();
            else return ProtectionResult.forbiddenWarZone();
        }

        Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            {
                if(optionalChunkFaction.get().isSafeZone())
                {
                    if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.SAFE_ZONE))
                        return ProtectionResult.okSafeZone();
                    if (user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                        return ProtectionResult.okSafeZone();
                    else return ProtectionResult.forbiddenSafeZone();
                }
                else //WarZone
                {
                    if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.WAR_ZONE))
                        return ProtectionResult.okWarZone();
                    if (user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                        return ProtectionResult.okWarZone();
                    else return ProtectionResult.forbiddenWarZone();
                }
            }

            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.FACTION))
                return ProtectionResult.ok();

            final Faction chunkFaction = optionalChunkFaction.get();
            if (optionalPlayerFaction.filter(faction -> this.permsManager.canPlaceBlock(user.getUniqueId(), faction, chunkFaction, chunkFaction.getClaimAt(world.getUniqueId(), location.getChunkPosition()).get())).isPresent())
                return ProtectionResult.okFactionPerm();
            else return ProtectionResult.forbidden();
        }
        else
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
        }
    }

    @Override
    public ProtectionResult canExplode(final Location<World> location, final User user, final boolean shouldNotify)
    {
        final ProtectionResult canExplode = canExplode(location, user);
        if (shouldNotify && !canExplode.hasAccess())
            notifyPlayer(user);
        return canExplode;
    }

    private ProtectionResult canExplode(final Location<World> location, final User user)
    {
        final World world = location.getExtent();

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                final Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Explosion:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Location: ", TextColors.RESET, location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "User: ", TextColors.RESET, user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block at location: ", TextColors.RESET, location.getBlockType().getName())));
            }
        }

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return ProtectionResult.ok();

        boolean shouldProtectWarZoneFromPlayers = this.protectionConfig.shouldProtectWarzoneFromPlayers();
        boolean allowExplosionsByOtherPlayersInClaims = this.protectionConfig.shouldAllowExplosionsByOtherPlayersInClaims();

        //Check if admin
        if(this.playerManager.hasAdminMode(user))
            return ProtectionResult.okAdmin();

        //Check world
        if (this.protectionConfig.getSafeZoneWorldNames().contains(world.getName()))
            return ProtectionResult.forbiddenSafeZone();
        else if (this.protectionConfig.getWarZoneWorldNames().contains(world.getName()))
        {
            if (!shouldProtectWarZoneFromPlayers)
                return ProtectionResult.okWarZone();
            return ProtectionResult.forbiddenWarZone();
        }

        //If no faction
        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if (!optionalChunkFaction.isPresent())
        {
            if (!this.protectionConfig.shouldProtectWildernessFromPlayers())
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
        }

        //If SafeZone or WarZone
        final Faction chunkFaction = optionalChunkFaction.get();

        if(chunkFaction.isSafeZone() || chunkFaction.isWarZone())
        {
            if(chunkFaction.isSafeZone())
            {
                if (user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                    return ProtectionResult.okSafeZone();
                else return ProtectionResult.forbiddenSafeZone();
            }
            else
            {
                if (chunkFaction.isWarZone() && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                    return ProtectionResult.okWarZone();
                else return ProtectionResult.forbiddenWarZone();
            }
        }

        //If player is in faction
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if(optionalPlayerFaction.isPresent())
        {
            final Faction playerFaction = optionalPlayerFaction.get();
            if (chunkFaction.getName().equalsIgnoreCase(playerFaction.getName()))
            {
                if (this.permsManager.canPlaceBlock(user.getUniqueId(), playerFaction, chunkFaction, chunkFaction.getClaimAt(world.getUniqueId(), location.getChunkPosition()).get()))
                    return ProtectionResult.okFactionPerm();
                else return ProtectionResult.forbidden();
            }
        }

        if (allowExplosionsByOtherPlayersInClaims)
            return ProtectionResult.ok();
        else return ProtectionResult.forbidden();
    }

    @Override
    public ProtectionResult canExplode(final Location<World> location)
    {
        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(location.getExtent().getName()))
            return ProtectionResult.ok();

        boolean shouldProtectWarZoneFromMobGrief = this.protectionConfig.shouldProtectWarZoneFromMobGrief();
        boolean shouldProtectClaimsFromMobGrief = this.protectionConfig.shouldProtectClaimFromMobGrief();

        //Check world
        if (this.protectionConfig.getSafeZoneWorldNames().contains(location.getExtent().getName()))
            return ProtectionResult.forbiddenSafeZone();

        if (this.protectionConfig.getWarZoneWorldNames().contains(location.getExtent().getName()))
        {
            if (!shouldProtectWarZoneFromMobGrief)
                return ProtectionResult.okWarZone();
            else return ProtectionResult.forbiddenWarZone();
        }

        Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
        if (!optionalChunkFaction.isPresent())
            return ProtectionResult.ok();

        Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
            return ProtectionResult.forbiddenSafeZone();
        else if (chunkFaction.isWarZone() && shouldProtectWarZoneFromMobGrief)
            return ProtectionResult.forbiddenWarZone();
        else
        {
            if (!shouldProtectClaimsFromMobGrief)
                return ProtectionResult.ok();
            else return ProtectionResult.forbidden();
        }
    }

    @Override
    public ProtectionResult canHitEntity(final Entity attackedEntity, final Player player, final boolean shouldNotify)
    {
        final ProtectionResult canAttack = canAttackEntity(attackedEntity, player);
        if (shouldNotify && !canAttack.hasAccess())
            notifyPlayer(player);
        return canAttack;
    }

    private ProtectionResult canAttackEntity(final Entity attackedEntity, final Player player)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "EntityAttack:")));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Location: ", TextColors.RESET, attackedEntity.getLocation().getExtent().getName() + " " + attackedEntity.getLocation().getBlockPosition().toString())));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Entity at location: ", TextColors.RESET, attackedEntity.getType().getId())));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "User: ", TextColors.RESET, player.getName())));
        }


        if (this.playerManager.hasAdminMode(player))
            return ProtectionResult.okAdmin();

        final boolean isPlayer = attackedEntity instanceof Player;
        final boolean isMob = !isPlayer && (attackedEntity instanceof Living && !(attackedEntity instanceof ArmorStand));
        if (isMob)
            return ProtectionResult.ok();

        final Location<World> entityLocation = attackedEntity.getLocation();
        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(entityLocation.getExtent().getUniqueId(), entityLocation.getChunkPosition());
        final Optional<Faction> optionalAttackerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(player.getUniqueId());
        final Optional<Faction> optionalSourceChunkFaction = this.factionLogic.getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        final boolean isSafeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(entityLocation.getExtent().getName());
        final boolean isWarZoneWorld = !isSafeZoneWorld && this.protectionConfig.getWarZoneWorldNames().contains(entityLocation.getExtent().getName());
        final boolean notClaimableWorld = !isSafeZoneWorld && !isWarZoneWorld && this.protectionConfig.getNotClaimableWorldNames().contains(entityLocation.getExtent().getName());

        if (isPlayer)
        {
            final Player attackedPlayer = (Player) attackedEntity;
            final Optional<Faction> optionalAttackedPlayerFaction = this.factionLogic
                    .getFactionByPlayerUUID(attackedPlayer.getUniqueId());
            if (isSafeZoneWorld)
                return ProtectionResult.forbiddenSafeZone();
            if (optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
                return ProtectionResult.forbiddenSafeZone();
            if (attackedEntity.equals(player))
                return ProtectionResult.ok();
            if (optionalSourceChunkFaction.isPresent() && optionalSourceChunkFaction.get().isSafeZone())
                return ProtectionResult.forbiddenSafeZone();
            if (!optionalAttackerPlayerFaction.isPresent())
                return ProtectionResult.ok();
            if (!optionalAttackedPlayerFaction.isPresent())
                return ProtectionResult.ok();
            final Faction attackedFaction = optionalAttackedPlayerFaction.get();
            final Faction attackerFaction = optionalAttackerPlayerFaction.get();
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
                return ProtectionResult.forbiddenSafeZone();
            if (isWarZoneWorld)
            {
                if (!this.protectionConfig.shouldProtectWarzoneFromPlayers())
                    return ProtectionResult.okWarZone();
                else return ProtectionResult.forbiddenWarZone();
            }
            if (!optionalChunkFaction.isPresent())
                return ProtectionResult.ok();
            final Faction chunkFaction = optionalChunkFaction.get();
            if (chunkFaction.isSafeZone())
                return ProtectionResult.forbiddenSafeZone();
            else if (chunkFaction.isWarZone())
            {
                if(!this.protectionConfig.shouldProtectWarzoneFromPlayers())
                    return ProtectionResult.okWarZone();
                else return ProtectionResult.forbiddenWarZone();
            }
            if (!optionalAttackerPlayerFaction.isPresent())
                return ProtectionResult.forbidden();
            final Faction attackerFaction = optionalAttackerPlayerFaction.get();
            if (this.permsManager.canBreakBlock(player.getUniqueId(), attackerFaction, chunkFaction, chunkFaction.getClaimAt(entityLocation.getExtent().getUniqueId(), entityLocation.getChunkPosition()).get()))
                return ProtectionResult.okFactionPerm();
            return ProtectionResult.forbidden();
        }
        return ProtectionResult.ok();
    }

    @Override
    public ProtectionResult canNotifyBlock(final Location<World> notifier, final Location<World> notifiedLocation)
    {
        //First, let's check the world.
        //TODO: Maybe we should check notifier's world as well?
        final boolean isSafeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(notifiedLocation.getExtent().getName());
        final boolean isWarZoneWorld = !isSafeZoneWorld && this.protectionConfig.getWarZoneWorldNames().contains(notifiedLocation.getExtent().getName());
        final boolean notClaimableWorld = !isSafeZoneWorld && !isWarZoneWorld && this.protectionConfig.getNotClaimableWorldNames().contains(notifiedLocation.getExtent().getName());

        //Entire world is one claim type thus we should allow the notification.
        if (isSafeZoneWorld || isWarZoneWorld || notClaimableWorld)
            return ProtectionResult.ok();

        final Optional<Faction> notifierFaction = this.factionLogic.getFactionByChunk(notifier.getExtent().getUniqueId(), notifier.getChunkPosition());
        final Optional<Faction> notifiedFaction = this.factionLogic.getFactionByChunk(notifiedLocation.getExtent().getUniqueId(), notifiedLocation.getChunkPosition());

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
                return ProtectionResult.forbiddenSafeZone();
            else if(faction.isWarZone()) //Notified WarZone
            {
                if (!this.protectionConfig.shouldProtectWarZoneFromMobGrief())
                    return ProtectionResult.okWarZone();
                else return ProtectionResult.forbiddenWarZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.getBlock().getId(), FactionType.FACTION))
                    return ProtectionResult.ok();
                if(!this.protectionConfig.shouldProtectClaimFromMobGrief()) //Notified Regular faction
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
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.getBlock().getId(), FactionType.WAR_ZONE))
                    return ProtectionResult.okWarZone();
                if (!this.protectionConfig.shouldProtectWarZoneFromMobGrief())
                    return ProtectionResult.okWarZone();
                else return ProtectionResult.forbiddenWarZone();
            }
            else
            {
                if (!this.protectionConfig.shouldProtectClaimFromMobGrief())
                    return ProtectionResult.ok();
                else return ProtectionResult.forbidden();
            }
        }
        else if (sourceFaction.isWarZone())
        {
             if(targetFaction.isSafeZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.getBlock().getId(), FactionType.SAFE_ZONE))
                    return ProtectionResult.okSafeZone();
                return ProtectionResult.forbiddenSafeZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.getBlock().getId(), FactionType.FACTION))
                    return ProtectionResult.ok();
                if (!this.protectionConfig.shouldProtectClaimFromMobGrief())
                    return ProtectionResult.ok();
                else return ProtectionResult.forbidden();
            }
        }
        else
        {
            if(targetFaction.isSafeZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.getBlock().getId(), FactionType.SAFE_ZONE))
                    return ProtectionResult.okSafeZone();
                return ProtectionResult.forbiddenSafeZone();
            }
            else if(targetFaction.isWarZone())
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.getBlock().getId(), FactionType.WAR_ZONE))
                    return ProtectionResult.okWarZone();
                if(!this.protectionConfig.shouldProtectWarZoneFromMobGrief())
                    return ProtectionResult.okWarZone();
                else return ProtectionResult.forbiddenWarZone();
            }
            else
            {
                if(this.isBlockWhitelistedForPlaceDestroy(notifiedLocation.getBlock().getId(), FactionType.FACTION))
                    return ProtectionResult.ok();
                if (!this.protectionConfig.shouldProtectClaimFromMobGrief())
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
            default:
                return false;
        }
    }

    private boolean isHoldingEagleFeather(final User user)
    {
        return user.getItemInHand(HandTypes.MAIN_HAND)
                .filter(itemStack -> ItemTypes.FEATHER == itemStack.getType())
                .filter(itemStack -> itemStack.get(Keys.DISPLAY_NAME).isPresent())
                .flatMap(itemStack -> itemStack.get(Keys.DISPLAY_NAME))
                .map(EagleFeather.getDisplayName()::equals)
                .orElse(false);
    }

    private void notifyPlayer(final User user)
    {
        if (this.chatConfig.shouldDisplayProtectionSystemMessages())
        {
            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
        }
    }
}
