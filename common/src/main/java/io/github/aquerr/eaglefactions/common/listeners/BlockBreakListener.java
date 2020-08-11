package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

        final LocatableBlock locatableBlock = event.getCause().first(LocatableBlock.class).orElse(null);
        final TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
        final boolean pistonExtend = event.getContext().containsKey(EventContextKeys.PISTON_EXTEND);
        final boolean pistonRetract = event.getContext().containsKey(EventContextKeys.PISTON_RETRACT);
        final boolean isLiquidSource = event.getContext().containsKey(EventContextKeys.LIQUID_FLOW)
                || (locatableBlock != null && (locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_WATER || locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_LAVA));
        final boolean isFireSource = !isLiquidSource && event.getContext().containsKey(EventContextKeys.FIRE_SPREAD);
        final boolean isLeafDecay = event.getContext().containsKey(EventContextKeys.LEAVES_DECAY);
        final boolean isForgePlayerBreak = event.getContext().containsKey(EventContextKeys.PLAYER_BREAK);
        final Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;

        if(user instanceof Player)
        {
            if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
            {
                Player player = (Player)user;
                if(locatableBlock != null)
                {
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "LocatableBlock: ", TextColors.GOLD, locatableBlock.getBlockState().getType().getName())));
                }
                if(sourceLocation != null)
                {
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "SourceBlock: ", TextColors.GOLD, sourceLocation.getBlockType().getName())));
                }
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "isForgePlayerBreak: ", TextColors.GOLD, isForgePlayerBreak)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "EventContext: ", TextColors.GOLD, event.getContext())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Cause: ", TextColors.GOLD, event.getCause())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Event: ", TextColors.GOLD, event)));
            }
        }

        if(isForgePlayerBreak)
        {
            //Helps blocking mining laser from IC2
            if(user == null)
            {
                user = event.getContext().get(EventContextKeys.OWNER)
                        .orElse(event.getContext().get(EventContextKeys.NOTIFIER).orElse(null));

                if(user != null)
                {
                    if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
                    {
                        Player player = (Player)user;
                        if(locatableBlock != null)
                        {
                            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "LocatableBlock: ", TextColors.GOLD, locatableBlock.getBlockState().getType().getName())));
                        }
                        if(sourceLocation != null)
                        {
                            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "SourceBlock: ", TextColors.GOLD, sourceLocation.getBlockType().getName())));
                        }
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "isForgePlayerBreak: ", TextColors.GOLD, isForgePlayerBreak)));
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "EventContext: ", TextColors.GOLD, event.getContext())));
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Cause: ", TextColors.GOLD, event.getCause())));
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Event: ", TextColors.GOLD, event)));
                    }
                }
            }

            // Helps with Ancient Warfare machines
            if (user == null)
                user = event.getContext().get(EventContextKeys.FAKE_PLAYER).orElse(null);

            if(user instanceof Player)
            {
                for(Location<World> location : event.getLocations())
                {
                    if(location.getBlockType() == BlockTypes.AIR)
                        continue;

                    if(!super.getPlugin().getProtectionManager().canBreak(location, user, true).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if(sourceLocation != null)
        {
            List<Location<World>> sourceLocations = event.getLocations();
            if(pistonExtend || pistonRetract)
            {
                sourceLocations = new ArrayList<>(event.getLocations());
                final Location<World> location = sourceLocations.get(sourceLocations.size() - 1);
                final Direction direction = locatableBlock.getLocation().getBlock().get(Keys.DIRECTION).get();
                final Location<World> directionLocation = location.getBlockRelative(direction);
                sourceLocations.add(directionLocation);

                if (user == null)
                {
                    user = event.getContext().get(EventContextKeys.OWNER).orElse(null);
                }
            }

            if (user == null)
            {
                user = event.getContext().get(EventContextKeys.OWNER)
                        .orElse(event.getContext().get(EventContextKeys.NOTIFIER).orElse(null));
            }

            for(Location<World> location : sourceLocations)
            {
                if(user != null && (pistonExtend || pistonRetract))
                {
                    if(!super.getPlugin().getProtectionManager().canBreak(location, user, true).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isFireSource)
                {
                    final Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
                    if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

//                if(isLiquidSource)
//                    continue;

                if(isLeafDecay)
                    continue;

                if(!isLiquidSource && location.getBlock().getType() == BlockTypes.AIR)
                    continue;

                if(user != null && !super.getPlugin().getProtectionManager().canBreak(location, user, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
                else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        else if(user != null)
        {
            for(Location<World> location : event.getLocations())
            {
                if(pistonExtend)
                {
                    if(!super.getPlugin().getProtectionManager().canBreak(location, user, true).hasAccess())
                    {
                        event.setCancelled(true);
                    }
                }

                if(isFireSource)
                {
                    Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
                    if(this.protectionConfig.getSafeZoneWorldNames().contains(location.getExtent().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone")))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isLiquidSource)
                    continue;

                if(isLeafDecay)
                    continue;

                //TODO: This runs even when player right clicks the block.
                if(!super.getPlugin().getProtectionManager().canBreak(location, user, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(final ChangeBlockEvent.Break event)
    {
        //SpawnType in break event? Should be located in Pre or global event in my opinion.
        //Custom Spawn Type = Can be a piston moving block or possibly any other "magically" spawned block.
        final boolean isCustomSpawnType = event.getContext().get(EventContextKeys.SPAWN_TYPE).isPresent() && event.getContext().get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.CUSTOM;

        if(event instanceof ExplosionEvent || event.getCause().containsType(Explosion.class))
            return;

        // I guess "placement" in "break" event is something weird?
        final boolean isPlacementSpawnType = event.getContext().get(EventContextKeys.SPAWN_TYPE).isPresent() && event.getContext().get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.PLACEMENT;
        if (isPlacementSpawnType)
            return;

        final Object source = event.getSource();

        //For ICBM
        //Missles and grenades should be handled by explosion listener.
        if(source instanceof Entity)
        {
            final Entity entity = (Entity)source;
            final String id = entity.getType().getId();
            final String name = entity.getType().getName();
            if(id.startsWith("icbmclassic:missile") || id.startsWith("icbmclassic:grenade") || name.contains("missile") || name.contains("grenade"))
                return;
        }

        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

        //Helps blocking dynamite from IC2
        if(user == null)
        {
            user = event.getContext().get(EventContextKeys.OWNER).orElse(null);
        }

        LocatableBlock locatableBlock = null;
        if(event.getSource() instanceof LocatableBlock)
        {
            locatableBlock = (LocatableBlock) event.getSource();
        }
        if(locatableBlock != null)
        {
            if(locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_WATER || locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_LAVA)
                return;

            Optional<Faction> optionalSourceChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(locatableBlock.getLocation().getExtent().getUniqueId(), locatableBlock.getLocation().getChunkPosition());
            if(!optionalSourceChunkFaction.isPresent())
                return;
        }

        if(isCustomSpawnType)
            return;

        for(Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            final Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if(location == null || transaction.getOriginal().getState().getType() == BlockTypes.AIR
                    || transaction.getOriginal().getState().getType() == BlockTypes.FLOWING_WATER
                    || transaction.getOriginal().getState().getType() == BlockTypes.FLOWING_LAVA)
            {
                continue;
            }

            if(user != null && !super.getPlugin().getProtectionManager().canBreak(location, user, true).hasAccess())
            {
                event.setCancelled(true);
                return;
            }
            else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location).hasAccess())
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockCollide(final CollideBlockEvent event)
    {
        if(event instanceof CollideBlockEvent.Impact)
            return;

        if(event.getSource() instanceof FallingBlock)
            return;

        User user = null;
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        if (cause.root() instanceof TileEntity) {
            user = context.get(EventContextKeys.OWNER)
                    .orElse(context.get(EventContextKeys.NOTIFIER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        } else {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

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

        final BlockType blockType = event.getTargetBlock().getType();
        if(blockType.equals(BlockTypes.AIR))
            return;

        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(event.getTargetLocation().getExtent().getUniqueId(), event.getTargetLocation().getChunkPosition());

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
        if(!(event.getSource() instanceof Entity))
            return;

        User user = null;
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        if (cause.root() instanceof TileEntity) {
            user = context.get(EventContextKeys.OWNER)
                    .orElse(context.get(EventContextKeys.NOTIFIER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        } else {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

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

        Location<World> impactPoint = event.getImpactPoint();
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(impactPoint.getExtent().getUniqueId(), impactPoint.getChunkPosition());

        if(!optionalChunkFaction.isPresent())
            return;

        Faction chunkFaction = optionalChunkFaction.get();
        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(!optionalPlayerFaction.isPresent())
        {
            event.setCancelled(true);
            return;
        }

        Faction playerFaction = optionalPlayerFaction.get();
        if(playerFaction.getName().equalsIgnoreCase(chunkFaction.getName()))
            return;
        else
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityCollideEntity(final CollideEntityEvent event)
    {
        final List<Entity> entityList = event.getEntities();
        final EventContext eventContext = event.getContext();
        final Cause cause = event.getCause();
        final Object source = event.getSource();
        User user = null;
        boolean isProjectileSource = eventContext.containsKey(EventContextKeys.PROJECTILE_SOURCE);

        if(isProjectileSource)
        {
            final ProjectileSource projectileSource = eventContext.get(EventContextKeys.PROJECTILE_SOURCE).get();
            if(projectileSource instanceof Player)
            {
                user = (Player)projectileSource;
            }
        }

        for(final Entity entity : entityList)
        {
            //Check if projectile fired by user collided with ItemFrame.
            if(entity instanceof ItemFrame && isProjectileSource && user != null)
            {
                if(!super.getPlugin().getProtectionManager().canInteractWithBlock(entity.getLocation(), user, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }

            if(entity instanceof Player && source instanceof Entity)
            {
                final Entity sourceEntity = (Entity) source;

                if(sourceEntity.getType().getName().contains("projectile"))
                {
                    if(this.protectionConfig.getSafeZoneWorldNames().contains(entity.getWorld().getName()))
                    {
                        sourceEntity.remove();
                        event.setCancelled(true);
                        return;
                    }

                    final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition());
                    if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().isSafeZone())
                    {
                        sourceEntity.remove();
                        event.setCancelled(true);
                        return;
                    }

                    //TechGuns - Should be better to find more generic way of doing this...
                    //If sourceEntity = projectile that comes from techguns
                    if(sourceEntity.getType().getId().contains("techguns"))
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

                                final Optional<Faction> optionalAffectedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                                final Optional<Faction> optionalShooterPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(shooterPlayer.getUniqueId());

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
                if(entity instanceof User && !getPlugin().getProtectionManager().canInteractWithBlock(entity.getLocation(), (User)entity, true).hasAccess())
                {
                    return false;
                }
            }
            return true;
        });
    }
}
