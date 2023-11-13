package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.HomeConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.util.ModSupport;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EntitySpawnListener extends AbstractListener
{
    private static final Set<UUID> HOME_TELEPORT_PLAYER_UUIDS = new HashSet<>();

    private final ProtectionManager protectionManager;
    private final FactionLogic factionLogic;
    private final HomeConfig homeConfig;
    private final ProtectionConfig protectionConfig;
    private final MessageService messageService;

    public EntitySpawnListener(EagleFactions plugin)
    {
        super(plugin);
        this.protectionManager = plugin.getProtectionManager();
        this.factionLogic = plugin.getFactionLogic();
        this.homeConfig = plugin.getConfiguration().getHomeConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.messageService = plugin.getMessageService();
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onEntitySpawn(final SpawnEntityEvent event)
    {
        Cause cause = event.cause();
        Object rootCause = cause.root();
        ServerPlayer serverPlayerCause = cause.first(ServerPlayer.class).orElse(null);
        EventContext eventContext = event.context();
        final SpawnType spawnType = eventContext.get(EventContextKeys.SPAWN_TYPE).orElse(null);
        final boolean isPlayerPlace = eventContext.get(EventContextKeys.PLAYER_PLACE).isPresent() && eventContext.get(EventContextKeys.CREATOR).isPresent();
        final boolean isItemUsed = eventContext.get(EventContextKeys.USED_ITEM).isPresent() && eventContext.get(EventContextKeys.CREATOR).isPresent();

        Iterator<Entity> entitiesIterator = event.entities().iterator();
        while(entitiesIterator.hasNext())
        {
            Entity entity = entitiesIterator.next();

            //Special case for IC2 and Mekanism
            if(rootCause instanceof Entity)
            {
                Entity causeEntity = (Entity)rootCause;
                if (ModSupport.isMekenism(causeEntity))
                {
                    final Entity entity1 = ModSupport.getEntityOwnerFromMekanism(causeEntity);
                    if (entity1 instanceof User)
                    {
                        if (!this.protectionManager.canBreak(causeEntity.serverLocation().createSnapshot(), (User) entity1, false).hasAccess())
                        {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                else if (ModSupport.isIndustrialCraftMiningLaser(entity) && eventContext.containsKey(EventContextKeys.CREATOR))
                {
                    Entity miningLaser = (Entity)rootCause;
                    if(!this.protectionManager.canExplode(miningLaser.serverLocation()).hasAccess())
                    {
                        event.setCancelled(true);
                        continue;
                    }
                }
            }

            if(entity.toString().contains("EntityCustomNpc")) return;

            // For dropped items either by destroying a block or killing a mob.
            if (isDropLoot(spawnType))
            {
                return;
            }
            else if (isItemUsed)
            {
                if (serverPlayerCause != null)
                {
                    final ItemStackSnapshot itemStackSnapshot = eventContext.get(EventContextKeys.USED_ITEM).get();
                    if (!this.protectionManager.canUseItem(entity.serverLocation(), serverPlayerCause.user(), itemStackSnapshot, true).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
                else
                {
                    if (!this.protectionManager.canBreak(entity.serverLocation().createSnapshot()).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            else if(spawnType == SpawnTypes.PLACEMENT.get() && isPlayerPlace)
            {
                //Entity spawned from a command or something similar... (can be a hammer that is being used with right-click on a machine block)
                //Let's treat is as a place event for now...
                if(!this.protectionManager.canPlace(entity.serverLocation().createSnapshot(), serverPlayerCause.user(), false).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }

            boolean isHostile = entity instanceof Hostile;
            boolean isLiving = entity instanceof Living;

            if(!isHostile && !isLiving)
            {
                return;
            }
            else if(isHostile)
            {
                handleHostileMobSpawn(event, entity);
            }
            else if(isLiving)
            {
                handleLivingEntitySpawn(event, entity);
            }
        }
    }

    @Listener(beforeModifications = true)
    public void onPlayerRespawn(final RespawnPlayerEvent.Recreate event)
    {
        if (!event.isDeath())
            return;

        handlePlayerSpawnAfterDeath(event.entity());
    }

    /**
     * Handles the actual teleportation to home after death.
     */
    @Listener
    public void onPlayerRespawnPost(final RespawnPlayerEvent.Post event)
    {
        ServerPlayer serverPlayer = event.entity();
        if (HOME_TELEPORT_PLAYER_UUIDS.contains(serverPlayer.uniqueId()))
        {
            HOME_TELEPORT_PLAYER_UUIDS.remove(serverPlayer.uniqueId());
            Faction faction = getPlugin().getFactionLogic().getFactionByPlayerUUID(serverPlayer.uniqueId())
                    .orElse(null);

            if(faction == null)
                return;

            FactionHome factionHome = faction.getHome();
            if (factionHome == null)
            {
                serverPlayer.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.home.could-not-spawn-at-faction-home-it-may-not-be-set")));
                return;
            }

            ServerWorld world = WorldUtil.getWorldByUUID(factionHome.getWorldUUID()).orElse(null);
            if (world != null)
            {
                ServerLocation safeLocation = Sponge.server().teleportHelper().findSafeLocation(ServerLocation.of(world, factionHome.getBlockPosition()))
                        .orElse(ServerLocation.of(world, factionHome.getBlockPosition()));
                serverPlayer.setLocation(safeLocation);
            }
            else
            {
                serverPlayer.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.home.could-not-spawn-at-faction-home-it-may-not-be-set")));
            }
        }
    }

    private void handleLivingEntitySpawn(SpawnEntityEvent event, Entity entity)
    {
        if(entity instanceof ArmorStand)
            return;

        //Check worlds
        if(this.protectionConfig.getSafeZoneWorldNames().contains(((ServerWorld)entity.world()).key().asString())
                && !canSpawnAnimalsInSafeZone())
        {
            event.setCancelled(true);
            return;
        }
        else if(this.protectionConfig.getWarZoneWorldNames().contains(((ServerWorld)entity.world()).key().asString()) && !canSpawnAnimalsInWarzone())
        {
            event.setCancelled(true);
            return;
        }

        Optional<Faction> optionalFaction = this.factionLogic.getFactionByChunk(((ServerWorld)entity.world()).uniqueId(), entity.serverLocation().chunkPosition());
        if(!optionalFaction.isPresent())
            return;

        Faction faction = optionalFaction.get();
        if(faction.isSafeZone() && !faction.getProtectionFlagValue(ProtectionFlagType.SPAWN_ANIMALS))
        {
            event.setCancelled(true);
        }
        else if(faction.isWarZone() && !faction.getProtectionFlagValue(ProtectionFlagType.SPAWN_ANIMALS))
        {
            event.setCancelled(true);
        }
        else if(!faction.getProtectionFlagValue(ProtectionFlagType.SPAWN_ANIMALS))
        {
            event.setCancelled(true);
        }
    }

    private void handleHostileMobSpawn(SpawnEntityEvent event, Entity entity)
    {
        //Check worlds
        if(this.protectionConfig.getSafeZoneWorldNames().contains(entity.world().toString()))
        {
            event.setCancelled(true);
            return;
        }
        else if(this.protectionConfig.getWarZoneWorldNames().contains(entity.world().toString()) && !canSpawnAnimalsInWarzone())
        {
            event.setCancelled(true);
            return;
        }

        Optional<Faction> optionalFaction = this.factionLogic.getFactionByChunk(((ServerWorld)entity.world()).uniqueId(), entity.serverLocation().chunkPosition());
        if(!optionalFaction.isPresent())
            return;

        Faction faction = optionalFaction.get();
        if(faction.isSafeZone())
        {
            event.setCancelled(true);
        }
        else if(faction.isWarZone() && !faction.getProtectionFlagValue(ProtectionFlagType.SPAWN_MONSTERS))
        {
            event.setCancelled(true);
        }
        else if(!faction.getProtectionFlagValue(ProtectionFlagType.SPAWN_MONSTERS))
        {
            event.setCancelled(true);
        }
    }

    private void handlePlayerSpawnAfterDeath(ServerPlayer serverPlayer)
    {
        if (!this.homeConfig.shouldSpawnAtHomeAfterDeath())
            return;

        HOME_TELEPORT_PLAYER_UUIDS.add(serverPlayer.uniqueId());
    }

    private boolean isDropLoot(SpawnType spawnType)
    {
        return spawnType == SpawnTypes.DROPPED_ITEM.get();
    }

    private boolean canSpawnAnimalsInSafeZone()
    {
        return this.factionLogic.getFactionByName(EagleFactionsPlugin.SAFE_ZONE_NAME).getProtectionFlagValue(ProtectionFlagType.SPAWN_ANIMALS);
    }

    private boolean canSpawnAnimalsInWarzone()
    {
        return this.factionLogic.getFactionByName(EagleFactionsPlugin.WAR_ZONE_NAME).getProtectionFlagValue(ProtectionFlagType.SPAWN_ANIMALS);
    }
}
