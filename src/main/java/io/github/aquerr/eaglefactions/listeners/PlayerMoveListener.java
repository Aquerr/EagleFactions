package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandExecutor;
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
                    if(PowerService.getFactionPower(FactionLogic.getFaction(playerFactionName)).doubleValue() >= FactionLogic.getClaims(playerFactionName).size())
                    {
                        if(FactionLogic.isClaimConnected(playerFactionName, chunk))
                        {
                            FactionLogic.addClaim(playerFactionName, chunk);

                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                            return;
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Claims needs to be connected!"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction does not have power to claim more land!"));
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
        if(EagleFactions.AutoMapList.contains(player.getUniqueId().toString()))
        {
            Sponge.getCommandManager().process(player, "f map");
        }
        return;
    }
}
