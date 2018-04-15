package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
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

public class BlockBreakListener
{
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
                     Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();

                     String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

                     String chunkFactionName = FactionLogic.getFactionNameByChunk(world.getUniqueId(), claim);

                     if(!chunkFactionName.equals(""))
                     {
                         if(chunkFactionName.equals("SafeZone") && player.hasPermission("eaglefactions.safezone.build"))
                         {
                             return;
                         }
                         else if(chunkFactionName.equals("WarZone") && player.hasPermission("eaglefactions.warzone.build"))
                         {
                             return;
                         }
                         else if(chunkFactionName.equals(playerFactionName))
                         {
                             boolean canBreakBlock = FlagManager.canBreakBlock(player, playerFactionName, chunkFactionName);
                             if (!canBreakBlock)
                             {
                                 player.sendMessage(Text.of(PluginInfo.ErrorPrefix, "You don't have privileges to destroy blocks here!"));
                                 event.setCancelled(true);
                             }
                             return;
                         }
                         else
                         {
                             event.setCancelled(true);
                             player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This land belongs to someone else!"));
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
                Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();

                String factionName = FactionLogic.getFactionNameByChunk(world.getUniqueId(), claim);

                if (!factionName.equals(""))
                {
                    if(!factionName.equals("SafeZone") && !factionName.equals("WarZone") && MainLogic.isBlockDestroyingDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if(factionName.equals("SafeZone"))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if (factionName.equals("WarZone") && MainLogic.isBlockDestroyingInWarZoneDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

    }

}
