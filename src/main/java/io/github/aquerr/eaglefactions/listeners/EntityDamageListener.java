package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class EntityDamageListener extends AbstractListener
{
    private final PVPLogger pvpLogger;

    public EntityDamageListener(final EagleFactions plugin)
    {
        super(plugin);
        this.pvpLogger = plugin.getPVPLogger();
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onEntityDamage(final DamageEntityEvent event)
    {
        if(!(event.getCause().root() instanceof DamageSource))
            return;

        if(event.getTargetEntity().getType() != EntityTypes.PLAYER)
            return;

        final Player attackedPlayer = (Player) event.getTargetEntity();
        final World world = attackedPlayer.getWorld();
        final boolean willCauseDeath = event.willCauseDeath();

        if(super.getPlugin().getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()))
        {
            event.setBaseDamage(0);
            event.setCancelled(true);
            return;
        }

        //Block all damage an attacked player would get if location is a SafeZone.
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), attackedPlayer.getLocation().getChunkPosition());
        if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equals("SafeZone"))
        {
            event.setBaseDamage(0);
            event.setCancelled(true);
            return;
        }

        final Object rootCause = event.getCause().root();

        //Handle projectiles
        if(rootCause instanceof IndirectEntityDamageSource)
        {
            final IndirectEntityDamageSource indirectEntityDamageSource = (IndirectEntityDamageSource)rootCause;
            final Entity indirectSource = indirectEntityDamageSource.getIndirectSource();

            //If player attacked the player
            if(indirectSource instanceof Player)
            {
                final boolean shouldBlockDamage = shouldBlockDamageFromPlayer(attackedPlayer, (Player) indirectSource, willCauseDeath);
                if(shouldBlockDamage)
                {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    indirectEntityDamageSource.getSource().remove();
                    return;
                }
            }
        }
        else if (rootCause instanceof EntityDamageSource)
        {
            //Handle damage from other entities
            EntityDamageSource entityDamageSource = (EntityDamageSource) rootCause;
            if(entityDamageSource.getSource() instanceof Player)
            {
                final Player player = (Player) entityDamageSource.getSource();
                final boolean shouldBlockDamage = shouldBlockDamageFromPlayer(attackedPlayer, player, willCauseDeath);
                if(shouldBlockDamage)
                {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

//    @Listener
//    public void onAttack(AttackEntityEvent event)
//    {
//        Entity entity = event.getTargetEntity();
//        Cause cause = event.getCause();
//        Object source = event.getSource();
//        EventContext eventContext = event.getContext();
//    }

    private boolean shouldBlockDamageFromPlayer(final Player attackedPlayer, final Player sourcePlayer, boolean willCauseDeath)
    {
        //Block all damage a player could deal if location is SafeZone.
        final World world = attackedPlayer.getWorld();

        final Optional<Faction> playerChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), sourcePlayer.getLocation().getChunkPosition());
        if(playerChunkFaction.isPresent() && playerChunkFaction.get().getName().equals("SafeZone"))
            return true;

        //Check if player is not in a faction.
        final Optional<Faction> optionalSourcePlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(sourcePlayer.getUniqueId());
        if(!optionalSourcePlayerFaction.isPresent())
        {
            if(willCauseDeath)
            {
                sendKillAwardMessageAndIncreasePower(sourcePlayer);
                return false;
            }

            if(this.pvpLogger.isActive())
            {
                this.pvpLogger.addOrUpdatePlayer(attackedPlayer);
            }

            return false;
        }

        final Faction sourcePlayerFaction = optionalSourcePlayerFaction.get();

        final Optional<Faction> optionalAttackedPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(attackedPlayer.getUniqueId());
        //Check if attackedPlayer is not in a faction.
        if(!optionalAttackedPlayerFaction.isPresent())
        {
            if(willCauseDeath)
            {
                sendKillAwardMessageAndIncreasePower(sourcePlayer);
                return false;
            }

            if(this.pvpLogger.isActive())
            {
                this.pvpLogger.addOrUpdatePlayer(attackedPlayer);
            }

            return false;
        }

        final Faction attackedPlayerFaction = optionalAttackedPlayerFaction.get();

        //Check if players are in the same faction
        if(sourcePlayerFaction.getName().equals(attackedPlayerFaction.getName()))
        {
            //If friendlyfire is off then block the damage.
            if(!super.getPlugin().getConfiguration().getConfigFields().isFactionFriendlyFire())
            {
                return true;
            }
            else
            {
                //If friendlyfire is on and damage will kill attackedPlayer then punish the player.
                if(willCauseDeath)
                {
                    sendPenaltyMessageAndDecreasePower(sourcePlayer);
                    return false;
                }
            }
        }//Check if players are in the alliance.
        else if(sourcePlayerFaction.getAlliances().contains(attackedPlayerFaction.getName()))
        {
            if(!super.getPlugin().getConfiguration().getConfigFields().isAllianceFriendlyFire())
            {
                return true;
            }
            else
            {
                if(willCauseDeath)
                {
                    sendPenaltyMessageAndDecreasePower(sourcePlayer);
                    return false;
                }
                if(this.pvpLogger.isActive())
                {
                    this.pvpLogger.addOrUpdatePlayer(attackedPlayer);
                }
                return false;
            }
        }
        else //Different factions
        {
            if(willCauseDeath)
            {
                sendKillAwardMessageAndIncreasePower(sourcePlayer);
                return false;
            }
            if(this.pvpLogger.isActive())
            {
                this.pvpLogger.addOrUpdatePlayer(attackedPlayer);
            }
            return false;
        }
        return false;
    }

    private void sendPenaltyMessageAndDecreasePower(Player player)
    {
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getPenalty()) + "\n",
                TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));
        super.getPlugin().getPowerManager().penalty(player.getUniqueId());
    }

    private void sendKillAwardMessageAndIncreasePower(Player player)
    {
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_INCREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getKillAward()) + "\n",
                TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));
        super.getPlugin().getPowerManager().addPower(player.getUniqueId(), true);
    }
}
