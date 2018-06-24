package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
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

/**
 * Created by Aquerr on 2018-06-24.
 */
public class PromoteCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalPromotedPlayer = context.<Player>getOne("player");

        if (optionalPromotedPlayer.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                Player promotedPlayer = optionalPromotedPlayer.get();
                Optional<Faction> optionalPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(player.getUniqueId());
                Optional<Faction> optionalPromotedPlayerFaction = FactionsCache.getInstance().getFactionByPlayer(promotedPlayer.getUniqueId());

                if(optionalPlayerFaction.isPresent())
                {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if(optionalPromotedPlayerFaction.isPresent() && optionalPromotedPlayerFaction.get().Name.equals(playerFaction.Name))
                    {
                        if(EagleFactions.AdminList.contains(player.getUniqueId()))
                        {
                            if(!playerFaction.Leader.equals(promotedPlayer.getUniqueId().toString()) && !playerFaction.Officers.contains(promotedPlayer.getUniqueId().toString()))
                            {
                                FactionMemberType promotedTo = FactionLogic.promotePlayer(playerFaction, FactionMemberType.LEADER, promotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, player.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }

                        if(playerFaction.Leader.equals(player.getUniqueId().toString()))
                        {
                            if(!playerFaction.Leader.equals(promotedPlayer.getUniqueId().toString()) && !playerFaction.Officers.contains(promotedPlayer.getUniqueId().toString()))
                            {
                                FactionMemberType promotedTo = FactionLogic.promotePlayer(playerFaction, FactionMemberType.LEADER, promotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, player.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }
                        else if(playerFaction.Officers.contains(player.getUniqueId().toString()))
                        {
                            if(!playerFaction.Leader.equals(promotedPlayer.getUniqueId().toString()) && !playerFaction.Officers.contains(promotedPlayer.getUniqueId().toString()) && !playerFaction.Members.contains(promotedPlayer.getUniqueId().toString()))
                            {
                                FactionMemberType promotedTo = FactionLogic.promotePlayer(playerFaction, FactionMemberType.OFFICER, promotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, player.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            }
            else
            {
                source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f promote <player>"));
        }
        return CommandResult.success();
    }
}
