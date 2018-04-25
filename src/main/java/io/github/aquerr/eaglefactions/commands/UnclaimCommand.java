package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class UnclaimCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

            //Check if player has admin mode.
            if(EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                World world = player.getWorld();
                Vector3i chunk = player.getLocation().getChunkPosition();

                Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), chunk);

                if (optionalChunkFaction.isPresent())
                {
                    if (optionalChunkFaction.get().Home != null)
                    {
                        if (world.getUniqueId().equals(optionalChunkFaction.get().Home.WorldUUID))
                        {
                            Location homeLocation = world.getLocation(optionalChunkFaction.get().Home.BlockPosition);

                            if(homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString())) FactionLogic.setHome(world.getUniqueId(), optionalChunkFaction.get(), null);
                        }
                    }

                    FactionLogic.removeClaim(optionalChunkFaction.get(), world.getUniqueId() ,chunk);

                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land has been successfully ", TextColors.GOLD, "unclaimed", TextColors.WHITE, "!"));
                    return CommandResult.success();
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This place is not claimed!"));
                    return CommandResult.success();
                }
            }

            //Check if player is in the faction.
            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if(playerFaction.Leader.equals(player.getUniqueId().toString()) || playerFaction.Officers.contains(player.getUniqueId().toString()))
                {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), chunk);

                    if (optionalChunkFaction.isPresent())
                    {
                        Faction chunkFaction = optionalChunkFaction.get();

                        if (chunkFaction.Name.equals(playerFaction.Name))
                        {
                            if (optionalChunkFaction.get().Home != null)
                            {
                                if (world.getUniqueId().equals(optionalChunkFaction.get().Home.WorldUUID))
                                {
                                    Location homeLocation = world.getLocation(optionalChunkFaction.get().Home.BlockPosition);

                                    if(homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString())) FactionLogic.setHome(world.getUniqueId(), optionalChunkFaction.get(), null);
                                }
                            }

                            FactionLogic.removeClaim(optionalChunkFaction.get(), world.getUniqueId() ,chunk);

                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land has been successfully ", TextColors.GOLD, "unclaimed", TextColors.WHITE, "!"));
                            return CommandResult.success();
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, "This land does not belong to your faction!"));
                            return CommandResult.success();
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This place is not claimed!"));
                        return CommandResult.success();
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to claim lands!"));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
