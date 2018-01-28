package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
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

public class FriendlyFireCommand implements CommandExecutor
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
                if(EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    if(FactionLogic.getFactionFriendlyFire(playerFactionName))
                    {
                        FactionLogic.setFactionFriendlyFire(playerFactionName,false);
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction's ", TextColors.GOLD, "Friendly Fire", TextColors.WHITE, " has been set to ", TextColors.GOLD, "false"));
                    }
                    else
                    {
                        FactionLogic.setFactionFriendlyFire(playerFactionName,true);
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction's ", TextColors.GOLD, "Friendly Fire", TextColors.WHITE, " has been set to ", TextColors.GOLD, "true"));
                    }
                }

                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    if(FactionLogic.getFactionFriendlyFire(playerFactionName))
                    {
                        FactionLogic.setFactionFriendlyFire(playerFactionName,false);
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction's ", TextColors.GOLD, "Friendly Fire", TextColors.WHITE, " has been turned ", TextColors.GOLD, "off"));
                    }
                    else
                    {
                        FactionLogic.setFactionFriendlyFire(playerFactionName,true);
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction's ", TextColors.GOLD, "Friendly Fire", TextColors.WHITE, " has been turned ", TextColors.GOLD, "on"));
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
