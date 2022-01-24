package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.block.entity.Piston;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public class BlockPlaceListener extends AbstractListener
{
    private final ProtectionConfig protectionConfig;

    public BlockPlaceListener(EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.EARLY)
    public void onBlockPlace(ChangeBlockEvent.All event)
    {
        if (event.cause().containsType(CommandBlock.class))
            return;

        final Object source = event.source();
        if(source instanceof Piston)
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

        if(user != null)
        {
            //Requested for sand/tnt cannons by Turner
            if(source instanceof FallingBlock && this.protectionConfig.shouldAllowExplosionsByOtherPlayersInClaims())
                return;

            for (Transaction<BlockSnapshot> transaction : event.transactions())
            {
                if(!super.getPlugin().getProtectionManager().canPlace(transaction.finalReplacement().location().get(), user, true).hasAccess())
                {
                    event.setCancelled(true);
                    break;
                }
            }
        }
        else
        {
//            final boolean pistonExtend = event.getContext().containsKey(EventContextKeys.PISTON_EXTEND);
//            final boolean pistonRetract = event.getContext().containsKey(EventContextKeys.PISTON_RETRACT);
//
//            final List<BlockSnapshot> sourceBlockSnapshots = event.getTransactions().stream().map(Transaction::getOriginal).collect(Collectors.toList());
//            if(pistonExtend || pistonRetract)
//            {
//                final BlockSnapshot blockSnapshot = sourceBlockSnapshots.get(sourceBlockSnapshots.size() - 1);
//                final Direction direction = blockSnapshot.get(Keys.DIRECTION).get();
//                final Location<World> directionLocation = location.getBlockRelative(direction);
//                sourceLocations.add(directionLocation);
//
//                if (user == null)
//                {
//                    user = event.getContext().get(EventContextKeys.OWNER).orElse(null);
//                }
//            }

            final Optional<BlockSnapshot> optionalNeighborNotifySource = event.context().get(EventContextKeys.NEIGHBOR_NOTIFY_SOURCE);
            if (optionalNeighborNotifySource.isPresent())
            {
                final BlockSnapshot blockSnapshot = optionalNeighborNotifySource.get();
                if(!(source instanceof BlockSnapshot))
                    return;
                final Optional<ServerLocation> sourceNotifyLocation = blockSnapshot.location();
                final Optional<ServerLocation> sourceLocation = ((BlockSnapshot) source).location();
                if (!sourceNotifyLocation.isPresent() || !sourceLocation.isPresent())
                    return;
                if (!super.getPlugin().getProtectionManager().canNotifyBlock(sourceNotifyLocation.get(), sourceLocation.get()).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
//                if (!super.getPlugin().getProtectionManager().canBreak(blockSnapshot.getLocation().get()))
//                {
//                    event.setCancelled(true);
//                    return;
//                }
            }

            for (Transaction<BlockSnapshot> transaction : event.transactions())
            {
                //Block fire from thunder
                if(transaction.finalReplacement().state().type() == BlockTypes.FIRE && super.getPlugin().getFactionLogic().getFactionByChunk(transaction.finalReplacement().location().get().world().uniqueId(), transaction.finalReplacement().location().get().chunkPosition()).isPresent())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
