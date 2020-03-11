package io.github.aquerr.eaglefactions.common.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.EagleFeather;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class ProtectionManagerImpl implements ProtectionManager
{
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
    public boolean canInteractWithBlock(final Location<World> location, final User user, final boolean shouldNotify)
    {
        final boolean canInteract = canInteractWithBlock(location, user);
        if (shouldNotify && !canInteract)
            notifyPlayer(user);
        return canInteract;
    }

    private boolean canInteractWithBlock(final Location<World> location, final User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Interact With Block:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
            }
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return true;

        if (this.playerManager.hasAdminMode(user))
            return true;

        if (isBlockWhitelistedForInteraction(location.getBlockType().getId()))
            return true;

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        final boolean isBlockCarrierAtLocation = location.getTileEntity().isPresent() && location.getTileEntity().get() instanceof BlockCarrier;

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return true;

            if (warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return true;

            //Warzone, tileentity and eaglefeather
            if(warZoneWorlds.contains(world.getName()) && isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }
            return false;
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
        {
            if(this.protectionConfig.shouldProtectWildernessFromPlayers())
            {
                return false;
            }
            return true;
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone() && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
            return true;
        if (chunkFaction.isWarZone() && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
            return true;

        if(chunkFaction.isWarZone() && isBlockCarrierAtLocation && isHoldingEagleFeather(user))
        {
            removeEagleFeather(user);
            return true;
        }

        //If player is not in a faction but there is a faction at chunk
        if(!optionalPlayerFaction.isPresent())
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }
            return false;
        }

        final Faction playerFaction = optionalPlayerFaction.get();
        if (this.permsManager.canInteract(user.getUniqueId(), playerFaction, chunkFaction))
            return true;
        else
        {
            //Holding Eagle Feather?
            if(isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean canUseItem(final Location<World> location, final User user, final ItemStackSnapshot usedItem, final boolean shouldNotify)
    {
        final boolean canUseItem = canUseItem(location, user, usedItem);
        if (shouldNotify && !canUseItem)
            notifyPlayer(user);
        return canUseItem;
    }

    private boolean canUseItem(final Location<World> location, final User user, final ItemStackSnapshot usedItem)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Usage of item:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Used item: " + usedItem.getType().getName())));
            }
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return true;

        if (this.playerManager.hasAdminMode(user) || isItemWhitelisted(usedItem.getType().getId()))
            return true;

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return true;
            else return warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT);
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
        {
            return !this.protectionConfig.shouldProtectWildernessFromPlayers();
        }

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone() && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
            return true;
        if (chunkFaction.isWarZone() && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
            return true;

        //If player is not in a faction but there is a faction at chunk
        if(!optionalPlayerFaction.isPresent())
        {
            return false;
        }

        Faction playerFaction = optionalPlayerFaction.get();
        return this.permsManager.canInteract(user.getUniqueId(), playerFaction, chunkFaction);
    }

    @Override
    public boolean canBreak(final Location<World> location, final User user, final boolean shouldNotify)
    {
        final boolean canBreak = canBreak(location, user);
        if (shouldNotify && !canBreak)
            notifyPlayer(user);
        return canBreak;
    }

    private boolean canBreak(final Location<World> location, final User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block break event!")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block id: " + location.getBlockType().getId())));
            }
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return true;

        if(this.playerManager.hasAdminMode(user) || isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId()))
            return true;

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                return true;
            return warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            {
                if(optionalChunkFaction.get().isSafeZone() && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                {
                    return true;
                }
                else return optionalChunkFaction.get().isWarZone() && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
            }

            return optionalPlayerFaction.filter(faction -> this.permsManager.canBreakBlock(user.getUniqueId(), faction, optionalChunkFaction.get())).isPresent();
        }
        else return !this.protectionConfig.shouldProtectWildernessFromPlayers();
    }

    @Override
    public boolean canBreak(final Location<World> location)
    {
        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return true;

        if(isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId()))
            return true;

        if(this.protectionConfig.getSafeZoneWorldNames().contains(world.getName()))
            return false;

        if(this.protectionConfig.getWarZoneWorldNames().contains(world.getName()) && this.protectionConfig.shouldProtectWarZoneFromMobGrief())
            return false;

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(!optionalChunkFaction.isPresent())
            return true;

        if(optionalChunkFaction.get().isSafeZone())
            return false;

        if(optionalChunkFaction.get().isWarZone() && this.protectionConfig.shouldProtectWarZoneFromMobGrief())
            return false;

        if(this.protectionConfig.shouldProtectClaimFromMobGrief())
            return false;

        return true;
    }

    @Override
    public boolean canPlace(final Location<World> location, final User user, final boolean shouldNotify)
    {
        final boolean canPlace = canPlace(location, user);
        if (shouldNotify && !canPlace)
            notifyPlayer(user);
        return canPlace;
    }

    private boolean canPlace(final Location<World> location, final User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block place:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Item in hand: " + (user.getItemInHand(HandTypes.MAIN_HAND).isPresent() ? user.getItemInHand(HandTypes.MAIN_HAND).get().getType().getName() : ""))));
            }
        }

        World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return true;

        if(this.playerManager.hasAdminMode(user) || (user.getItemInHand(HandTypes.MAIN_HAND).isPresent() && isBlockWhitelistedForPlaceDestroy(user.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId())))
            return true;

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                return true;

            if (warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                return true;

            return false;
        }

        Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            {
                if(optionalChunkFaction.get().isSafeZone() && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                {
                    return true;
                }
                else return optionalChunkFaction.get().isWarZone() && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
            }

            return optionalPlayerFaction.filter(faction -> this.permsManager.canPlaceBlock(user.getUniqueId(), faction, optionalChunkFaction.get())).isPresent();
        }
        else return !this.protectionConfig.shouldProtectWildernessFromPlayers();
    }

    @Override
    public boolean canExplode(final Location<World> location, final User user, final boolean shouldNotify)
    {
        final boolean canExplode = canExplode(location, user);
        if (shouldNotify && !canExplode)
            notifyPlayer(user);
        return canExplode;
    }

    private boolean canExplode(final Location<World> location, final User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                final Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Explosion:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
            }
        }

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(location.getExtent().getName()))
            return true;

        boolean shouldProtectWarZoneFromPlayers = this.protectionConfig.shouldProtectWarzoneFromPlayers();
        boolean allowExplosionsByOtherPlayersInClaims = this.protectionConfig.shouldAllowExplosionsByOtherPlayersInClaims();

        //Check if admin
        if(this.playerManager.hasAdminMode(user))
            return true;

        //Check world
        if (this.protectionConfig.getSafeZoneWorldNames().contains(location.getExtent().getName()))
        {
            return false;
        }
        else if (this.protectionConfig.getWarZoneWorldNames().contains(location.getExtent().getName()))
        {
            return !shouldProtectWarZoneFromPlayers;
        }

        //If no faction
        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
        if (!optionalChunkFaction.isPresent())
        {
            return !this.protectionConfig.shouldProtectWildernessFromPlayers();
        }

        //If SafeZone or WarZone
        final Faction chunkFaction = optionalChunkFaction.get();

        if(chunkFaction.isSafeZone() || chunkFaction.isWarZone())
        {
            if(chunkFaction.isSafeZone() && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                return true;
            else return chunkFaction.isWarZone() && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
        }

        //If player is in faction
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if(optionalPlayerFaction.isPresent())
        {
            final Faction playerFaction = optionalPlayerFaction.get();
            if (chunkFaction.getName().equalsIgnoreCase(playerFaction.getName()))
            {
                return this.permsManager.canPlaceBlock(user.getUniqueId(), playerFaction, chunkFaction);
            }
        }

        return allowExplosionsByOtherPlayersInClaims;
    }

    @Override
    public boolean canExplode(final Location<World> location)
    {
        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(location.getExtent().getName()))
            return true;

        boolean shouldProtectWarZoneFromMobGrief = this.protectionConfig.shouldProtectWarZoneFromMobGrief();
        boolean shouldProtectClaimsFromMobGrief = this.protectionConfig.shouldProtectClaimFromMobGrief();

        //Check world
        if (this.protectionConfig.getSafeZoneWorldNames().contains(location.getExtent().getName()))
            return false;

        if (this.protectionConfig.getWarZoneWorldNames().contains(location.getExtent().getName()))
            return !shouldProtectWarZoneFromMobGrief;

        Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
        if (!optionalChunkFaction.isPresent())
            return true;

        Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
            return false;
        else if (chunkFaction.isWarZone() && shouldProtectWarZoneFromMobGrief)
            return false;
        else return !shouldProtectClaimsFromMobGrief;
    }

    @Override
    public boolean canAttackEntity(final Entity attackedEntity, final Player player, final boolean shouldNotify)
    {
        final boolean canAttack = canAttackEntity(attackedEntity, player);
        if (shouldNotify && !canAttack)
            notifyPlayer(player);
        return canAttack;
    }

    private boolean canAttackEntity(final Entity attackedEntity, final Player player)
    {
        if (this.playerManager.hasAdminMode(player))
            return true;

        final boolean isPlayer = attackedEntity instanceof Player;
        final boolean isMob = !isPlayer && (attackedEntity instanceof Living && !(attackedEntity instanceof ArmorStand));
        if (isMob)
            return true;

        final Location<World> entityLocation = attackedEntity.getLocation();
        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(entityLocation.getExtent().getUniqueId(), entityLocation.getChunkPosition());
        final Optional<Faction> optionalAttackerPlayerFaction = this.factionLogic.getFactionByPlayerUUID(player.getUniqueId());
        final Optional<Faction> optionalSourceChunkFaction = this.factionLogic.getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        final boolean isSafeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(entityLocation.getExtent().getName());
        final boolean isWarZoneWorld = this.protectionConfig.getWarZoneWorldNames().contains(entityLocation.getExtent().getName());
        final boolean notClaimableWorld = this.protectionConfig.getNotClaimableWorldNames().contains(entityLocation.getExtent().getName());

        if (isPlayer)
        {
            final Player attackedPlayer = (Player) attackedEntity;
            final Optional<Faction> optionalAttackedPlayerFaction = this.factionLogic
                    .getFactionByPlayerUUID(attackedPlayer.getUniqueId());
            if (isSafeZoneWorld)
                return false;
            if (optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
                return false;
            if (attackedEntity.equals(player))
                return true;
            if (optionalSourceChunkFaction.isPresent() && optionalSourceChunkFaction.get().isSafeZone())
                return false;
            if (!optionalAttackerPlayerFaction.isPresent())
                return true;
            if (!optionalAttackedPlayerFaction.isPresent())
                return true;
            final Faction attackedFaction = optionalAttackedPlayerFaction.get();
            final Faction attackerFaction = optionalAttackerPlayerFaction.get();
            if (!attackerFaction.getName().equals(attackedFaction.getName()))
            {
                if (attackerFaction.isAlly(attackedFaction) && !this.factionsConfig.isAllianceFriendlyFire())
                    return false;
                else if (attackerFaction.isTruce(attackedFaction) && !this.factionsConfig.isTruceFriendlyFire())
                    return false;
            }
            else
            {
                return this.factionsConfig.isFactionFriendlyFire();
            }
        }
        else //Item Frame, Minecraft, Painting etc.
        {
            //Not claimable worlds should be always ignored by protection system.
            if (notClaimableWorld)
                return true;
            if (isSafeZoneWorld)
                return false;
            if (isWarZoneWorld)
                return !this.protectionConfig.shouldProtectWarzoneFromPlayers();
            if (!optionalChunkFaction.isPresent())
                return true;
            final Faction chunkFaction = optionalChunkFaction.get();
            if (chunkFaction.isSafeZone())
                return false;
            else if (chunkFaction.isWarZone())
                return !this.protectionConfig.shouldProtectWarzoneFromPlayers();
            if (!optionalAttackerPlayerFaction.isPresent())
                return false;
            final Faction attackerFaction = optionalAttackerPlayerFaction.get();
            return this.permsManager.canBreakBlock(player.getUniqueId(), attackerFaction, chunkFaction);
        }

        return true;
    }

    @Override
    public boolean isItemWhitelisted(final String itemId)
    {
        final Set<String> whiteListedItems = this.protectionConfig.getWhiteListedItems();
        return isWhiteListed(whiteListedItems, itemId);
    }

    @Override
    public boolean isBlockWhitelistedForInteraction(final String blockId)
    {
        final Set<String> whiteListedBlocks = this.protectionConfig.getWhiteListedInteractBlocks();
        return isWhiteListed(whiteListedBlocks, blockId);
    }

    @Override
    public boolean isBlockWhitelistedForPlaceDestroy(final String blockOrItemId)
    {
        final Set<String> whiteListedBlocks = this.protectionConfig.getWhiteListedPlaceDestroyBlocks();
        return isWhiteListed(whiteListedBlocks, blockOrItemId);
    }

    private boolean isWhiteListed(final Collection<String> collection, final String itemId)
    {
        for(final String whiteListedItemId : collection)
        {
            if(whiteListedItemId.equals(itemId))
                return true;

            try
            {
                final Pattern pattern = Pattern.compile(whiteListedItemId);
                if(pattern.matcher(itemId).matches())
                    return true;
            }
            catch(final PatternSyntaxException exception)
            {
                //I guess it must be empty...
            }
        }
        return false;
    }

    private boolean isHoldingEagleFeather(final User user)
    {
        return user.getItemInHand(HandTypes.MAIN_HAND).isPresent()
                && user.getItemInHand(HandTypes.MAIN_HAND).get().getType() == ItemTypes.FEATHER
                && user.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).isPresent()
                && user.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().equals(EagleFeather.getDisplayName());
    }

    private void removeEagleFeather(final User user)
    {
        final ItemStack feather = user.getItemInHand(HandTypes.MAIN_HAND).get();
        feather.setQuantity(feather.getQuantity() - 1);
        user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.DARK_PURPLE, "You have used eagle's feather!")));
    }

    private void notifyPlayer(final User user)
    {
        if (this.chatConfig.shouldDisplayProtectionSystemMessages())
        {
            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
        }
    }
}
