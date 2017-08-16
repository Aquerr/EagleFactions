package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
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
        //Set player location
        if(!PlayerService.getPlayerChunkPosition(player.getUniqueId()).toString().equals(player.getLocation().getChunkPosition().toString()))
        {
            //Check if player has tuned on AutoClaim
            if(EagleFactions.AutoClaimList.contains(player.getUniqueId().toString()))
            {
                Sponge.getCommandManager().process(player, "f claim");
            }

            //Check if player has turned on AutoMap
            if(EagleFactions.AutoMapList.contains(player.getUniqueId().toString()))
            {
                Sponge.getCommandManager().process(player, "f map");
            }


            //Set new player chunk location.
            PlayerService.setPlayerChunkPosition(player.getUniqueId(), player.getLocation().getChunkPosition());
        }
        return;
    }
}
