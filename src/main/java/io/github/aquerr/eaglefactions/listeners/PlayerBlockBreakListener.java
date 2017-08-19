package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

public class PlayerBlockBreakListener
{
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player)
    {
        String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

        for (Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            World world = player.getWorld();
            Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();

            if(FactionLogic.isClaimed(world.getUniqueId(), claim))
            {
                if(!FactionLogic.getFactionNameByChunk(world.getUniqueId(), claim).equals(playerFactionName))
                {
                    event.setCancelled(true);
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This land belongs to someone else!"));
                    return;
                }
                else
                {
                    return;
                }
            }
            else
            {
                return;
            }
        }
    }

}
