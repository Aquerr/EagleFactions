package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
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
public class PromoteCommand extends AbstractCommand
{
    public PromoteCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalPromotedPlayer = context.<Player>getOne("player");

        if (optionalPromotedPlayer.isPresent())
        {
            if(source instanceof Player)
            {
                final Player player = (Player)source;
                final Player promotedPlayer = optionalPromotedPlayer.get();
                final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                final Optional<Faction> optionalPromotedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(promotedPlayer.getUniqueId());

                if(optionalPlayerFaction.isPresent())
                {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if(optionalPromotedPlayerFaction.isPresent() && optionalPromotedPlayerFaction.get().getName().equals(playerFaction.getName()))
                    {
                        if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                        {
                            if(!playerFaction.getLeader().equals(promotedPlayer.getUniqueId()) && !playerFaction.getOfficers().contains(promotedPlayer.getUniqueId()))
                            {
                                FactionMemberType promotedTo = getPlugin().getFactionLogic().promotePlayer(playerFaction, promotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, promotedPlayer.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }

                        if(playerFaction.getLeader().equals(player.getUniqueId()))
                        {
                            if(!playerFaction.getLeader().equals(promotedPlayer.getUniqueId()) && !playerFaction.getOfficers().contains(promotedPlayer.getUniqueId()))
                            {
                                FactionMemberType promotedTo = getPlugin().getFactionLogic().promotePlayer(playerFaction, promotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, promotedPlayer.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
                            }

                            return CommandResult.success();
                        }
                        else if(playerFaction.getOfficers().contains(player.getUniqueId()))
                        {
                            if(!playerFaction.getLeader().equals(promotedPlayer.getUniqueId()) && !playerFaction.getOfficers().contains(promotedPlayer.getUniqueId()) && !playerFaction.getMembers().contains(promotedPlayer.getUniqueId()))
                            {
                                FactionMemberType promotedTo = getPlugin().getFactionLogic().promotePlayer(playerFaction, promotedPlayer);
                                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOU_PROMOTED + " ", TextColors.GOLD, promotedPlayer.getName(), TextColors.RESET, " " + PluginMessages.TO, " ", promotedTo.toString() + "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
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
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f promote <player>"));
        }
        return CommandResult.success();
    }
}
