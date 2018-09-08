package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

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
            World world = player.getWorld();

            if (getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                event.setCancelled(true);
                return;
            }
            if (getPlugin().getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                event.setCancelled(true);
                return;
            }

            if(event.getInteractionPoint().isPresent() && event.getContext().containsKey(EventContextKeys.BLOCK_HIT))
            {
                Optional<Location<World>> optionalLocation = event.getContext().get(EventContextKeys.BLOCK_HIT).get().getLocation();
                if(optionalLocation.isPresent())
                {
                    if(!canInteract(world.getUniqueId(), optionalLocation.get(), player))
                        event.setCancelled(true);
                }
            }
            else if((event.getInteractionPoint().isPresent() && event.getContext().containsKey(EventContextKeys.ENTITY_HIT)))
            {
                Location<World> entityLocation = event.getContext().get(EventContextKeys.ENTITY_HIT).get().getLocation();
                if(!canInteract(world.getUniqueId(), entityLocation, player))
                    event.setCancelled(true);
            }
        }
    }

    private boolean canInteract(UUID worldUUID, Location location, Player player)
    {
        Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(worldUUID, location.getChunkPosition());
        Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
            {
                return true;
            }
            else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
            {
                return true;
            }
            else if (optionalPlayerFaction.isPresent())
            {
                if (!getPlugin().getFlagManager().canInteract(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                    return false;
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
                return false;
            }
        }
        return true;
    }
}
