package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class BlockBreakListener extends AbstractListener
{
    public BlockBreakListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event)
    {
        if(event.getCause().root() instanceof Player)
        {
            Player player = (Player)event.getCause().root();

            if(!EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                 for (Transaction<BlockSnapshot> transaction : event.getTransactions())
                 {
                     World world = player.getWorld();

                     if (MainLogic.getSafeZoneWorldNames().contains(world.getName()))
                     {
                         event.setCancelled(true);
                         return;
                     }
                     else if (MainLogic.getWarZoneWorldNames().contains(world.getName()) && MainLogic.isBlockDestroyingInWarZoneDisabled())
                     {
                         event.setCancelled(true);
                         return;
                     }

                     Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();

                     Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

                     Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), claim);

                     if(optionalChunkFaction.isPresent())
                     {
                         if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                         {
                             return;
                         }
                         else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                         {
                             return;
                         }
                         else if(optionalPlayerFaction.isPresent())
                         {
                             if (!plugin.getFlagManager().canBreakBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                             {
                                 player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                                 event.setCancelled(true);
                                 return;
                             }
                         }
                         else
                         {
                             event.setCancelled(true);
                             player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                             return;
                         }
                     }
                 }
            }
        }
        else
        {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                World world = transaction.getFinal().getLocation().get().getExtent();

                if (MainLogic.getSafeZoneWorldNames().contains(world.getName()))
                {
                    event.setCancelled(true);
                    return;
                }
                else if (MainLogic.getWarZoneWorldNames().contains(world.getName()) && MainLogic.isBlockDestroyingInWarZoneDisabled())
                {
                    event.setCancelled(true);
                    return;
                }

                Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();
                Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), claim);

                if (optionalChunkFaction.isPresent())
                {
                    if(!optionalChunkFaction.get().getName().equals("SafeZone") && !optionalChunkFaction.get().getName().equals("WarZone") && MainLogic.isBlockDestroyingDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if(optionalChunkFaction.get().getName().equals("SafeZone"))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if (optionalChunkFaction.get().getName().equals("WarZone") && MainLogic.isBlockDestroyingInWarZoneDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        return;
    }

}
