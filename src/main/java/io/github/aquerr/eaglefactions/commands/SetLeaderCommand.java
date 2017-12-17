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

import java.util.Optional;

public class SetLeaderCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalNewLeaderPlayer = context.<Player>getOne("player");

        if (optionalNewLeaderPlayer.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                Player newLeaderPlayer = optionalNewLeaderPlayer.get();
                String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

                if(playerFactionName != null)
                {
                    if(EagleFactions.AdminList.contains(player.getUniqueId().toString()))
                    {
                        if(FactionLogic.getFactionName(newLeaderPlayer.getUniqueId()).equals(playerFactionName))
                        {
                            if(!FactionLogic.getLeader(playerFactionName).equals(newLeaderPlayer.getUniqueId().toString()))
                            {
                                FactionLogic.setLeader(newLeaderPlayer.getUniqueId().toString(), playerFactionName);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You set ", TextColors.GOLD, newLeaderPlayer.getName(), TextColors.WHITE, " as your new ", TextColors.BLUE, "Leader", TextColors.WHITE, "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already a leader of this faction!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This player is not in your team!"));
                        }

                        return CommandResult.success();
                    }

                    if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()))
                    {
                        if(FactionLogic.getFactionName(newLeaderPlayer.getUniqueId()).equals(playerFactionName))
                        {
                            if(!FactionLogic.getLeader(playerFactionName).equals(newLeaderPlayer.getUniqueId().toString()))
                            {
                                FactionLogic.setLeader(newLeaderPlayer.getUniqueId().toString(), playerFactionName);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You set ", TextColors.GOLD, newLeaderPlayer.getName(), TextColors.WHITE, " as your new ", TextColors.BLUE, "Leader", TextColors.WHITE, "!"));
                                return CommandResult.success();
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already a leader of this faction!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This player is not in your team!"));
                        }

                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader to do this!"));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to use this command!"));
                }
            }
            else
            {
                source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments!"));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f setleader <player>"));
        }

        return CommandResult.success();
    }
}
