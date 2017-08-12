package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class PlayerInteractListener
{
    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @Root Player player)
    {
        String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

        Optional<Location<World>> location = event.getTargetBlock().getLocation();

        if(location.isPresent())
        {
            Vector3i claim = location.get().getChunkPosition();

            if(FactionLogic.isClaimed(claim))
            {
                if(FactionLogic.getFactionNameByChunk(claim).equals(playerFactionName))
                {
                    return;
                }
                else
                {
                    event.setCancelled(true);
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have access to do this!"));
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
