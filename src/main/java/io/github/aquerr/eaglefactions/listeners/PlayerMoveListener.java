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
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class PlayerMoveListener
{
    @Listener
    public void onPlayerMove(MoveEntityEvent event, @Root Player player)
    {
        //Check if player changed chunk.
        if(!PlayerService.getPlayerChunkPosition(player.getUniqueId()).toString().equals(player.getLocation().getChunkPosition().toString()))
        {
            World world = player.getWorld();
            Vector3i oldChunk = PlayerService.getPlayerChunkPosition(player.getUniqueId());
            Vector3i newChunk = player.getLocation().getChunkPosition();

            //Inform a player about entering faction's land.
            if(!FactionLogic.getFactionNameByChunk(world.getUniqueId(), oldChunk).equals(FactionLogic.getFactionNameByChunk(world.getUniqueId(), newChunk)))
            {
                if(!FactionLogic.getFactionNameByChunk(world.getUniqueId(), newChunk).equals("SafeZone") && !FactionLogic.getFactionNameByChunk(world.getUniqueId(),newChunk).equals("WarZone") && !FactionLogic.getFactionNameByChunk(world.getUniqueId(), newChunk).equals(""))
                {
                    if(!FactionLogic.hasOnlinePlayers(FactionLogic.getFactionNameByChunk(world.getUniqueId(), newChunk)))
                    {
                        //Teleport player back if all entering faction's players are offline.
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't enter this faction! None of this faction's players are online!"));
                        player.setLocation(new Location<World>(world, event.getFromTransform().getLocation().getBlockPosition()));
                        return;
                    }
                }

                String factionName = FactionLogic.getFactionNameByChunk(world.getUniqueId(), newChunk);
                if(factionName == "") factionName = "Wilderness";

                Title title = Title.builder()
                        .subtitle(Text.of("You have enterned faction ", TextColors.GOLD, factionName))
                        .build();

                player.sendTitle(title);
            }
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
