package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.world.World;

public class EntityDamageListener
{
    @Listener
    public void onEntityDamage(DamageEntityEvent event)
    {
        if(event.getCause().root() instanceof EntityDamageSource)
        {
            EntityDamageSource source = (EntityDamageSource)event.getCause().root();

                if(event.getTargetEntity().getType() == EntityTypes.PLAYER)
                {
                    Player attackedPlayer = (Player) event.getTargetEntity();
                    World world = attackedPlayer.getWorld();

                    if(FactionLogic.getFactionNameByChunk(world.getUniqueId(), attackedPlayer.getLocation().getChunkPosition()).equals("SafeZone"))
                    {
                        event.setBaseDamage(0);
                        event.setCancelled(true);
                        return;
                    }

                    if(source.getSource() instanceof Player)
                    {
                        Player player = (Player) source.getSource();

                        if(FactionLogic.getFactionNameByChunk(world.getUniqueId(), player.getLocation().getChunkPosition()).equals("SafeZone"))
                        {
                            event.setBaseDamage(0);
                            event.setCancelled(true);
                            return;
                        }
                        else
                        {
                            if(FactionLogic.getFactionName(player.getUniqueId()) != null)
                            {
                                //Check if players are in the same faction
                                if(FactionLogic.getFactionName(player.getUniqueId()) == FactionLogic.getFactionName(attackedPlayer.getUniqueId()))
                                {
                                    if(!FactionLogic.getFactionFriendlyFire(FactionLogic.getFactionName(player.getUniqueId())))
                                    {
                                        event.setBaseDamage(0);
                                        event.setCancelled(true);
                                        return;
                                    }
                                    else
                                    {
                                        if(event.willCauseDeath())
                                        {
                                            PowerService.punish(player.getUniqueId());
                                            return;
                                        }
                                    }
                                }//Check if players are in the alliance.
                                else if(FactionLogic.getAlliances(FactionLogic.getFactionName(player.getUniqueId())).contains(FactionLogic.getFactionName(attackedPlayer.getUniqueId())) && !MainLogic.getAllianceFriendlyFire())
                                {
                                    event.setBaseDamage(0);
                                    event.setCancelled(true);
                                    return;
                                }
                                else if(FactionLogic.getAlliances(FactionLogic.getFactionName(player.getUniqueId())).contains(FactionLogic.getFactionName(attackedPlayer.getUniqueId())) && MainLogic.getAllianceFriendlyFire())
                                {
                                    if(event.willCauseDeath())
                                    {
                                        PowerService.punish(player.getUniqueId());
                                        return;
                                    }
                                }
                            }
                            else
                            {
                                if(event.willCauseDeath())
                                {
                                    PowerService.addPower(player.getUniqueId(), true);
                                    return;
                                }
                            }
                        }
                    }
                }
                else
                {
                    if(source.getSource() instanceof Player)
                    {
                        Player player = (Player) source.getSource();
                        World world = player.getWorld();

                        if(FactionLogic.getFactionNameByChunk(world.getUniqueId(), player.getLocation().getChunkPosition()).equals("SafeZone"))
                        {
                            event.setBaseDamage(0);
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
        }
        return;
    }
}
