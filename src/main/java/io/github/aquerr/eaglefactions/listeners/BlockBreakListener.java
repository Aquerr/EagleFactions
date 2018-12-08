package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockBreakListener extends AbstractListener
{
    public BlockBreakListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onBlockBreak(ChangeBlockEvent.Pre event)
    {
//        if(event.getContext().containsKey(EventContextKeys.PLAYER_BREAK) || event.getContext().containsKey(EventContextKeys.FIRE_SPREAD))
//        {
//            List<Location<World>> locationList = new ArrayList<>(event.getLocations());
//            for(Location<World> location : locationList)
//            {
//                BlockState blockState = location.getBlock();
//                if(blockState.getType() == BlockTypes.FLOWING_WATER)
//                {
//                    return;
//                }
//
//                if(event.getContext().containsKey(EventContextKeys.OWNER)
//                        && event.getContext().get(EventContextKeys.OWNER).isPresent()
//                        && event.getContext().get(EventContextKeys.OWNER).get() instanceof Player)
//                {
//
//                    Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
//                    World world = player.getWorld();
//
//                    if(!super.getPlugin().getProtectionManager().canBreak(location, world, player))
//                        event.setCancelled(true);
//                }
//                else
//                {
//                    if(blockState.getType() == BlockTypes.FLOWING_WATER)
//                    {
//                        return;
//                    }
//
//                    if(!super.getPlugin().getProtectionManager().canBreak(location, location.getExtent()))
//                        event.setCancelled(true);
//                }
//            }
//        }
    }

//    @Listener(order = Order.EARLY)
//    public void onBlockBreak(ChangeBlockEvent.Break event)
//    {
//        User user = null;
//        if(event.getCause().containsType(Player.class))
//        {
//            user = event.getCause().first(Player.class).get();
//        }
//        else if(event.getCause().containsType(User.class))
//        {
//            user = event.getCause().first(User.class).get();
//        }
//
//        if(user instanceof Player)
//        {
//            for(Transaction<BlockSnapshot> transaction : event.getTransactions())
//            {
//                if(super.getPlugin().getProtectionManager().isBlockWhitelistedForPlaceDestroy(transaction.getOriginal().getState().getType()))
//                    return;
//
//                if(!super.getPlugin().getProtectionManager().canBreak(transaction.getFinal().getLocation().get(), transaction.getFinal().getLocation().get().getExtent(), (Player) user))
//                    event.setCancelled(true);
//            }
//        }
//        else
//        {
//            if(event.getContext().get(EventContextKeys.SPAWN_TYPE).isPresent())
//                return;
//
//            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
//            {
//                if(transaction.getOriginal().getState().getType() == BlockTypes.FLOWING_WATER)
//                {
//                    return;
//                }
//
//                if(!super.getPlugin().getProtectionManager().canBreak(transaction.getFinal().getLocation().get(), transaction.getFinal().getLocation().get().getExtent()))
//                    event.setCancelled(true);
//            }
//        }
//    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPre(ChangeBlockEvent.Pre event)
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
        final boolean isLiquidSource = event.getContext().containsKey(EventContextKeys.LIQUID_FLOW);
        final boolean isFireSource = !isLiquidSource && event.getContext().containsKey(EventContextKeys.FIRE_SPREAD);
        final boolean isLeafDecay = event.getContext().containsKey(EventContextKeys.LEAVES_DECAY);
        final boolean isForgePlayerBreak = event.getContext().containsKey(EventContextKeys.PLAYER_BREAK);
        Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;

        if(isForgePlayerBreak && user instanceof Player)
        {
            for(Location<World> location : event.getLocations())
            {
                if(location.getBlockType() == BlockTypes.AIR)
                    continue;

                if(!super.getPlugin().getProtectionManager().canBreak(location, location.getExtent(), user))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if(sourceLocation != null)
        {
            List<Location<World>> sourceLocations = event.getLocations();
            if(pistonExtend)
            {
                sourceLocations = new ArrayList<>(event.getLocations());
                Location<World> location = sourceLocations.get(sourceLocations.size() - 1);
                final Direction direction = locatableBlock.getLocation().getBlock().get(Keys.DIRECTION).get();
                final Location<World> directionLocation = location.getBlockRelative(direction);
                sourceLocations.add(directionLocation);
            }
            for(Location<World> location : sourceLocations)
            {
                if(user != null && pistonExtend)
                {
                    if(!super.getPlugin().getProtectionManager().canInteract(location, location.getExtent(), user))
                    {
                        event.setCancelled(true);
                        return;
                    }
//                    Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
//                    Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
//                    if(optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent())
//                    {
//                        if(super.getPlugin().getFlagManager().canInteract(user.getUniqueId(), optionalPlayerFaction.get(), optionalChunkFaction.get()))
//                        {
//                            event.setCancelled(true);
//                        }
//                    }
                }

                if(isFireSource)
                {
                    Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
                    if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone"))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isLiquidSource)
                    continue;

                if(isLeafDecay)
                    continue;

                if(user != null && !super.getPlugin().getProtectionManager().canBreak(location, location.getExtent(), user.getPlayer().get()))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location, location.getExtent()))
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
                    if(!super.getPlugin().getProtectionManager().canInteract(location, location.getExtent(), user))
                    {
                        event.setCancelled(true);
                    }
                }

                if(isFireSource)
                {
                    Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
                    if(super.getPlugin().getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(location.getExtent().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone")))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isLiquidSource)
                    continue;

                if(isLeafDecay)
                    continue;

                if(!super.getPlugin().getProtectionManager().canBreak(location, location.getExtent(), user.getPlayer().get()))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(ChangeBlockEvent.Break event)
    {
        if(event instanceof ExplosionEvent)
            return;

        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
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

        for(Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if(location == null || transaction.getOriginal().getState().getType() == BlockTypes.AIR)
            {
                continue;
            }

            if(user != null && !super.getPlugin().getProtectionManager().canBreak(location, location.getExtent(), user))
            {
                event.setCancelled(true);
                return;
            }
            else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location, location.getExtent()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockNotify(NotifyNeighborBlockEvent event)
    {
//        LocatableBlock locatableBlock = event.getCause().first(LocatableBlock.class).orElse(null);
//        TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
//        Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;
//        Optional<Faction> optionalChunkFaction = null;
//
//        User user = null;
//        final Cause cause = event.getCause();
//        final EventContext context = event.getContext();
//        if (user == null) {
//            // Always use owner for ticking TE's
//            // See issue MinecraftPortCentral/GriefPrevention#610 for more information
//            if (cause.root() instanceof TileEntity) {
//                user = context.get(EventContextKeys.OWNER)
//                        .orElse(context.get(EventContextKeys.NOTIFIER)
//                                .orElse(context.get(EventContextKeys.CREATOR)
//                                        .orElse(null)));
//            } else {
//                user = context.get(EventContextKeys.NOTIFIER)
//                        .orElse(context.get(EventContextKeys.OWNER)
//                                .orElse(context.get(EventContextKeys.CREATOR)
//                                        .orElse(null)));
//            }
//        }
//
//        if (user == null) {
//            if (event instanceof ExplosionEvent) {
//                // Check igniter
//                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
//                if (living != null && living instanceof User) {
//                    user = (User) living;
//                }
//            }
//        }
//
//        if(user == null)
//            return;
//
//        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
//
//        if(sourceLocation == null)
//        {
//            Player player = event.getCause().first(Player.class).orElse(null);
//            if(player == null)
//                return;
//
//            sourceLocation = player.getLocation();
//            optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(sourceLocation.getExtent().getUniqueId(), sourceLocation.getChunkPosition());
//        }
//        else
//        {
//            optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(sourceLocation.getExtent().getUniqueId(), sourceLocation.getChunkPosition());
//        }

//        Iterator<Direction> directionIterator = event.getNeighbors().keySet().iterator();
//        while(directionIterator.hasNext())
//        {
//            Direction direction = directionIterator.next();
//            Location<World> location = sourceLocation.getBlockRelative(direction);
//            Vector3i chunkPosition = location.getChunkPosition();
//            optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), chunkPosition);
//            if(optionalChunkFaction.isPresent())
//            {
//                if(optionalPlayerFaction.isPresent() && optionalPlayerFaction.get().getName().equalsIgnoreCase(optionalChunkFaction.get().getName()))
//                {
//                    continue;
//                }
//            }
//            else
//            {
//                continue;
//            }
//
//            directionIterator.remove();
//        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockCollide(CollideBlockEvent event)
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
    public void onProjectileImpactBlock(CollideBlockEvent.Impact event)
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
}
