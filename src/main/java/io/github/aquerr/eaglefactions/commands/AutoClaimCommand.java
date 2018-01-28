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

public class AutoClaimCommand implements CommandExecutor
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
                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    if(EagleFactions.AutoClaimList.contains(player.getUniqueId()))
                    {
                        EagleFactions.AutoClaimList.remove(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " has been turned ", TextColors.GOLD, "off"));

                        return CommandResult.success();
                    }
                    else
                    {
                        EagleFactions.AutoClaimList.add(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " has been turned ", TextColors.GOLD, "on"));

                        return CommandResult.success();
                    }
                }
                else if(EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    if(EagleFactions.AutoClaimList.contains(player.getUniqueId()))
                    {
                        EagleFactions.AutoClaimList.remove(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " has been turned ", TextColors.GOLD, "off"));

                        return CommandResult.success();
                    }
                    else
                    {
                        EagleFactions.AutoClaimList.add(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " has been turned ", TextColors.GOLD, "on"));

                        return CommandResult.success();
                    }
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
