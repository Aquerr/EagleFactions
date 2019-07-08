package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class SetHomeCommand extends AbstractCommand
{
    public SetHomeCommand(EagleFactionsPlugin plugin)
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

            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();
                World world = player.getWorld();

                if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                {
                    Vector3i home = new Vector3i(player.getLocation().getBlockPosition());
                    getPlugin().getFactionLogic().setHome(world.getUniqueId(), playerFaction, home);
                    source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.FACTION_HOME_HAS_BEEN_SET));

                    return CommandResult.success();
                }

                if(playerFaction.getLeader().equals(player.getUniqueId()) || playerFaction.getOfficers().contains(player.getUniqueId()))
                {
                    Optional<Faction> chunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), player.getLocation().getChunkPosition());

                    if(chunkFaction.isPresent() && chunkFaction.get().getName().equals(playerFaction.getName()))
                    {
                        Vector3i home = new Vector3i(player.getLocation().getBlockPosition());

                        getPlugin().getFactionLogic().setHome(world.getUniqueId(), playerFaction, home);
                        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.FACTION_HOME_HAS_BEEN_SET));
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
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
