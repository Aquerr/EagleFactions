package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

public class EntityDamageListener
{
    @Listener
    public void onEntityDamage(DamageEntityEvent event)
    {
        if(event.getCause().root() instanceof DamageSource)
        {
            DamageSource source = (DamageSource) event.getCause().root();

                if(event.getTargetEntity().getType() == EntityTypes.PLAYER)
                {
                    Player attackedPlayer = (Player) event.getTargetEntity();
                    World world = attackedPlayer.getWorld();

                    //Block all damage an attacked player would get if location is a SafeZone.
                    if(FactionLogic.getFactionNameByChunk(world.getUniqueId(), attackedPlayer.getLocation().getChunkPosition()).equals("SafeZone"))
                    {
                        event.setBaseDamage(0);
                        event.setCancelled(true);
                        return;
                    }

                    if(source instanceof Player)
                    {
                        Player player = (Player) source;

                        //Block all damage a player could deal if location is SafeZone.
                        if(FactionLogic.getFactionNameByChunk(world.getUniqueId(), player.getLocation().getChunkPosition()).equals("SafeZone"))
                        {
                            event.setBaseDamage(0);
                            event.setCancelled(true);
                            return;
                        }
                        else //If player is is not in a SafeZone.
                        {
                            //Check if player is in a faction.
                            if(FactionLogic.getFactionName(player.getUniqueId()) != null)
                            {
                                //Check if players are in the same faction
                                if(FactionLogic.getFactionName(player.getUniqueId()).equals(FactionLogic.getFactionName(attackedPlayer.getUniqueId())))
                                {
                                    //If friendlyfire is off the block the damage.
                                    if(!FactionLogic.getFactionFriendlyFire(FactionLogic.getFactionName(player.getUniqueId())))
                                    {
                                        event.setBaseDamage(0);
                                        event.setCancelled(true);
                                        return;
                                    }
                                    else
                                    {
                                        //If friendlyfire is on and damage will kill attackedPlayer then punish the player.
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
                                else
                                {
                                    if(event.willCauseDeath())
                                    {
                                        PowerService.addPower(player.getUniqueId(), true);
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
        }
        return;
    }
}
