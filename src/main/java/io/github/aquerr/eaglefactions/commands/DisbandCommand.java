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

public class DisbandCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId ());

            if(playerFactionName != null)
            {
                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()))
                {
                    try
                    {
                        FactionLogic.disbandFaction(playerFactionName);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN,"Faction has been disbanded"));

                        return CommandResult.success();
                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if(FactionLogic.getMembers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, "You need to be the leader to disband a faction!"));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are not in the faction!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
