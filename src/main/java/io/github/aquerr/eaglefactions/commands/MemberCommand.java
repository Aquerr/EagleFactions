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

public class MemberCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalNewMemberPlayer = context.<Player>getOne("player");

        if (optionalNewMemberPlayer.isPresent())
        {
            if (source instanceof Player)
            {
                Player player = (Player) source;
                Player newMemberPlayer = optionalNewMemberPlayer.get();
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
                Optional<Faction> optionalNewMemberFaction = FactionLogic.getFactionByPlayerUUID(newMemberPlayer.getUniqueId());

                if (optionalPlayerFaction.isPresent())
                {
                    Faction playerFaction = optionalPlayerFaction.get();
                    if (EagleFactions.AdminList.contains(player.getUniqueId()))
                    {
                        if (optionalNewMemberFaction.isPresent() && optionalNewMemberFaction.get().Name.equals(playerFaction.Name))
                        {
                            if (!playerFaction.Leader.equals(newMemberPlayer.getUniqueId().toString()))
                            {
                                if (playerFaction.Officers.contains(newMemberPlayer.getUniqueId().toString()))
                                {
                                    FactionLogic.removeOfficerAndSetAsMember(newMemberPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_REMOVED + " ", TextColors.GOLD, newMemberPlayer.getName(), TextColors.WHITE, " " + PluginMessages.FROM_YOUR + " ", TextColors.BLUE, PluginMessages.OFFICERS, TextColors.WHITE, "!"));
                                } else if (playerFaction.Recruits.contains(newMemberPlayer.getUniqueId().toString()))
                                {
                                    FactionLogic.addMemberAndRemoveRecruit(newMemberPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, newMemberPlayer.getName(), TextColors.WHITE, " " + PluginMessages.TO + " ", TextColors.BLUE, PluginMessages.MEMBERS, TextColors.WHITE, "!"));
                                }
                            } else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_SET_FACTIONS_LEADER_AS_MEMBER));
                            }
                        } else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }

                        return CommandResult.success();
                    }

                    if (playerFaction.Leader.equals(player.getUniqueId().toString()))
                    {
                        if (optionalNewMemberFaction.isPresent() && optionalNewMemberFaction.get().Name.equals(playerFaction.Name))
                        {
                            if (!playerFaction.Leader.equals(newMemberPlayer.getUniqueId().toString()))
                            {
                                if (playerFaction.Officers.contains(newMemberPlayer.getUniqueId().toString()))
                                {
                                    FactionLogic.removeOfficerAndSetAsMember(newMemberPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_REMOVED + " ", TextColors.GOLD, newMemberPlayer.getName(), TextColors.WHITE, " " + PluginMessages.FROM_YOUR + " ", TextColors.BLUE, PluginMessages.OFFICERS, TextColors.WHITE, "!"));
                                } else if (playerFaction.Recruits.contains(newMemberPlayer.getUniqueId().toString()))
                                {
                                    FactionLogic.addMemberAndRemoveRecruit(newMemberPlayer.getUniqueId().toString(), playerFaction.Name);
                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, newMemberPlayer.getName(), TextColors.WHITE, " " + PluginMessages.TO + " ", TextColors.BLUE, PluginMessages.MEMBERS, TextColors.WHITE, "!"));
                                }
                            } else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_SET_FACTIONS_LEADER_AS_MEMBER));
                            }
                        } else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }

                    } else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));
                    }
                } else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f member <player>"));
        }

        return CommandResult.success();
    }
}
