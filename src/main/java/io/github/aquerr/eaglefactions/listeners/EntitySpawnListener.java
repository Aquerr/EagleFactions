package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
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
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Iterator;
import java.util.Optional;

public class EntitySpawnListener extends AbstractListener
{
    public EntitySpawnListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onEntitySpawn(SpawnEntityEvent event)
    {
        Cause cause = event.getCause();
        Object rootCause = cause.root();
        EventContext eventContext = event.getContext();

        Iterator<Entity> entitiesIterator = event.getEntities().iterator();
        while(entitiesIterator.hasNext())
        {
            Entity entity = entitiesIterator.next();

            //Special case for IC2's Mining Laser's explosive mode
            if(rootCause instanceof Entity)
            {
                Entity causeEntity = (Entity)rootCause;
                String entityId = causeEntity.getType().getId();
                if(entityId.contains("mininglaser")
                        && eventContext.containsKey(EventContextKeys.OWNER))
                {
                    User user = eventContext.get(EventContextKeys.OWNER).get();
                    Entity miningLaser = (Entity)rootCause;
                    if(!super.getPlugin().getProtectionManager().canExplode(miningLaser.getLocation(), user))
                    {
                        event.setCancelled(true);
                        continue;
                    }
                }
            }

            if(entity.toString().contains("EntityCustomNpc")) return;

            boolean isHostile = entity instanceof Hostile;
            boolean isPlayer = entity instanceof Player;
            boolean isLiving = entity instanceof Living;

            if(!isHostile && !isPlayer && !isLiving)
                return;

            if(isPlayer)
            {
                if(super.getPlugin().getConfiguration().getConfigFields().shouldSpawnAtHomeAfterDeath())
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
                        player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.COULD_NOT_SPAWN_AT_FACTIONS_HOME_HOME_MAY_NOT_BE_SET));
                    }
                }
                return;
            }

            if(isHostile)
            {
                //Check worlds
                if(super.getPlugin().getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(entity.getWorld().getName()))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(super.getPlugin().getConfiguration().getConfigFields().getWarZoneWorldNames().contains(entity.getWorld().getName()) && !super.getPlugin().getConfiguration().getConfigFields().canSpawnHostileMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }

                Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition());
                if(!optionalFaction.isPresent())
                    return;

                Faction faction = optionalFaction.get();
                if(faction.getName().equalsIgnoreCase("SafeZone"))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(faction.getName().equalsIgnoreCase("WarZone") && !super.getPlugin().getConfiguration().getConfigFields().canSpawnHostileMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(!super.getPlugin().getConfiguration().getConfigFields().canSpawnHostileMobsInFactionsTerritory())
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
                if(super.getPlugin().getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(entity.getWorld().getName())
                    && !super.getPlugin().getConfiguration().getConfigFields().canSpawnMobsInSafeZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(super.getPlugin().getConfiguration().getConfigFields().getWarZoneWorldNames().contains(entity.getWorld().getName()) && !super.getPlugin().getConfiguration().getConfigFields().canSpawnMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }

                Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition());
                if(!optionalFaction.isPresent())
                    return;

                Faction faction = optionalFaction.get();
                if(faction.getName().equalsIgnoreCase("SafeZone") && !super.getPlugin().getConfiguration().getConfigFields().canSpawnMobsInSafeZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(faction.getName().equalsIgnoreCase("WarZone") && !super.getPlugin().getConfiguration().getConfigFields().canSpawnMobsInWarZone())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(!super.getPlugin().getConfiguration().getConfigFields().canSpawnMobsInFactionsTerritory())
                {
                    event.setCancelled(true);
                    return;
                }
                return;
            }
        }
    }
}
