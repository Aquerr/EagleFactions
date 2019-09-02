package io.github.aquerr.eaglefactions.common.listeners;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
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
                    if(!(indirectEntityDamageSource.getSource() instanceof Player))
                    {
                        indirectEntityDamageSource.getSource().remove();
                    }
                    world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.SMOKE).quantity(50).offset(new Vector3d(0.5, 1.5, 0.5)).build(), attackedPlayer.getPosition());
                    return;
                }
            }
        }
        else if (rootCause instanceof EntityDamageSource)
        {
            //Handle damage from other entities
            final EntityDamageSource entityDamageSource = (EntityDamageSource) rootCause;
            Entity entitySource = entityDamageSource.getSource();

            //Try closure for TechGuns
            if(entityDamageSource.getClass().getName().contains("techguns"))
            {
                try
                {
                    //Reflection
                    final Object attacker = entityDamageSource.getClass().getField("attacker").get(entityDamageSource);
                    if(attacker instanceof Player)
                    {
                        //We got attacker
                        entitySource = (Player)attacker;
                    }
                }
                catch(NoSuchFieldException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }

            if(entitySource instanceof Player)
            {
                final Player player = (Player) entitySource;
                final boolean shouldBlockDamage = shouldBlockDamageFromPlayer(attackedPlayer, player, willCauseDeath);
                if(shouldBlockDamage)
                {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.SMOKE).quantity(50).offset(new Vector3d(0.5, 1.5, 0.5)).build(), attackedPlayer.getPosition());
                    return;
                }
            }
        }
    }

    private boolean shouldBlockDamageFromPlayer(final Player attackedPlayer, final Player sourcePlayer, boolean willCauseDeath)
    {
        //Block all damage a player could deal if location is SafeZone.
        final World world = attackedPlayer.getWorld();

        final Optional<Faction> playerChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), sourcePlayer.getLocation().getChunkPosition());
        if(playerChunkFaction.isPresent() && playerChunkFaction.get().getName().equals("SafeZone"))
            return true;

        //If player attacked herself/himself
        if(attackedPlayer.equals(sourcePlayer))
            return false;

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

    @Listener
    public void onIgniteEntity(final IgniteEntityEvent event)
    {
        final ConfigFields configFields = super.getPlugin().getConfiguration().getConfigFields();
        final EventContext eventContext = event.getContext();
        final Entity entity = event.getTargetEntity();
        final World world = event.getTargetEntity().getWorld();

        //Only if ignited entity is player
        if(!(entity instanceof Player))
            return;

        //Check safezone world
        if(configFields.getSafeZoneWorldNames().contains(world.getName()))
        {
            event.setCancelled(true);
            return;
        }

        final Player ignitedPlayer = (Player) entity;

        //Check if location is safezone
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), ignitedPlayer.getLocation().getChunkPosition());
        if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone"))
        {
            event.setCancelled(true);
            return;
        }

        if(!eventContext.containsKey(EventContextKeys.OWNER) || !(eventContext.get(EventContextKeys.OWNER).get() instanceof Player))
            return;

//        if(!cause.containsType(Player.class))
//            return;

        final Player igniterPlayer = (Player) eventContext.get(EventContextKeys.OWNER).get();
        final boolean isFactionFriendlyFireOn = configFields.isFactionFriendlyFire();
        final boolean isAllianceFriendlyFireOn = configFields.isAllianceFriendlyFire();

        if(isFactionFriendlyFireOn && isAllianceFriendlyFireOn)
            return;

        final Optional<Faction> optionalIgnitedPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(ignitedPlayer.getUniqueId());
        final Optional<Faction> optionalIgniterPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(igniterPlayer.getUniqueId());

        if(optionalIgnitedPlayerFaction.isPresent() && optionalIgniterPlayerFaction.isPresent())
        {
            final Faction ignitedPlayerFaction = optionalIgnitedPlayerFaction.get();
            final Faction igniterPlayerFaction = optionalIgniterPlayerFaction.get();

            if(!isFactionFriendlyFireOn)
            {
                if(ignitedPlayerFaction.getName().equals(igniterPlayerFaction.getName()))
                {
                    event.setCancelled(true);
                    return;
                }
            }

            if(!isAllianceFriendlyFireOn)
            {
                if(ignitedPlayerFaction.getAlliances().contains(igniterPlayerFaction.getName()))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
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
