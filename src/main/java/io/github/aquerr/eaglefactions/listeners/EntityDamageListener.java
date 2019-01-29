package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class EntityDamageListener extends AbstractListener
{
    public EntityDamageListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onEntityDamage(DamageEntityEvent event)
    {
        if(event.getCause().root() instanceof DamageSource)
        {
                if(event.getTargetEntity().getType() == EntityTypes.PLAYER)
                {
                    Player attackedPlayer = (Player) event.getTargetEntity();
                    World world = attackedPlayer.getWorld();

                    if (getPlugin().getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()))
                    {
                        event.setBaseDamage(0);
                        event.setCancelled(true);
                        return;
                    }

                    //Block all damage an attacked player would get if location is a SafeZone.
                    Optional<Faction> chunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), attackedPlayer.getLocation().getChunkPosition());
                    if(chunkFaction.isPresent() && chunkFaction.get().getName().equals("SafeZone"))
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
                            Optional<Faction> playerChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), player.getLocation().getChunkPosition());
                            if(playerChunkFaction.isPresent() && playerChunkFaction.get().getName().equals("SafeZone"))
                            {
                                event.setBaseDamage(0);
                                event.setCancelled(true);
                                return;
                            }
                            else //If player is is not in a SafeZone.
                            {
                                //Check if player is in a faction.
                                Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                                if(optionalPlayerFaction.isPresent())
                                {
                                    //Check if attackedPlayer is in a faction.
                                    Optional<Faction> optionalAttackedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(attackedPlayer.getUniqueId());
                                    if(optionalAttackedPlayerFaction.isPresent())
                                    {
                                        //Check if players are in the same faction
                                        if(optionalPlayerFaction.get().getName().equals(optionalAttackedPlayerFaction.get().getName()))
                                        {
                                            //If friendlyfire is off the block the damage.
                                            if(!getPlugin().getConfiguration().getConfigFields().isFactionFriendlyFire())
                                            {
                                                event.setBaseDamage(0);
                                                event.setCancelled(true);
                                                return;
                                            }
                                            else
                                            {
                                                //If friendlyfire is on and damage will kill attackedPlayer then penalty the player.
                                                if(event.willCauseDeath())
                                                {
                                                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getPenalty()) + "\n",
                                                            TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));
                                                    getPlugin().getPowerManager().penalty(player.getUniqueId());
                                                    return;
                                                }
                                            }
                                        }//Check if players are in the alliance.
                                        else if(optionalPlayerFaction.get().getAlliances().contains(optionalAttackedPlayerFaction.get().getName()) && !getPlugin().getConfiguration().getConfigFields().isAllianceFriendlyFire())
                                        {
                                            event.setBaseDamage(0);
                                            event.setCancelled(true);
                                            return;
                                        }
                                        else if(optionalPlayerFaction.get().getAlliances().contains(optionalAttackedPlayerFaction.get().getName()) && getPlugin().getConfiguration().getConfigFields().isAllianceFriendlyFire())
                                        {
                                            if(EagleFactions.getPlugin().getPVPLogger().isActive()) EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                            if(event.willCauseDeath())
                                            {
                                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getPenalty()) + "\n",
                                                        TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));
                                                getPlugin().getPowerManager().penalty(player.getUniqueId());
                                                return;
                                            }
                                        }
                                        else
                                        {
                                            if(EagleFactions.getPlugin().getPVPLogger().isActive()) EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                            if(event.willCauseDeath())
                                            {
                                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getKillAward()) + "\n",
                                                        TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));
                                                getPlugin().getPowerManager().addPower(player.getUniqueId(), true);
                                                return;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        if(EagleFactions.getPlugin().getPVPLogger().isActive()) EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                        if(event.willCauseDeath())
                                        {
                                            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getKillAward()) + "\n",
                                                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));
                                            getPlugin().getPowerManager().addPower(player.getUniqueId(), true);
                                            return;
                                        }
                                    }
                                }
                                else
                                {
                                    if(EagleFactions.getPlugin().getPVPLogger().isActive()) EagleFactions.getPlugin().getPVPLogger().addOrUpdatePlayer(attackedPlayer);
                                    if(event.willCauseDeath())
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getKillAward()) + "\n",
                                                TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));
                                        getPlugin().getPowerManager().addPower(player.getUniqueId(), true);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}
