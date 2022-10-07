package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.block.entity.Piston;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
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
    private final FactionLogic factionLogic;
    private final ProtectionManager protectionManager;
    private final ProtectionConfig protectionConfig;

    public BlockPlaceListener(EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.protectionManager = plugin.getProtectionManager();
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

            for (BlockTransaction transaction : event.transactions())
            {
                ServerLocation serverLocation = transaction.finalReplacement().location().orElse(null);
                if (transaction.operation() == Operations.PLACE.get())
                {
                    if(!this.protectionManager.canPlace(transaction.finalReplacement(), user, true).hasAccess())
                    {
                        event.setCancelled(true);
                        break;
                    }
                }
                else if (transaction.operation() == Operations.MODIFY.get() && event.context().get(EventContextKeys.USED_ITEM).isPresent() && serverLocation != null)
                {
                    if (!this.protectionManager.canUseItem(serverLocation, user, event.context().get(EventContextKeys.USED_ITEM).get(), true).hasAccess())
                    {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
        else
        {
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
                if (!this.protectionManager.canNotifyBlock(sourceNotifyLocation.get(), sourceLocation.get()).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }

            for (BlockTransaction transaction : event.transactions())
            {
                if (transaction.operation() == Operations.PLACE.get())
                {
                    //Block fire from thunder
                    if(transaction.finalReplacement().state().type() == BlockTypes.FIRE.get() && getFactionAtBlock(transaction.finalReplacement()).isPresent())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    private Optional<Faction> getFactionAtBlock(BlockSnapshot blockSnapshot)
    {
        ServerLocation blockLocation = blockSnapshot.location().orElse(null);
        if (blockLocation == null)
            return Optional.empty();
        return this.factionLogic.getFactionByChunk(blockLocation.world().uniqueId(), blockLocation.chunkPosition());
    }
}
