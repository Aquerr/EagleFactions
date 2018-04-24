package io.github.aquerr.eaglefactions.commands;

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

public class UnclaimallCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            //Check if player is in the faction.
            if(playerFactionName != "")
            {
                Faction playerFaction = FactionLogic.getFaction(playerFactionName);

                if(playerFaction.Leader.equals(player.getUniqueId().toString()) || playerFaction.Officers.contains(player.getUniqueId().toString()) || EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    if(FactionLogic.getHome(playerFaction) != null)
                    {
                        FactionLogic.setHome(null, playerFactionName, null);
                    }

                    FactionLogic.removeClaims(playerFaction);
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully removed all claims!"));

                    return CommandResult.success();

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
