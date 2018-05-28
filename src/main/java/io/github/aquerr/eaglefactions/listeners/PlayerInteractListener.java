package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
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

                if (MainLogic.getSafeZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                {
                    return;
                }
                if (MainLogic.getWarZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                {
                    return;
                }

                Vector3d vector3d = event.getInteractionPoint().get();
                Location location = new Location(world, vector3d);
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
                    else if (optionalPlayerFaction.isPresent())
                    {
                        if (!FlagManager.canInteract(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                            event.setCancelled(true);
                        }
                    }
                    else
                    {
                        event.setCancelled(true);
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
                        return;
                    }
                }
            }
        }
    }
}
