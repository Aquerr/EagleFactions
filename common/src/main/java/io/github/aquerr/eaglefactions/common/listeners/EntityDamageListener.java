package io.github.aquerr.eaglefactions.common.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

public class EntityDamageListener extends AbstractListener
{
    private final PVPLogger pvpLogger;
    private final ProtectionConfig protectionConfig;
    private final FactionsConfig factionsConfig;
    private final PowerConfig powerConfig;
    private final ProtectionManager protectionManager;

    public EntityDamageListener(final EagleFactions plugin)
    {
        super(plugin);
        this.pvpLogger = plugin.getPVPLogger();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
        this.protectionManager = plugin.getProtectionManager();
    }

    //Method used for handling damaging entities like ArmorStands and Item Frames.
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamage(final DamageEntityEvent event)
    {
        final Entity targetEntity = event.getTargetEntity();
        final Cause cause = event.getCause();

        //Handle damaging player in separate method.
        if(targetEntity instanceof Player)
            return;

        //It should be possible to attack living mobs always. Maybe we will provide a config for it in the future but not now.
        if((targetEntity instanceof Living) && !(targetEntity instanceof ArmorStand))
            return;

        User user = null;

        if(cause.root() instanceof IndirectEntityDamageSource)
        {
            IndirectEntityDamageSource indirectEntityDamageSource = (IndirectEntityDamageSource) cause.root();
            final Entity sourceEntity = indirectEntityDamageSource.getIndirectSource();
            if(sourceEntity instanceof Player)
                user = (Player) sourceEntity;
        }

        if(user == null)
            return;

        if(!super.getPlugin().getProtectionManager().canInteractWithBlock(targetEntity.getLocation(), user, true))
        {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onPlayerDamage(final DamageEntityEvent event, final @Getter(value = "getTargetEntity") Player attackedPlayer)
    {
        if(!(event.getCause().root() instanceof DamageSource))
            return;

        final World world = attackedPlayer.getWorld();
        final boolean willCauseDeath = event.willCauseDeath();

        final Object rootCause = event.getCause().root();

        //Percentage damage reduction operator
        final DoubleUnaryOperator doubleUnaryOperator = operand ->
        {
            final double difference = operand * (this.factionsConfig.getPercentageDamageReductionInOwnTerritory() / 100);
            return -difference;
        };
        final DamageModifier damageReductionModifier = DamageModifier.builder()
                .type(DamageModifierTypes.ARMOR)
                .cause(
                        Cause.builder()
                                .append(super.getPlugin())
                                .build(EventContext.builder().build()))
                .build();

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
                }
                else
                {
                    this.pvpLogger.addOrUpdatePlayer(attackedPlayer);
                    if (isInOwnTerritory(attackedPlayer))
                    {
                        event.addModifierAfter(damageReductionModifier, doubleUnaryOperator, new HashSet<>());
                    }
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
                }
                else
                {
                    this.pvpLogger.addOrUpdatePlayer(attackedPlayer);
                    if (isInOwnTerritory(attackedPlayer))
                    {
                        event.addModifierAfter(damageReductionModifier, doubleUnaryOperator, new HashSet<>());
                    }
                }
            }
            else
            {
                final Optional<Faction> optionalAttackedChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(attackedPlayer.getWorld().getUniqueId(), attackedPlayer.getLocation().getChunkPosition());
                if(this.protectionConfig.getSafeZoneWorldNames().contains(attackedPlayer.getWorld().getName()))
                {
                    event.setCancelled(true);
                    return;
                }
                else if (optionalAttackedChunkFaction.isPresent() && optionalAttackedChunkFaction.get().isSafeZone())
                {
                    event.setCancelled(true);
                    return;
                }

                if (isInOwnTerritory(attackedPlayer))
                {
                    event.addModifierAfter(damageReductionModifier, doubleUnaryOperator, new HashSet<>());
                }
            }
        }
    }

    private boolean shouldBlockDamageFromPlayer(final Player attackedPlayer, final Player sourcePlayer, boolean willCauseDeath)
    {
        final boolean canAttack = this.protectionManager.canAttackEntity(attackedPlayer, sourcePlayer, false);
        if (!canAttack)
            return true;

        if (!willCauseDeath)
            return false;

        final Optional<Faction> attackedPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(attackedPlayer.getUniqueId());
        final Optional<Faction> sourcePlayerFFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(sourcePlayer.getUniqueId());

        if (attackedPlayerFaction.isPresent() && sourcePlayerFFaction.isPresent())
        {
            final Faction attackedFaction = attackedPlayerFaction.get();
            final Faction sourceFaction = sourcePlayerFFaction.get();
            if (attackedFaction.equals(sourceFaction))
            {
                sendPenaltyMessageAndDecreasePower(sourcePlayer);
            }
            else if (attackedFaction.isTruce(sourceFaction))
            {
                sendPenaltyMessageAndDecreasePower(sourcePlayer);
            }
            else if (attackedFaction.isAlly(sourceFaction))
            {
                sendPenaltyMessageAndDecreasePower(sourcePlayer);
            }
            else
            {
                sendKillAwardMessageAndIncreasePower(sourcePlayer);
            }
        }
        else
        {
            sendKillAwardMessageAndIncreasePower(sourcePlayer);
        }

        return false;
    }

    @Listener
    public void onIgniteEntity(final IgniteEntityEvent event)
    {
        final EventContext eventContext = event.getContext();
        final Entity entity = event.getTargetEntity();
        final World world = event.getTargetEntity().getWorld();

        //Only if ignited entity is player
        if(!(entity instanceof Player))
            return;

        //Check safezone world
        if(this.protectionConfig.getSafeZoneWorldNames().contains(world.getName()))
        {
            event.setCancelled(true);
            return;
        }

        final Player ignitedPlayer = (Player) entity;

        //Check if location is safezone
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), ignitedPlayer.getLocation().getChunkPosition());
        if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
        {
            event.setCancelled(true);
            return;
        }

        if(!eventContext.containsKey(EventContextKeys.OWNER) || !(eventContext.get(EventContextKeys.OWNER).get() instanceof Player))
            return;

