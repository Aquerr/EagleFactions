package io.github.aquerr.eaglefactions.common.managers;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.EagleFeather;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        final boolean isBlockCarrierAtLocation = location.getTileEntity().isPresent() && location.getTileEntity().get() instanceof BlockCarrier;

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT);
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.WAR_ZONE))
                return true;
            if (user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return true;
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }
            return false;
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
            return !this.protectionConfig.shouldProtectWildernessFromPlayers();

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT);
        }
        if (chunkFaction.isWarZone())
        {
            if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.WAR_ZONE))
                return true;
            if(user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return true;
            if (isBlockCarrierAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }
            return false;
        }

        if (isBlockWhitelistedForInteraction(location.getBlockType().getId(), FactionType.FACTION))
            return true;

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

        if (this.playerManager.hasAdminMode(user))
            return true;

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.SAFE_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT);
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.WAR_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT);
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
            return !this.protectionConfig.shouldProtectWildernessFromPlayers();

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.isSafeZone())
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.SAFE_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT);
        }
        if (chunkFaction.isWarZone())
        {
            if (isItemWhitelisted(usedItem.getType().getId(), FactionType.WAR_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT);
        }

        if (isItemWhitelisted(usedItem.getType().getId(), FactionType.FACTION))
            return true;

        //If player is not in a faction but there is a faction at chunk
        if(!optionalPlayerFaction.isPresent())
            return false;

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
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block break event!")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block at location: " + location.getBlockType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, "Block id: " + location.getBlockType().getId())));
            }
        }

        final World world = location.getExtent();

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return true;

        if(this.playerManager.hasAdminMode(user))
            return true;

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD);
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.WAR_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(user.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            {
                if(optionalChunkFaction.get().isSafeZone())
                {
                    if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.SAFE_ZONE))
                        return true;
                    return user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD);
                }
                else //WarZone
                {
                    if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.WAR_ZONE))
                        return true;
                    return user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
                }
            }

            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.FACTION))
                return true;

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

        if(this.protectionConfig.getSafeZoneWorldNames().contains(world.getName()))
            return isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.SAFE_ZONE);

        if(this.protectionConfig.getWarZoneWorldNames().contains(world.getName()) && this.protectionConfig.shouldProtectWarZoneFromMobGrief())
        {
            //Not sure if we should use white-list for mobs...
            return isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.WAR_ZONE);
        }

        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(!optionalChunkFaction.isPresent())
            return true;

        if(optionalChunkFaction.get().isSafeZone())
            return isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.SAFE_ZONE);

        if(optionalChunkFaction.get().isWarZone() && this.protectionConfig.shouldProtectWarZoneFromMobGrief())
            return isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.WAR_ZONE);

        if(this.protectionConfig.shouldProtectClaimFromMobGrief())
            return isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.FACTION);

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
        final String itemId = user.getItemInHand(HandTypes.MAIN_HAND).isPresent() ? user.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId() : "";

        //Not claimable worlds should be always ignored by protection system.
        if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()))
            return true;

        if(this.playerManager.hasAdminMode(user))
            return true;

        final Set<String> safeZoneWorlds = this.protectionConfig.getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.protectionConfig.getWarZoneWorldNames();

        if (safeZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.SAFE_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD);
        }
        if (warZoneWorlds.contains(world.getName()))
        {
            if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.WAR_ZONE))
                return true;
            return user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
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
                        return true;
                    return user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD);
                }
                else //WarZone
                {
                    if (isBlockWhitelistedForPlaceDestroy(itemId, FactionType.WAR_ZONE))
                        return true;
                    return user.hasPermission(PluginPermissions.WAR_ZONE_BUILD);
                }
            }

            if (isBlockWhitelistedForPlaceDestroy(location.getBlockType().getId(), FactionType.FACTION))
                return true;

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
            if(chunkFaction.isSafeZone())
                return user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD);
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
    public boolean canHitEntity(final Entity attackedEntity, final Player player, final boolean shouldNotify)
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
        final boolean isWarZoneWorld = !isSafeZoneWorld && this.protectionConfig.getWarZoneWorldNames().contains(entityLocation.getExtent().getName());
        final boolean notClaimableWorld = !isSafeZoneWorld && !isWarZoneWorld && this.protectionConfig.getNotClaimableWorldNames().contains(entityLocation.getExtent().getName());

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
    public boolean canNotifyBlock(final Location<World> notifier, final Location<World> notifiedLocation)
    {
        //First, let's check the world.
        //TODO: Maybe we should check notifier's world as well?
        final boolean isSafeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(notifiedLocation.getExtent().getName());
        final boolean isWarZoneWorld = !isSafeZoneWorld && this.protectionConfig.getWarZoneWorldNames().contains(notifiedLocation.getExtent().getName());
        final boolean notClaimableWorld = !isSafeZoneWorld && !isWarZoneWorld && this.protectionConfig.getNotClaimableWorldNames().contains(notifiedLocation.getExtent().getName());

        //Entire world is one claim type thus we should allow the notification.
        if (isSafeZoneWorld || isWarZoneWorld || notClaimableWorld)
            return true;

        final Optional<Faction> notifierFaction = this.factionLogic.getFactionByChunk(notifier.getExtent().getUniqueId(), notifier.getChunkPosition());
        final Optional<Faction> notifiedFaction = this.factionLogic.getFactionByChunk(notifiedLocation.getExtent().getUniqueId(), notifiedLocation.getChunkPosition());

        // Factions can notify wilderness but wilderness cannot notify factions.
        // Wilderness can only notify other factions if mob-gref is set to true.

        //Source is wilderness.
        if (!notifierFaction.isPresent())
        {
            //Both wilderness
            if (!notifiedFaction.isPresent())
                return true;
            final Faction faction = notifiedFaction.get();
            if (faction.isSafeZone()) //Notified SafeZone
                return false;
            else if(faction.isWarZone()) //Notified WarZone
                return !this.protectionConfig.shouldProtectWarZoneFromMobGrief();
            else return !this.protectionConfig.shouldProtectClaimFromMobGrief(); //Notified Regular faction

        }

        final Faction sourceFaction = notifierFaction.get();

        //Regular factions can notify locations in wilderness.
        if (!notifiedFaction.isPresent())
            return true;

        final Faction targetFaction = notifiedFaction.get();

        //Reference check if factions are equal.
        if(targetFaction.equals(sourceFaction))
            return true;

        if (sourceFaction.isSafeZone())
        {
            if(targetFaction.isSafeZone())
                return true;
            else if(targetFaction.isWarZone())
                return !this.protectionConfig.shouldProtectWarZoneFromMobGrief();
            else return !this.protectionConfig.shouldProtectClaimFromMobGrief();
        }
        else if (sourceFaction.isWarZone())
        {
            if(targetFaction.isWarZone())
                return true;
            else if(targetFaction.isSafeZone())
                return false;
            else return !this.protectionConfig.shouldProtectClaimFromMobGrief();
        }
        else
        {
            if(targetFaction.isSafeZone())
                return false;
            else if(targetFaction.isWarZone())
                return !this.protectionConfig.shouldProtectWarZoneFromMobGrief();
            else return !this.protectionConfig.shouldProtectClaimFromMobGrief();
        }
    }

    @Override
    public boolean isItemWhitelisted(final String itemId, final FactionType factionType)
    {
        if (Strings.isNullOrEmpty(itemId) || Objects.isNull(factionType))
            throw new IllegalArgumentException("Item id and faction type must be provided");

        switch (factionType)
        {
            case FACTION:
                return isWhiteListed(this.protectionConfig.getFactionWhitelists().getWhiteListedItems(), itemId);
            case WAR_ZONE:
                return isWhiteListed(this.protectionConfig.getWarZoneWhitelists().getWhiteListedItems(), itemId);
            case SAFE_ZONE:
                return isWhiteListed(this.protectionConfig.getSafeZoneWhitelists().getWhiteListedItems(), itemId);
            default:
                return false;
        }
    }

    @Override
    public boolean isBlockWhitelistedForInteraction(final String blockId, final FactionType factionType)
    {
        if (Strings.isNullOrEmpty(blockId) || Objects.isNull(factionType))
            throw new IllegalArgumentException("Item id and faction type must be provided");

        switch (factionType)
        {
            case FACTION:
                return isWhiteListed(this.protectionConfig.getFactionWhitelists().getWhiteListedInteractBlocks(), blockId);
            case WAR_ZONE:
                return isWhiteListed(this.protectionConfig.getWarZoneWhitelists().getWhiteListedInteractBlocks(), blockId);
            case SAFE_ZONE:
                return isWhiteListed(this.protectionConfig.getSafeZoneWhitelists().getWhiteListedInteractBlocks(), blockId);
            default:
                return false;
        }
    }

    @Override
    public boolean isBlockWhitelistedForPlaceDestroy(final String blockOrItemId, final FactionType factionType)
    {
        if (Strings.isNullOrEmpty(blockOrItemId) || Objects.isNull(factionType))
            throw new IllegalArgumentException("Item id and faction type must be provided");

        switch (factionType)
        {
            case FACTION:
                return isWhiteListed(this.protectionConfig.getFactionWhitelists().getWhiteListedPlaceDestroyBlocks(), blockOrItemId);
            case WAR_ZONE:
                return isWhiteListed(this.protectionConfig.getWarZoneWhitelists().getWhiteListedPlaceDestroyBlocks(), blockOrItemId);
            case SAFE_ZONE:
                return isWhiteListed(this.protectionConfig.getSafeZoneWhitelists().getWhiteListedPlaceDestroyBlocks(), blockOrItemId);
            default:
                return false;
        }
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
