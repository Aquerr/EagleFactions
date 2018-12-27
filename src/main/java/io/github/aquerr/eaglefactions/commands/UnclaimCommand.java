package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class UnclaimCommand extends AbstractCommand
{
    public UnclaimCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

            //Check if player has admin mode.
            if(EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                World world = player.getWorld();
                Vector3i chunk = player.getLocation().getChunkPosition();

                Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);

                if (optionalChunkFaction.isPresent())
                {
                    if (optionalChunkFaction.get().getHome() != null)
                    {
                        if (world.getUniqueId().equals(optionalChunkFaction.get().getHome().getWorldUUID()))
                        {
                            Location homeLocation = world.getLocation(optionalChunkFaction.get().getHome().getBlockPosition());

                            if(homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString())) getPlugin().getFactionLogic().setHome(world.getUniqueId(), optionalChunkFaction.get(), null);
                        }
                    }

                    getPlugin().getFactionLogic().removeClaim(optionalChunkFaction.get(), new Claim(world.getUniqueId(), chunk));

                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND_HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.UNCLAIMED, TextColors.WHITE, "!"));
                    return CommandResult.success();
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_DOES_NOT_BELOG_TO_ANYONE));
                    return CommandResult.success();
                }
            }

            //Check if player is in the faction.
            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if (this.getPlugin().getFlagManager().canClaim(player.getUniqueId(), playerFaction))
                {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);

                    if (optionalChunkFaction.isPresent())
                    {
                        Faction chunkFaction = optionalChunkFaction.get();

                        if (chunkFaction.getName().equals(playerFaction.getName()))
                        {
                            if (optionalChunkFaction.get().getHome() != null)
                            {
                                if (world.getUniqueId().equals(optionalChunkFaction.get().getHome().getWorldUUID()))
                                {
                                    Location homeLocation = world.getLocation(optionalChunkFaction.get().getHome().getBlockPosition());

                                    if(homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString())) getPlugin().getFactionLogic().setHome(world.getUniqueId(), optionalChunkFaction.get(), null);
                                }
                            }

                            super.getPlugin().getFactionLogic().removeClaim(optionalChunkFaction.get(), new Claim(world.getUniqueId(), chunk));

                            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND_HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.UNCLAIMED, TextColors.WHITE, "!"));
                            return CommandResult.success();
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_UNCLAIM_LANDS));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
