package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.FlagChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class PlayerInteractListener
{
    @Listener
    public void onPlayerInteract(HandInteractEvent event, @Root Player player)
    {
        if(!EagleFactions.AdminList.contains(player.getUniqueId()))
        {
            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(event.getInteractionPoint().isPresent())
            {
                World world = player.getWorld();
                Vector3d vector3d = event.getInteractionPoint().get();
                Location<World> location = new Location(world, vector3d);
                Vector3i claim = location.getChunkPosition();

                String chunkFactionName = FactionLogic.getFactionNameByChunk(world.getUniqueId(), claim);

                if(!chunkFactionName.equals(""))
                {
                    if(chunkFactionName.equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                    {
                        return;
                    }
                    else if(chunkFactionName.equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                    {
                        return;
                    }
                    else if(chunkFactionName.equals(playerFactionName))
                    {
                        boolean canInteract = FlagChecker.canInteract(player, playerFactionName, chunkFactionName);
                        if (!canInteract)
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, "You don't have privileges to interact here!"));
                            event.setCancelled(true);
                        }
                        return;
                    }
                    else
                    {
                        event.setCancelled(true);
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have access to do this!"));
                        return;
                    }
                }
            }
        }
    }
}
