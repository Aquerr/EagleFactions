package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PlayerMoveListener
{
    @Listener
    public void onPlayerMove(MoveEntityEvent event, @Root Player player)
    {
        String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

        if(EagleFactions.AutoClaimList.contains(player.getUniqueId().toString()))
        {
            Vector3i chunk = player.getLocation().getChunkPosition();

            if(!FactionLogic.getClaims(playerFactionName).isEmpty())
            {
                if(!FactionLogic.isClaimed(chunk))
                {
                    if(FactionLogic.isClaimConnected(playerFactionName, chunk))
                    {
                        FactionLogic.addClaim(playerFactionName, chunk);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                        return;
                    }
                }
            }
            else
            {
                FactionLogic.addClaim(playerFactionName, chunk);
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                return;
            }

        }
        return;
    }
}
