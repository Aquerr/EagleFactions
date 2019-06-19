package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
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
        if(event instanceof InteractBlockEvent)
            return;

        if (event.getItemStack() == ItemStackSnapshot.NONE)
            return;

        final Optional<Vector3d> optionalInteractionPoint = event.getInteractionPoint();
        if (!optionalInteractionPoint.isPresent())
            return;

        Location<World> location = new Location<>(player.getWorld(), optionalInteractionPoint.get());

        //Handle hitting entities
        boolean hasHitEntity = event.getContext().containsKey(EventContextKeys.ENTITY_HIT);
        if(hasHitEntity)
        {
            Entity hitEntity = event.getContext().get(EventContextKeys.ENTITY_HIT).get();
            location = hitEntity.getLocation();
        }

        boolean canUseItem = super.getPlugin().getProtectionManager().canUseItem(location, player, event.getItemStack());
        if (!canUseItem)
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityInteract(final InteractEntityEvent event, @Root final Player player)
    {
        final Entity targetEntity = event.getTargetEntity();
        final Optional<Vector3d> optionalInteractionPoint = event.getInteractionPoint();

        if(targetEntity instanceof Living)
            return;

        if(!optionalInteractionPoint.isPresent())
            return;

        final Location<World> location = new Location<>(targetEntity.getWorld(), optionalInteractionPoint.get());
        boolean canInteractWithEntity = super.getPlugin().getProtectionManager().canInteractWithBlock(location, player);
        if(!canInteractWithEntity)
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

        final Optional<Location<World>> optionalLocation = event.getTargetBlock().getLocation();
        if (!optionalLocation.isPresent())
            return;

        final Location<World> blockLocation = optionalLocation.get();

        boolean canInteractWithBlock = super.getPlugin().getProtectionManager().canInteractWithBlock(blockLocation, player);
        if (!canInteractWithBlock)
        {
            event.setCancelled(true);
            return;
        }
    }
}
