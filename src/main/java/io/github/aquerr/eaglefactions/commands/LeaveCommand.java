package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class LeaveCommand extends AbstractCommand
{
    public LeaveCommand(EagleFactions plugin)
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
                if(!optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()))
                {
                    getPlugin().getFactionLogic().leaveFaction(player.getUniqueId(), optionalPlayerFaction.get().getName());

                    //TODO: Add listener that will inform players in a faction that someone has left their faction.
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, PluginMessages.YOU_LEFT_FACTION + " ", TextColors.GOLD, optionalPlayerFaction.get().getName()));

                    EagleFactions.AutoClaimList.remove(player.getUniqueId());

                    return CommandResult.success();
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER + " " + PluginMessages.DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
