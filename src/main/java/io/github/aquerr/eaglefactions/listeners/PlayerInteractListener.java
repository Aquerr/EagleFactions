package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class PlayerInteractListener
{
    @Listener
    public void onPlayerInteract(HandInteractEvent event, @Root Player player)
    {
        if(!EagleFactions.AdminList.contains(player.getUniqueId()))
        {
            if(event.getInteractionPoint().isPresent())
            {
                World world = player.getWorld();
                Vector3d vector3d = event.getInteractionPoint().get();
                Location<World> location = new Location(world, vector3d);
                Vector3i claim = location.getChunkPosition();

                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
                Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), claim);

                if(optionalChunkFaction.isPresent())
                {
                    if(optionalChunkFaction.get().Name.equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                    {
                        return;
                    }
                    else if(optionalChunkFaction.get().Name.equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                    {
                        return;
                    }
                    else if(optionalPlayerFaction.isPresent() && optionalChunkFaction.get().Name.equals(optionalPlayerFaction.get().Name))
                    {
                        boolean canInteract = FlagManager.canInteract(player, optionalPlayerFaction.get(), optionalChunkFaction.get());
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
