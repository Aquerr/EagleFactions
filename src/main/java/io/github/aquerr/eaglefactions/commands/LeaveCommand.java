package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class LeaveCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());


            if(playerFactionName != null)
            {
                if(!FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()))
                {

                    FactionLogic.leaveFaction(player.getUniqueId(), playerFactionName);

                    //TODO: Add listener that will inform players in a faction that someone has left their faction.
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN,"You left faction ", TextColors.GOLD, playerFactionName));

                    CommandResult.success();
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't leave your faction because you are its leader! Disband your faction or set someone as leader."));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are not in a faction!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
