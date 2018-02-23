package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

public class EntityDamageListener
{
    @Listener
    public void onEntityDamage(DamageEntityEvent event)
    {
        if(event.getCause().root() instanceof DamageSource)
        {
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

                    if (event.getCause().root() instanceof EntityDamageSource)
                    {
                        EntityDamageSource entityDamageSource = (EntityDamageSource) event.getCause().root();

                        if(entityDamageSource.getSource() instanceof Player)
                        {
                            Player player = (Player) entityDamageSource.getSource();

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
                                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Your power has been decreased by ", TextColors.GOLD, String.valueOf(MainLogic.getPunishment()) + "\n",
                                                        TextColors.GRAY, "Current power: ", String.valueOf(PowerService.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerService.getPlayerMaxPower(player.getUniqueId()))));
                                                PowerService.punish(player.getUniqueId());
                                                return;
                                            }
                                        }
                                    }//Check if players are in the alliance.
                                    else if(FactionLogic.getAlliances(FactionLogic.getFactionName(player.getUniqueId())).contains(FactionLogic.getFactionName(attackedPlayer.getUniqueId())) && !MainLogic.isAllianceFriendlyFire())
                                    {
                                        event.setBaseDamage(0);
                                        event.setCancelled(true);
                                        return;
                                    }
                                    else if(FactionLogic.getAlliances(FactionLogic.getFactionName(player.getUniqueId())).contains(FactionLogic.getFactionName(attackedPlayer.getUniqueId())) && MainLogic.isAllianceFriendlyFire())
                                    {
                                        PVPLogger.addOrUpdatePlayer(attackedPlayer);
                                        if(event.willCauseDeath())
                                        {
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Your power has been decreased by ", TextColors.GOLD, String.valueOf(MainLogic.getPunishment()) + "\n",
                                                    TextColors.GRAY, "Current power: ", String.valueOf(PowerService.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerService.getPlayerMaxPower(player.getUniqueId()))));
                                            PowerService.punish(player.getUniqueId());
                                            return;
                                        }
                                    }
                                    else
                                    {
                                        PVPLogger.addOrUpdatePlayer(attackedPlayer);
                                        if(event.willCauseDeath())
                                        {
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Your power has been increased by ", TextColors.GOLD, String.valueOf(MainLogic.getKillAward()) + "\n",
                                                    TextColors.GRAY, "Current power: ", String.valueOf(PowerService.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerService.getPlayerMaxPower(player.getUniqueId()))));
                                            PowerService.addPower(player.getUniqueId(), true);
                                            return;
                                        }
                                    }
                                }
                                else
                                {
                                    PVPLogger.addOrUpdatePlayer(attackedPlayer);
                                    if(event.willCauseDeath())
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Your power has been increased by ", TextColors.GOLD, String.valueOf(MainLogic.getKillAward()) + "\n",
                                                TextColors.GRAY, "Current power: ", String.valueOf(PowerService.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerService.getPlayerMaxPower(player.getUniqueId()))));
                                        PowerService.addPower(player.getUniqueId(), true);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
        }
        return;
    }
}
