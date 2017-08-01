package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.managers.FactionManager;
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

            String playerFactionName = FactionManager.getFaction(player.getUniqueId ());

            if(playerFactionName != null)
            {
                if(FactionManager.getLeader(playerFactionName).equals(player.getUniqueId().toString()))
                {
                    //TODO: Invoke disband function here.
                    try
                    {
                        FactionManager.disbandFaction(playerFactionName);

                        player.sendMessage(Text.of(TextColors.BLUE, "[EagleFactions] ",TextColors.GREEN,"Faction has been disbanded"));

                        return CommandResult.success();
                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if(FactionManager.getMembers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    player.sendMessage(Text.of(TextColors.DARK_RED, "[ERROR] ", "You need to be the leader to disband a faction!"));
                }
            }
            else
            {
                player.sendMessage(Text.of(TextColors.DARK_RED, "[ERROR] ", TextColors.RED, "You are not in the faction!"));
            }
        }

        return CommandResult.success();
    }
}