//        if(!cause.containsType(Player.class))
//            return;

        final Player igniterPlayer = (Player) eventContext.get(EventContextKeys.OWNER).get();
        final boolean isFactionFriendlyFireOn = this.factionsConfig.isFactionFriendlyFire();
        final boolean isAllianceFriendlyFireOn = this.factionsConfig.isAllianceFriendlyFire();
        final boolean isTruceFriendlyFireOn = this.factionsConfig.isTruceFriendlyFire();

        if(isFactionFriendlyFireOn && isAllianceFriendlyFireOn && isTruceFriendlyFireOn)
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

            if(!isTruceFriendlyFireOn)
            {
                if(ignitedPlayerFaction.getTruces().contains(igniterPlayerFaction.getName()))
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

    private void sendPenaltyMessageAndDecreasePower(final Player player)
    {
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader
                        .parseMessage(Messages.YOUR_POWER_HAS_BEEN_DECREASED_BY, ImmutableMap.of(Placeholders.NUMBER, this.powerConfig.getPenalty())) + "\n",
                TextColors.GRAY, Messages.CURRENT_POWER + " ", super.getPlugin().getPowerManager().getPlayerPower(player.getUniqueId()) + "/" + getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId())));
        super.getPlugin().getPowerManager().penalty(player.getUniqueId());
    }

    private void sendKillAwardMessageAndIncreasePower(final Player player)
    {
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOUR_POWER_HAS_BEEN_INCREASED_BY, ImmutableMap.of(Placeholders.NUMBER, this.powerConfig.getKillAward())) + "\n",
                TextColors.GRAY, Messages.CURRENT_POWER + " ", super.getPlugin().getPowerManager().getPlayerPower(player.getUniqueId()) + "/" + getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId())));
        super.getPlugin().getPowerManager().addPower(player.getUniqueId(), true);
    }

    private boolean isInOwnTerritory(final Player player)
    {
        final Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        return optionalFaction.filter(x-> x.getPlayerMemberType(player.getUniqueId()) != null).isPresent();
    }
}
