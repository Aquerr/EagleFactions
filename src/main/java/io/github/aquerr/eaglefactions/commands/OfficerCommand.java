package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.AllyInvite;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OfficerCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Player newOfficerPlayer = context.<Player>getOne("player").get();

        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                    if(EagleFactions.AdminList.contains(player.getUniqueId().toString()))
                    {
                        if(FactionLogic.getFactionName(newOfficerPlayer.getUniqueId()).equals(playerFactionName))
                        {
                            if(!FactionLogic.getLeader(playerFactionName).equals(newOfficerPlayer.getUniqueId().toString()))
                            {
                                if(!FactionLogic.getOfficers(playerFactionName).contains(newOfficerPlayer.getUniqueId().toString()))
                                {
                                    FactionLogic.addOfficer(newOfficerPlayer.getUniqueId().toString(), playerFactionName);

                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You added ", TextColors.GOLD, newOfficerPlayer.getName(), TextColors.WHITE, " as your new ", TextColors.BLUE, "Officer", TextColors.WHITE, "!"));
                                }
                                else
                                {
                                    FactionLogic.removeOfficer(newOfficerPlayer.getUniqueId().toString(), playerFactionName);

                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "You removed ", TextColors.GOLD, newOfficerPlayer.getName(), TextColors.WHITE, " from your ", TextColors.BLUE, "Officers", TextColors.WHITE, "!"));
                                }
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't set faction's leader as officer!"));
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
                        if(FactionLogic.getFactionName(newOfficerPlayer.getUniqueId()).equals(playerFactionName))
                        {
                            if(!FactionLogic.getLeader(playerFactionName).equals(newOfficerPlayer.getUniqueId().toString()))
                            {
                                if(!FactionLogic.getOfficers(playerFactionName).contains(newOfficerPlayer.getUniqueId().toString()))
                                {
                                    FactionLogic.addOfficer(newOfficerPlayer.getUniqueId().toString(), playerFactionName);

                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "You added ", TextColors.GOLD, newOfficerPlayer, TextColors.WHITE, " as your new ", TextColors.BLUE, "Officer", TextColors.WHITE, "!"));
                                }
                                else
                                {
                                    FactionLogic.removeOfficer(newOfficerPlayer.getUniqueId().toString(), playerFactionName);

                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "You removed ", TextColors.GOLD, newOfficerPlayer, TextColors.WHITE, " from your ", TextColors.BLUE, "Officers", TextColors.WHITE, "!"));
                                }
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't set faction's leader as officer!"));
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

        return CommandResult.success();
    }
}
