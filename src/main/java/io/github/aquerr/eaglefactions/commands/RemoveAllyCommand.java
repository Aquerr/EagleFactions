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

/**
 * Created by Aquerr on 2017-08-04.
 */
public class RemoveAllyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        String removedFaction = context.<String>getOne(Text.of("faction name")).get();

        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                //TODO: Add check for officer.
                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()))
                {
                    if(FactionLogic.getAlliances(playerFactionName).contains(removedFaction))
                    {
                        FactionLogic.removeAlliance(playerFactionName,removedFaction);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, "You removed your alliance with ", TextColors.GOLD, removedFaction, TextColors.GREEN, "!"));

                        CommandResult.success();

                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction is not in alliance with ", TextColors.GOLD, removedFaction + "!"));
                    }

                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to invite players!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
