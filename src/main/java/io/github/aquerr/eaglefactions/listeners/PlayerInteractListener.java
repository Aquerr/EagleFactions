package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
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
    public void onPlayerInteract(InteractBlockEvent event, @Root Player player)
    {
//        if(event.getTargetBlock().getState().getType() == BlockTypes.FURNACE)
//            return;

//       if(event.getContext().containsKey(EventContextKeys.BLOCK_HIT))
//       {
//           Optional<BlockSnapshot> blockType = event.getContext().get(EventContextKeys.BLOCK_HIT);
//           if(blockType.isPresent() && blockType.get().getState().getType() == BlockTypes.FURNACE)
//               return;
//       }

        if(!EagleFactions.AdminList.contains(player.getUniqueId()))
        {
            if(event.getInteractionPoint().isPresent())
            {
                World world = player.getWorld();

                if (getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                {
                    return;
                }
                if (getPlugin().getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                {
                    return;
                }

                Vector3d vector3d = event.getInteractionPoint().get();
                Location location = new Location<>(world, vector3d);
                Vector3i claim = location.getChunkPosition();

                Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), claim);

                if(optionalChunkFaction.isPresent())
                {
                    if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                    {
                        return;
                    }
                    else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                    {
                        return;
                    }
                    else if (optionalPlayerFaction.isPresent())
                    {
                        if (!getPlugin().getFlagManager().canInteract(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                            event.setCancelled(true);
                        }
                    }
                    else
                    {
                        event.setCancelled(true);
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
                    }
                }
            }
        }
    }
}
