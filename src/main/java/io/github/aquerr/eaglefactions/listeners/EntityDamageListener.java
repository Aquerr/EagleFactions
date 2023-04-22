package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.util.ModSupport;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

public class EntityDamageListener extends AbstractListener
{
    private final PVPLogger pvpLogger;
    private final ProtectionConfig protectionConfig;
    private final FactionsConfig factionsConfig;
    private final PowerConfig powerConfig;
    private final ProtectionManager protectionManager;
    private final MessageService messageService;

    private final DamageModifier damageReductionModifier = DamageModifier.builder()
            .type(DamageModifierTypes.ARMOR)
            .cause(Cause.builder().append(super.getPlugin()).build(EventContext.builder().build()))
            .build();

    public EntityDamageListener(final EagleFactions plugin)
    {
        super(plugin);
        this.pvpLogger = plugin.getPVPLogger();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
        this.protectionManager = plugin.getProtectionManager();
        this.messageService = plugin.getMessageService();
    }

    //Method used for handling damaging entities like ArmorStands and Item Frames.
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamage(final DamageEntityEvent event)
    {
        final Entity targetEntity = event.entity();
        final Cause cause = event.cause();

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
            final Entity sourceEntity = indirectEntityDamageSource.indirectSource();
            if(sourceEntity instanceof ServerPlayer)
                user = ((ServerPlayer) sourceEntity).user();
        }

        if(user == null)
            return;

        if(!super.getPlugin().getProtectionManager().canInteractWithBlock(targetEntity.serverLocation(), user, true).hasAccess())
        {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onPlayerDamage(final DamageEntityEvent event, final @Getter(value = "entity") ServerPlayer attackedPlayer)
    {
        if(!(event.cause().root() instanceof DamageSource) || ((DamageSource)event.cause().root()).doesAffectCreative())
            return;

        final ServerWorld world = attackedPlayer.world();
        final ServerLocation location = attackedPlayer.serverLocation();

        //If it is safezone, protect the player from everything.
        if (isSafeZone(location))
        {
            event.setBaseDamage(0);
            event.setCancelled(true);
            world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.SMOKE).quantity(50).offset(new Vector3d(0.5, 1.5, 0.5)).build(), attackedPlayer.position());
            return;
        }

        //At this point we know that damage has NOT been dealt inside Safe Zone.

        final boolean willCauseDeath = event.willCauseDeath();
        final Object rootCause = event.cause().root();

        //Percentage damage reduction operator
        final DoubleUnaryOperator doubleUnaryOperator = operand ->
        {
            final double difference = operand * (this.factionsConfig.getPercentageDamageReductionInOwnTerritory() / 100);
            return -difference;
        };

