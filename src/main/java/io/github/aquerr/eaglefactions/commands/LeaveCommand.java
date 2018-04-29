package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class LeaveCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

            if(optionalPlayerFaction.isPresent())
            {
                if(!optionalPlayerFaction.get().equals(player.getUniqueId().toString()))
                {
                    FactionLogic.leaveFaction(player.getUniqueId(), optionalPlayerFaction.get().Name);

                    //TODO: Add listener that will inform players in a faction that someone has left their faction.
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, PluginMessages.YOU_LEFT_FACTION + " ", TextColors.GOLD, optionalPlayerFaction.get().Name));

                    if(EagleFactions.AutoClaimList.contains(player.getUniqueId())) EagleFactions.AutoClaimList.remove(player.getUniqueId());

                    return CommandResult.success();
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER + " " + PluginMessages.DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
