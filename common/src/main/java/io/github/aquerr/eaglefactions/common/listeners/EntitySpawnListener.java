package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.util.ModSupport;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Iterator;
import java.util.Optional;

public class EntitySpawnListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;

    public EntitySpawnListener(EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onEntitySpawn(final SpawnEntityEvent event)
    {
        Cause cause = event.getCause();
        Object rootCause = cause.root();
        EventContext eventContext = event.getContext();
        final SpawnType spawnType = eventContext.get(EventContextKeys.SPAWN_TYPE).orElse(null);
        final boolean isPlayerPlace = eventContext.get(EventContextKeys.PLAYER_PLACE).isPresent() && eventContext.get(EventContextKeys.OWNER).isPresent();
        final boolean isItemUsed = eventContext.get(EventContextKeys.USED_ITEM).isPresent() && eventContext.get(EventContextKeys.OWNER).isPresent();

        Iterator<Entity> entitiesIterator = event.getEntities().iterator();
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
                        if (!super.getPlugin().getProtectionManager().canBreak(causeEntity.getLocation(), (User) entity1, false).hasAccess())
                        {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                else if (ModSupport.isIndustrialCraftMiningLaser(entity) && eventContext.containsKey(EventContextKeys.OWNER))
                {
                    User user = eventContext.get(EventContextKeys.OWNER).get();
                    Entity miningLaser = (Entity)rootCause;
                    if(!super.getPlugin().getProtectionManager().canExplode(miningLaser.getLocation(), user, false).hasAccess())
                    {
                        event.setCancelled(true);
                        continue;
                    }
                }
            }

            if(entity.toString().contains("EntityCustomNpc")) return;

            if (isItemUsed)
            {
                final User user = eventContext.get(EventContextKeys.OWNER).get();
                final ItemStackSnapshot itemStackSnapshot = eventContext.get(EventContextKeys.USED_ITEM).get();
                if (!super.getPlugin().getProtectionManager().canUseItem(entity.getLocation(), user, itemStackSnapshot, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }
            else if(spawnType == SpawnTypes.PLACEMENT && isPlayerPlace)
            {
                final User user = eventContext.get(EventContextKeys.OWNER).get();
                //Entity spawned from a command or something similar... (can be a hammer that is being used with right-click on a machine block)
                //Let's treat is as a place event for now...
                if(!super.getPlugin().getProtectionManager().canPlace(entity.getLocation(), user, false).hasAccess())
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
                    Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                    if(!optionalPlayerFaction.isPresent())
                        return;

                    Faction faction = optionalPlayerFaction.get();
                    FactionHome factionHome = faction.getHome();
                    if(factionHome != null)
                    {
                        event.setCancelled(true);
                        World world = Sponge.getServer().getWorld(factionHome.getWorldUUID()).get();
                        player.setLocation(new Location<World>(world, factionHome.getBlockPosition()));
                        return;
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.COULD_NOT_SPAWN_AT_FACTIONS_HOME_HOME_MAY_NOT_BE_SET));
                    }
                }
                return;
            }

            if(isHostile)
            {
                //Check worlds
                if(this.protectionConfig.getSafeZoneWorldNames().contains(entity.getWorld().getName()))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(this.protectionConfig.getWarZoneWorldNames().contains(entity.getWorld().getName()) && !this.protectionConfig.canSpawnHostileMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }

                Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition());
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
                if(this.protectionConfig.getSafeZoneWorldNames().contains(entity.getWorld().getName())
                    && !this.protectionConfig.canSpawnMobsInSafeZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(this.protectionConfig.getWarZoneWorldNames().contains(entity.getWorld().getName()) && !this.protectionConfig.canSpawnMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }

                Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition());
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
