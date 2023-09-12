package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.List;

public class ChangeBlockEventListener extends AbstractListener
{
    public ChangeBlockEventListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPre(final ChangeBlockEvent.Pre event)
    {
        if (isTriggeredByCommandBlock(event))
            return;

        User user = getUserFromEvent(event).orElse(null);

        final LocatableBlock locatableBlock = event.cause().first(LocatableBlock.class).orElse(null);
        final BlockEntity tileEntity = event.cause().first(BlockEntity.class).orElse(null);
        final boolean pistonExtend = event.context().containsKey(EventContextKeys.PISTON_EXTEND);
        final boolean pistonRetract = event.context().containsKey(EventContextKeys.PISTON_RETRACT);
        final boolean isForgePlayerBreak = event.context().containsKey(EventContextKeys.PLAYER_BREAK);
        final ServerLocation tileEntityLocation = tileEntity != null ? tileEntity.serverLocation() : null;
        final ServerLocation sourceLocation = locatableBlock != null ? locatableBlock.serverLocation() : tileEntityLocation;

        printDebugMessageForUser(user, locatableBlock, sourceLocation, event);

        if (isForgePlayerBreak)
        {
            if (user instanceof Player)
            {
                for (ServerLocation location : event.locations())
                {
                    if (location.blockType() == BlockTypes.AIR.get())
                        continue;

                    if (!super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (sourceLocation != null)
        {
            List<ServerLocation> sourceLocations = event.locations();
            if (pistonExtend || pistonRetract)
            {
                sourceLocations = new ArrayList<>(event.locations());
                final ServerLocation location = sourceLocations.get(sourceLocations.size() - 1);
                final Direction direction = locatableBlock.serverLocation().block().get(Keys.DIRECTION).get();
                final ServerLocation directionLocation = location.relativeToBlock(direction);
                sourceLocations.add(directionLocation);
            }

            for (ServerLocation location : sourceLocations)
            {
                if (user != null && (pistonExtend || pistonRetract))
                {
                    if (!super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (user != null && !super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
                else if (user == null && !super.getPlugin().getProtectionManager().canBreak(location.createSnapshot()).hasAccess())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        else if (user != null)
        {
            for (ServerLocation location : event.locations())
            {
                if (pistonExtend)
                {
                    if (!super.getPlugin().getProtectionManager().canBreak(location.createSnapshot(), user, true).hasAccess())
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
