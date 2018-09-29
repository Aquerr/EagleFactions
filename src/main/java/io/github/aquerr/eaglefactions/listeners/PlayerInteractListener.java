package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.EagleFeather;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
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
    public void onHandInteract(InteractBlockEvent.Secondary event, @Root Player player)
    {
        if(!EagleFactions.AdminList.contains(player.getUniqueId()))
        {
            if(event.getInteractionPoint().isPresent() && event.getContext().containsKey(EventContextKeys.BLOCK_HIT))
            {
                Optional<Location<World>> optionalLocation = event.getContext().get(EventContextKeys.BLOCK_HIT).get().getLocation();
                if(optionalLocation.isPresent())
                {
                    //Check if player has Eagle's Feather
                    if(event.getInteractionPoint().isPresent()
                            && player.getWorld().getTileEntity(event.getTargetBlock().getPosition()).isPresent()
                            && player.getItemInHand(event.getHandType()).isPresent()
                            && player.getItemInHand(event.getHandType()).get().getType() == ItemTypes.FEATHER
                            && player.getItemInHand(event.getHandType()).get().get(Keys.DISPLAY_NAME).get().equals(EagleFeather.getDisplayName()))
                    {
                        ItemStack feather = player.getItemInHand(event.getHandType()).get();
                        feather.setQuantity(feather.getQuantity() - 1);
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.DARK_PURPLE, "You have used eagle's feather!"));
                        return;
                    }

                    if(!this.getPlugin().getProtectionManager().canInteract(optionalLocation.get(), player.getWorld(), player))
                        event.setCancelled(true);
                }
            }
            else if(event.getInteractionPoint().isPresent() && event.getContext().containsKey(EventContextKeys.ENTITY_HIT) && !(event.getContext().get(EventContextKeys.ENTITY_HIT).get() instanceof Living))
            {
                Location<World> entityLocation = event.getContext().get(EventContextKeys.ENTITY_HIT).get().getLocation();
                if(!this.getPlugin().getProtectionManager().canInteract(entityLocation, player.getWorld(), player))
                    event.setCancelled(true);
            }
        }
    }
}
