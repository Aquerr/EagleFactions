package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class PlayerInteractListener extends AbstractListener
{
    public PlayerInteractListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onHandInteract(HandInteractEvent event, @Root Player player)
    {
        if(!EagleFactions.AdminList.contains(player.getUniqueId()))
        {
            if(event.getInteractionPoint().isPresent() && event.getContext().containsKey(EventContextKeys.BLOCK_HIT))
            {
                Optional<Location<World>> optionalLocation = event.getContext().get(EventContextKeys.BLOCK_HIT).get().getLocation();
                if(optionalLocation.isPresent())
                {
                    if(!this.getPlugin().getProtectionManager().canInteract(optionalLocation.get(), player.getWorld(), player))
                        event.setCancelled(true);
                }
            }
            else if((event.getInteractionPoint().isPresent() && event.getContext().containsKey(EventContextKeys.ENTITY_HIT)))
            {
                Location<World> entityLocation = event.getContext().get(EventContextKeys.ENTITY_HIT).get().getLocation();
                if(!this.getPlugin().getProtectionManager().canInteract(entityLocation, player.getWorld(), player))
                    event.setCancelled(true);
            }
        }
    }
}
