package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

/**
 * Created by Aquerr on 2018-06-24.
 */
public class DemoteCommand extends AbstractCommand
{
    public DemoteCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalDemotedPlayer = context.<Player>getOne("player");

        if (optionalDemotedPlayer.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                Player demotedPlayer = optionalDemotedPlayer.get();
                Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                Optional<Faction> optionalDemotedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(demotedPlayer.getUniqueId());

                if(optionalPlayerFaction.isPresent())
                {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if(optionalDemotedPlayerFaction.isPresent() && optionalDemotedPlayerFaction.get().getName().equals(playerFaction.getName()))
                    {
                        if(EagleFactions.AdminList.contains(player.getUniqueId()))
                        {
                            if(!playerFaction.getLeader().equals(demotedPlayer.getUniqueId()) && !playerFaction.getOfficers().contains(demotedPlayer.getUniqueId()))
                            {
                                FactionMemberType demotedTo = getPlugin().getFactionLogic().demotePlayer(playerFaction, demotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOU_DEMOTED + " ", TextColors.GOLD, player.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", demotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }

                        if(playerFaction.getLeader().equals(player.getUniqueId()))
                        {
                            if(!playerFaction.getLeader().equals(demotedPlayer.getUniqueId()) && !playerFaction.getRecruits().contains(demotedPlayer.getUniqueId()))
                            {
                                FactionMemberType promotedTo = getPlugin().getFactionLogic().demotePlayer(playerFaction, demotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOU_DEMOTED + " ", TextColors.GOLD, player.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }
                        else if(playerFaction.getOfficers().contains(player.getUniqueId()))
                        {
                            if(!playerFaction.getLeader().equals(demotedPlayer.getUniqueId()) && !playerFaction.getOfficers().contains(demotedPlayer.getUniqueId()) && !playerFaction.getMembers().contains(demotedPlayer.getUniqueId()))
                            {
                                FactionMemberType promotedTo = getPlugin().getFactionLogic().demotePlayer(playerFaction, demotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOU_DEMOTED + " ", TextColors.GOLD, player.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            }
            else
            {
                source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f demote <player>"));
        }
        return CommandResult.success();
    }
}
