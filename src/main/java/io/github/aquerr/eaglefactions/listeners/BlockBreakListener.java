package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
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
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class BlockBreakListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;

    public BlockBreakListener(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPre(final ChangeBlockEvent.Pre event)
    {
        if (event.cause().containsType(CommandBlock.class))
            return;

        User user = null;
        if(event.cause().containsType(ServerPlayer.class))
        {
            user = event.cause().first(ServerPlayer.class).get().user();
        }
        else if(event.cause().containsType(User.class))
        {
            user = event.cause().first(User.class).get();
        }

        final boolean isHandUsage = event.context().get(EventContextKeys.USED_HAND).isPresent();
        if (isHandUsage) // (Should be) Handled by interact event listeners
            return;

        final LocatableBlock locatableBlock = event.cause().first(LocatableBlock.class).orElse(null);
        final BlockEntity tileEntity = event.cause().first(BlockEntity.class).orElse(null);
        final boolean pistonExtend = event.context().containsKey(EventContextKeys.PISTON_EXTEND);
        final boolean pistonRetract = event.context().containsKey(EventContextKeys.PISTON_RETRACT);
        final boolean isLiquidSource = event.context().containsKey(EventContextKeys.LIQUID_FLOW)
                || (locatableBlock != null && (locatableBlock.blockState().type() == BlockTypes.WATER.get() || locatableBlock.blockState().type() == BlockTypes.LAVA.get()));
        final boolean isFireSource = !isLiquidSource && event.context().containsKey(EventContextKeys.FIRE_SPREAD);
        final boolean isLeafDecay = event.context().containsKey(EventContextKeys.LEAVES_DECAY);
        final boolean isForgePlayerBreak = event.context().containsKey(EventContextKeys.PLAYER_BREAK);
        final ServerLocation sourceLocation = locatableBlock != null ? locatableBlock.serverLocation() : tileEntity != null ? tileEntity.serverLocation() : null;

        if(user != null)
        {
            if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
            {
                ServerPlayer player = user.player().orElse(null);
                if (player != null)
                {
                    if(locatableBlock != null)
                    {
                        player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("LocatableBlock: ", BLUE), locatableBlock.blockState().type().asComponent().color(GOLD)));
                    }
                    if(sourceLocation != null)
                    {
                        player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("SourceBlock: ", BLUE), sourceLocation.blockType().asComponent().color(GOLD)));
                    }
                    player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("Event: ", BLUE), text(event.toString(), GOLD)));
                }
            }
        }

        if(isForgePlayerBreak)
        {
            // Helps with Ancient Warfare machines
            if (user == null)
                user = event.context().get(EventContextKeys.FAKE_PLAYER).map(ServerPlayer.class::cast)
                        .map(ServerPlayer::user)
                        .orElse(null);

            if(user instanceof Player)
            {
                for(ServerLocation location : event.locations())
                {
                    if(location.blockType() == BlockTypes.AIR.get())
                        continue;

                    if(!super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if(sourceLocation != null)
        {
            List<ServerLocation> sourceLocations = event.locations();
            if(pistonExtend || pistonRetract)
            {
                sourceLocations = new ArrayList<>(event.locations());
                final ServerLocation location = sourceLocations.get(sourceLocations.size() - 1);
                final Direction direction = locatableBlock.serverLocation().block().get(Keys.DIRECTION).get();
                final ServerLocation directionLocation = location.relativeToBlock(direction);
                sourceLocations.add(directionLocation);

                if (user == null)
                {
                    user = event.context().get(EventContextKeys.AUDIENCE)
                            .filter(ServerPlayer.class::isInstance)
                            .map(ServerPlayer.class::cast)
                            .map(ServerPlayer::user)
                            .orElse(null);
                }
            }

            for(ServerLocation location : sourceLocations)
            {
                if(user != null && (pistonExtend || pistonRetract))
                {
                    if(!super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isFireSource)
                {
                    final Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.world().uniqueId(), location.chunkPosition());
                    if(optionalChunkFaction.isPresent() && !optionalChunkFaction.get().getProtectionFlags().getValueForFlag(ProtectionFlagType.FIRE_SPREAD))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isLeafDecay)
                    continue;

                if(!isLiquidSource && location.block().type() == BlockTypes.AIR.get())
                    continue;

                if(user != null && !super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location.createSnapshot()).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        else if(user != null)
        {
            for(ServerLocation location : event.locations())
            {
                if(pistonExtend)
                {
                    if(!super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                    {
                        event.setCancelled(true);
                    }
                }

                if(isFireSource)
                {
                    Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.world().uniqueId(), location.chunkPosition());
                    if(this.protectionConfig.getSafeZoneWorldNames().contains(location.world().key().asString()) && !super.getPlugin().getFactionLogic().getFactionByName("SafeZone").getProtectionFlags().getValueForFlag(ProtectionFlagType.FIRE_SPREAD))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if ((optionalChunkFaction.isPresent()) && !optionalChunkFaction.get().getProtectionFlags().getValueForFlag(ProtectionFlagType.FIRE_SPREAD))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockCollide(final CollideBlockEvent event)
    {
        if(event instanceof CollideBlockEvent.Impact)
            return;

        if(event.source() instanceof FallingBlock)
            return;

        User user = null;
        final Cause cause = event.cause();
        final EventContext context = event.context();
//        if (cause.root() instanceof BlockEntity) {
            user = context.get(EventContextKeys.PLAYER)
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .map(ServerPlayer::user)
                                    .orElse(null);

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living instanceof User) {
                    user = (User) living;
                }
            }
        }

        if(user == null)
            return;

        final BlockType blockType = event.targetBlock().type();
        if(blockType.equals(BlockTypes.AIR.get()))
            return;

        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.uniqueId());
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(event.targetLocation().world().uniqueId(), event.targetLocation().chunkPosition());

        if(optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent())
        {
            if(!optionalChunkFaction.get().getName().equalsIgnoreCase(optionalPlayerFaction.get().getName()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onProjectileImpactBlock(final CollideBlockEvent.Impact event)
    {
        if(!(event.source() instanceof Entity))
            return;

        User user = null;
        final Cause cause = event.cause();
        final EventContext context = event.context();
            user = context.get(EventContextKeys.PLAYER)
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .map(ServerPlayer::user)
                                    .orElse(null);

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living instanceof User) {
                    user = (User) living;
                }
            }
        }

        if(user == null)
            return;

        ServerLocation impactPoint = event.impactPoint();
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(impactPoint.world().uniqueId(), impactPoint.chunkPosition());

        if(!optionalChunkFaction.isPresent())
            return;

        Faction chunkFaction = optionalChunkFaction.get();
        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.uniqueId());
        if(!optionalPlayerFaction.isPresent())
        {
            //Special case for pixelmon... we should consider adding a configurable list in the config file.
            if (StringUtils.containsIgnoreCase(event.cause().root().getClass().getName(), "Pokeball"))
                return;

            event.setCancelled(true);
            return;
        }

        Faction playerFaction = optionalPlayerFaction.get();
        if(playerFaction.getName().equalsIgnoreCase(chunkFaction.getName()))
            return;
        else
        {
            //Special case for pixelmon... we should consider adding a configurable list in the config file.
            if (StringUtils.containsIgnoreCase(event.cause().root().getClass().getName(), "Pokeball"))
                return;

            event.setCancelled(true);
            return;
        }
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
