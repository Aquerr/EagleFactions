package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.projectile.source.ProjectileSource;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class CollideEntityEventListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;

    public CollideEntityEventListener(EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityCollideEntity(final CollideEntityEvent event)
    {
        final List<Entity> entityList = event.entities();
        final EventContext eventContext = event.context();
        final Cause cause = event.cause();
        final Object source = event.source();
        User user = null;
        boolean isProjectileSource = eventContext.containsKey(EventContextKeys.PROJECTILE_SOURCE);

        if(isProjectileSource)
        {
            final ProjectileSource projectileSource = eventContext.get(EventContextKeys.PROJECTILE_SOURCE).get();
            if(projectileSource instanceof ServerPlayer)
            {
                user = ((ServerPlayer) projectileSource).user();
            }
        }

        for(final Entity entity : entityList)
        {
            //Check if projectile fired by user collided with ItemFrame.
            if(entity instanceof ItemFrame && isProjectileSource && user != null)
            {
                if(!super.getPlugin().getProtectionManager().canInteractWithBlock(entity.serverLocation(), user, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }

            if(entity instanceof Player && source instanceof Entity)
            {
                final Entity sourceEntity = (Entity) source;

                if(sourceEntity.type().toString().contains("projectile"))
                {
                    if(this.protectionConfig.getSafeZoneWorldNames().contains(entity.serverLocation().worldKey().asString()))
                    {
                        sourceEntity.remove();
                        event.setCancelled(true);
                        return;
                    }

                    final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(entity.serverLocation().world().uniqueId(), entity.serverLocation().chunkPosition());
                    if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
                    {
                        sourceEntity.remove();
                        event.setCancelled(true);
                        return;
                    }

                    //TechGuns - Should be better to find more generic way of doing this...
                    //If sourceEntity = projectile that comes from techguns
                    if(sourceEntity.type().toString().contains("techguns"))
                    {
                        final Player player = (Player) entity;

                        //This code will break if techguns will change theirs code. Hope they won't.
                        final Class sourceEntityClass = sourceEntity.getClass();
                        try
                        {
                            Player shooterPlayer = null;
                            final Field[] fields = sourceEntityClass.getDeclaredFields();
                            for(Field field : fields)
                            {
                                if(field.getName().equals("shooter"))
                                {
                                    field.setAccessible(true);
                                    final Object playerObject = field.get(sourceEntity);
                                    if(playerObject instanceof Player)
                                    {
                                        shooterPlayer = (Player) playerObject;
                                    }
                                    field.setAccessible(false);
                                }
                            }

                            if(shooterPlayer != null)
                            {
                                //Crazy situation...
                                if(shooterPlayer == player)
                                    continue;

                                //We got shooter player
                                //Check friendly fire
                                final boolean isFactionFriendlyFireOn = factionsConfig.isFactionFriendlyFire();
                                final boolean isTruceFriendlyFireOn = factionsConfig.isTruceFriendlyFire();
                                final boolean isAllianceFriendlyFireOn = factionsConfig.isAllianceFriendlyFire();
                                if(isFactionFriendlyFireOn && isAllianceFriendlyFireOn && isTruceFriendlyFireOn)
                                    continue;

                                final Optional<Faction> optionalAffectedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
                                final Optional<Faction> optionalShooterPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(shooterPlayer.uniqueId());

                                if(optionalAffectedPlayerFaction.isPresent() && optionalShooterPlayerFaction.isPresent())
                                {
                                    final Faction affectedPlayerFaction = optionalAffectedPlayerFaction.get();
                                    final Faction shooterPlayerFaction = optionalShooterPlayerFaction.get();

                                    if(!isFactionFriendlyFireOn)
                                    {
                                        if(affectedPlayerFaction.getName().equals(shooterPlayerFaction.getName()))
                                        {
                                            sourceEntity.remove();
                                            event.setCancelled(true);
                                            return;
                                        }
                                    }

                                    if(!isTruceFriendlyFireOn)
                                    {
                                        if(affectedPlayerFaction.getTruces().contains(shooterPlayerFaction.getName()))
                                        {
                                            sourceEntity.remove();
                                            event.setCancelled(true);
                                            return;
                                        }
                                    }

                                    if(!isAllianceFriendlyFireOn)
                                    {
                                        if(affectedPlayerFaction.getAlliances().contains(shooterPlayerFaction.getName()))
                                        {
                                            sourceEntity.remove();
                                            event.setCancelled(true);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        catch(IllegalAccessException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if(event instanceof CollideEntityEvent.Impact)
            return;

        //Handle Item Frames
        Object rootCause = cause.root();
        if(!(rootCause instanceof ItemFrame))
            return;

        event.filterEntities(entity ->
        {
            if(entity instanceof Living)
            {
                if(entity instanceof User && !getPlugin().getProtectionManager().canInteractWithBlock(entity.serverLocation(), (User)entity, true).hasAccess())
                {
                    return false;
                }
            }
            return true;
        });
    }
}
