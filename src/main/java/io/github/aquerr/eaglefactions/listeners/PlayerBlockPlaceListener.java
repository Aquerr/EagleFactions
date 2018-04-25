package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class PlayerBlockPlaceListener
{
    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player)
    {
        if(!EagleFactions.AdminList.contains(player.getUniqueId()))
        {
            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
             {
                 World world = player.getWorld();
                 Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();
                 Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), claim);

                 if(optionalChunkFaction.isPresent())
                 {
                     if(optionalChunkFaction.get().Name.equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                     {
                         return;
                     }
                     else if(optionalChunkFaction.get().Name.equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                     {
                         return;
                     }
                     else if(optionalPlayerFaction.isPresent() && optionalChunkFaction.get().Name.equals(optionalPlayerFaction.get().Name))
                     {
                         boolean canPlaceBlock = FlagManager.canPlaceBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get());
                         if (!canPlaceBlock)
                         {
                             player.sendMessage(Text.of(PluginInfo.ErrorPrefix, "You don't have privileges to place blocks here!"));
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
        return;
    }

}