        //Handle projectiles
        if(rootCause instanceof IndirectEntityDamageSource)
        {
            final IndirectEntityDamageSource indirectEntityDamageSource = (IndirectEntityDamageSource)rootCause;
            final Entity indirectSource = indirectEntityDamageSource.indirectSource();

            //If player attacked the player
            if(indirectSource instanceof ServerPlayer)
            {
                final boolean shouldBlockDamage = shouldBlockDamageFromPlayer(attackedPlayer, (ServerPlayer) indirectSource, willCauseDeath);
                if(shouldBlockDamage)
                {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    if(!(indirectEntityDamageSource.source() instanceof ServerPlayer))
                    {
                        indirectEntityDamageSource.source().remove();
                    }
                    world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.SMOKE).quantity(50).offset(new Vector3d(0.5, 1.5, 0.5)).build(), attackedPlayer.position());
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
            Entity entitySource = entityDamageSource.source();

            //TechGuns
            if (ModSupport.isTechGuns(entityDamageSource.getClass()))
            {
                final Entity entity = ModSupport.getAttackerFromTechGuns(entityDamageSource);
                if (entity != null)
                    entitySource = entity;
            }

            if(entitySource instanceof ServerPlayer)
            {
                final ServerPlayer player = (ServerPlayer) entitySource;
                final boolean shouldBlockDamage = shouldBlockDamageFromPlayer(attackedPlayer, player, willCauseDeath);
                if(shouldBlockDamage)
                {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                    world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.SMOKE).quantity(50).offset(new Vector3d(0.5, 1.5, 0.5)).build(), attackedPlayer.position());
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
            else // Player attacked by mob
            {
                if(isInOwnTerritory(attackedPlayer))
                {
                    event.addModifierAfter(damageReductionModifier, doubleUnaryOperator, new HashSet<>());
                }
            }
        }
    }

    private boolean shouldBlockDamageFromPlayer(final ServerPlayer attackedPlayer, final ServerPlayer sourcePlayer, boolean willCauseDeath)
    {
        final boolean canAttack = this.protectionManager.canHitEntity(attackedPlayer, sourcePlayer, false).hasAccess();
        if (!canAttack)
            return true;

        if (!willCauseDeath)
            return false;

        final Optional<Faction> attackedPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(attackedPlayer.uniqueId());
        final Optional<Faction> sourcePlayerFFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(sourcePlayer.uniqueId());

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
        final EventContext eventContext = event.context();
        final Entity entity = event.entity();
        final ServerWorld world = event.entity().serverLocation().world();

        //Only if ignited entity is player
        if(!(entity instanceof Player))
            return;

        //Check safezone world
        if(this.protectionConfig.getSafeZoneWorldNames().contains(world.properties().name()))
        {
            event.setCancelled(true);
            return;
        }

        final Player ignitedPlayer = (Player) entity;

        //Check if location is safezone
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), ignitedPlayer.serverLocation().chunkPosition());
        if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
        {
            event.setCancelled(true);
            return;
        }

        if(!eventContext.containsKey(EventContextKeys.PLAYER))
            return;

//        if(!cause.containsType(Player.class))
//            return;

        final Player igniterPlayer = eventContext.get(EventContextKeys.PLAYER).get();
        final boolean isFactionFriendlyFireOn = this.factionsConfig.isFactionFriendlyFire();
        final boolean isAllianceFriendlyFireOn = this.factionsConfig.isAllianceFriendlyFire();
        final boolean isTruceFriendlyFireOn = this.factionsConfig.isTruceFriendlyFire();

        if(isFactionFriendlyFireOn && isAllianceFriendlyFireOn && isTruceFriendlyFireOn)
            return;

        final Optional<Faction> optionalIgnitedPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(ignitedPlayer.uniqueId());
        final Optional<Faction> optionalIgniterPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(igniterPlayer.uniqueId());

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

    private void sendPenaltyMessageAndDecreasePower(final ServerPlayer player)
    {
        super.getPlugin().getPowerManager().penalty(player.uniqueId());
        player.sendMessage(messageService.resolveMessageWithPrefix("power.decreased-by", this.powerConfig.getPenalty()));
        player.sendMessage(messageService.resolveComponentWithMessage("power.current-power", super.getPlugin().getPowerManager().getPlayerPower(player.uniqueId()) + "/" + super.getPlugin().getPowerManager().getPlayerMaxPower(player.uniqueId())));
    }

    private void sendKillAwardMessageAndIncreasePower(final ServerPlayer player)
    {
        super.getPlugin().getPowerManager().addPower(player.uniqueId(), true);
        player.sendMessage(messageService.resolveMessageWithPrefix("power.increased-by", this.powerConfig.getKillAward()));
        player.sendMessage(messageService.resolveComponentWithMessage("power.current-power", super.getPlugin().getPowerManager().getPlayerPower(player.uniqueId()) + "/" + super.getPlugin().getPowerManager().getPlayerMaxPower(player.uniqueId())));
    }

    private boolean isInOwnTerritory(final ServerPlayer player)
    {
        final Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition());
        return optionalFaction.filter(x-> x.getPlayerMemberType(player.uniqueId()) != null).isPresent();
    }

    private boolean isSafeZone(final ServerLocation location)
    {
        return this.protectionManager.isSafeZone(location);
    }
}
