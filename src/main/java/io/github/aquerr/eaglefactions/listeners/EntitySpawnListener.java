package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.util.ModSupport;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Iterator;
import java.util.Optional;

public class EntitySpawnListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;
    private final MessageService messageService;

    public EntitySpawnListener(EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.messageService = plugin.getMessageService();
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onEntitySpawn(final SpawnEntityEvent event)
    {
        Cause cause = event.cause();
        Object rootCause = cause.root();
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
                        if (!super.getPlugin().getProtectionManager().canBreak(causeEntity.serverLocation(), (User) entity1, false).hasAccess())
                        {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                else if (ModSupport.isIndustrialCraftMiningLaser(entity) && eventContext.containsKey(EventContextKeys.CREATOR))
                {
                    ServerPlayer user = (ServerPlayer) eventContext.get(EventContextKeys.PLAYER).get();
                    Entity miningLaser = (Entity)rootCause;
                    if(!super.getPlugin().getProtectionManager().canExplode(miningLaser.serverLocation(), user.user(), false).hasAccess())
                    {
                        event.setCancelled(true);
                        continue;
                    }
                }
            }

            if(entity.toString().contains("EntityCustomNpc")) return;

            if (isItemUsed)
            {
                final ServerPlayer user = (ServerPlayer) eventContext.get(EventContextKeys.PLAYER).get();
                final ItemStackSnapshot itemStackSnapshot = eventContext.get(EventContextKeys.USED_ITEM).get();
                if (!super.getPlugin().getProtectionManager().canUseItem(entity.serverLocation(), user.user(), itemStackSnapshot, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }
            else if(spawnType == SpawnTypes.PLACEMENT && isPlayerPlace)
            {
                ServerPlayer user = (ServerPlayer) eventContext.get(EventContextKeys.PLAYER).get();
                //Entity spawned from a command or something similar... (can be a hammer that is being used with right-click on a machine block)
                //Let's treat is as a place event for now...
                if(!super.getPlugin().getProtectionManager().canPlace(entity.serverLocation(), user.user(), false).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }

            boolean isHostile = entity instanceof Hostile;
            boolean isPlayer = entity instanceof Player;
            boolean isLiving = entity instanceof Living;

            if(!isHostile && !isPlayer && !isLiving)
                return;

            if(isPlayer)
            {
                if(this.factionsConfig.shouldSpawnAtHomeAfterDeath())
                {
                    Player player = (Player)entity;
                    Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
                    if(!optionalPlayerFaction.isPresent())
                        return;

                    Faction faction = optionalPlayerFaction.get();
                    FactionHome factionHome = faction.getHome();
                    if(factionHome != null)
                    {
                        event.setCancelled(true);
                        ServerWorld world = WorldUtil.getWorldByUUID(factionHome.getWorldUUID()).get();
                        player.setLocation(ServerLocation.of(world, factionHome.getBlockPosition()));
                        return;
                    }
                    else
                    {
                        player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.home.could-not-spawn-at-faction-home-it-may-not-be-set")));
                    }
                }
                return;
            }

            if(isHostile)
            {
                //Check worlds
                if(this.protectionConfig.getSafeZoneWorldNames().contains(entity.world().toString()))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(this.protectionConfig.getWarZoneWorldNames().contains(entity.world().toString()) && !this.protectionConfig.canSpawnHostileMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }

                Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(((ServerWorld)entity.world()).uniqueId(), entity.serverLocation().chunkPosition());
                if(!optionalFaction.isPresent())
                    return;

                Faction faction = optionalFaction.get();
                if(faction.isSafeZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(faction.isWarZone() && !this.protectionConfig.canSpawnHostileMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(!this.protectionConfig.canSpawnHostileMobsInFactionsTerritory())
                {
                    event.setCancelled(true);
                    return;
                }

                return;
            }

            if(isLiving)
            {
                if(entity instanceof ArmorStand)
                    return;

                //Check worlds
                if(this.protectionConfig.getSafeZoneWorldNames().contains(((ServerWorld)entity.world()).key().asString())
                    && !this.protectionConfig.canSpawnMobsInSafeZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(this.protectionConfig.getWarZoneWorldNames().contains(((ServerWorld)entity.world()).key().asString()) && !this.protectionConfig.canSpawnMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }

                Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(((ServerWorld)entity.world()).uniqueId(), entity.serverLocation().chunkPosition());
                if(!optionalFaction.isPresent())
                    return;

                Faction faction = optionalFaction.get();
                if(faction.isSafeZone() && !this.protectionConfig.canSpawnMobsInSafeZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(faction.isWarZone() && !this.protectionConfig.canSpawnMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(!this.protectionConfig.canSpawnMobsInFactionsTerritory())
                {
                    event.setCancelled(true);
                    return;
                }
                return;
            }
        }
    }
}
