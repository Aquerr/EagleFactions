package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
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
    private final ProtectionManager protectionManager;
    private final FactionLogic factionLogic;
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;
    private final MessageService messageService;

    public EntitySpawnListener(EagleFactions plugin)
    {
        super(plugin);
        this.protectionManager = plugin.getProtectionManager();
        this.factionLogic = plugin.getFactionLogic();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
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

            if (isItemUsed)
            {
                final ItemStackSnapshot itemStackSnapshot = eventContext.get(EventContextKeys.USED_ITEM).get();
                if (serverPlayerCause != null)
                {
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
                    return;
                }
                else if(faction.isWarZone() && !faction.getProtectionFlags().getValueForFlag(ProtectionFlagType.SPAWN_MONSTERS))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(!faction.getProtectionFlags().getValueForFlag(ProtectionFlagType.SPAWN_MONSTERS))
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
                if(faction.isSafeZone() && !faction.getProtectionFlags().getValueForFlag(ProtectionFlagType.SPAWN_ANIMALS))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(faction.isWarZone() && !faction.getProtectionFlags().getValueForFlag(ProtectionFlagType.SPAWN_ANIMALS))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(!faction.getProtectionFlags().getValueForFlag(ProtectionFlagType.SPAWN_ANIMALS))
                {
                    event.setCancelled(true);
                    return;
                }
                return;
            }
        }
    }

    private boolean canSpawnAnimalsInSafeZone()
    {
        return this.factionLogic.getFactionByName("SafeZone").getProtectionFlags().getValueForFlag(ProtectionFlagType.SPAWN_ANIMALS);
    }

    private boolean canSpawnAnimalsInWarzone()
    {
        return this.factionLogic.getFactionByName("WarZone").getProtectionFlags().getValueForFlag(ProtectionFlagType.SPAWN_ANIMALS);
    }
}
