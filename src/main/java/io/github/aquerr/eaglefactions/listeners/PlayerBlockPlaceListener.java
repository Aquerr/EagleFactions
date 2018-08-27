package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class PlayerBlockPlaceListener extends AbstractListener
{
    public PlayerBlockPlaceListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player)
    {
        if(!EagleFactions.AdminList.contains(player.getUniqueId()))
        {
            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
             {
                 World world = player.getWorld();

                 if (getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                 {
                     return;
                 }
                 if (getPlugin().getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                 {
                     return;
                 }

                 Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();
                 Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), claim);

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
                         if (!getPlugin().getFlagManager().canPlaceBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                         {
                             player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                             event.setCancelled(true);
                         }
                         return;
                     }
                     else
                     {
                         event.setCancelled(true);
                         player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                     }
                 }
             }
        }
    }

}
