package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class PlayerInteractListener extends AbstractListener
{
    public PlayerInteractListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemUse(final InteractItemEvent event, @Root final Player player)
    {
        if (event.getItemStack() == ItemStackSnapshot.NONE)
            return;

        Optional<Vector3d> optionalInteractionPoint = event.getInteractionPoint();
        if (!optionalInteractionPoint.isPresent())
            return;

        Location<World> location = new Location<>(player.getWorld(), optionalInteractionPoint.get());

        boolean canUseItem = super.getPlugin().getProtectionManager().canUseItem(location, player, event.getItemStack());
        if (!canUseItem)
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockInteract(final InteractBlockEvent event, @Root final Player player)
    {
        //If AIR or NONE then return
        if (event.getTargetBlock() == BlockSnapshot.NONE || event.getTargetBlock().getState().getType() == BlockTypes.AIR)
            return;

        Optional<Location<World>> optionalLocation = event.getTargetBlock().getLocation();
        if (!optionalLocation.isPresent())
            return;

        Location<World> blockLocation = optionalLocation.get();

        boolean canInteractWithBlock = super.getPlugin().getProtectionManager().canInteractWithBlock(blockLocation, player);
        if (!canInteractWithBlock)
        {
            event.setCancelled(true);
            return;
        }
    }
}
