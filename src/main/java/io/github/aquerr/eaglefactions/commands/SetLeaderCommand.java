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

public class SetLeaderCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalNewLeaderPlayer = context.<Player>getOne("player");

        if (optionalNewLeaderPlayer.isPresent())
        {
            if (source instanceof Player)
            {
                Player player = (Player) source;
                Player newLeaderPlayer = optionalNewLeaderPlayer.get();
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
                Optional<Faction> optionalNewLeaderPlayerFaction = FactionLogic.getFactionByPlayerUUID(newLeaderPlayer.getUniqueId());

                if (optionalPlayerFaction.isPresent())
                {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if (optionalNewLeaderPlayerFaction.isPresent() && optionalNewLeaderPlayerFaction.get().Name.equals(playerFaction.Name))
                    {
                        if (EagleFactions.AdminList.contains(player.getUniqueId()))
                        {
                            if (!playerFaction.Leader.equals(newLeaderPlayer.getUniqueId().toString()))
                            {
                                FactionLogic.setLeader(newLeaderPlayer.getUniqueId(), playerFaction.Name);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_SET + " ", TextColors.GOLD, newLeaderPlayer.getName(), TextColors.WHITE, " " + PluginMessages.AS_YOUR_NEW + " ", TextColors.BLUE, PluginMessages.LEADER, TextColors.WHITE, "!"));
                            } else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION));
                            }

                            return CommandResult.success();
                        }

                        if (playerFaction.Leader.equals(player.getUniqueId().toString()))
                        {
                            if (!playerFaction.Leader.equals(newLeaderPlayer.getUniqueId().toString()))
                            {
                                FactionLogic.setLeader(newLeaderPlayer.getUniqueId(), playerFaction.Name);
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_SET + " ", TextColors.GOLD, newLeaderPlayer.getName(), TextColors.WHITE, " as your new ", TextColors.BLUE, "Leader", TextColors.WHITE, "!"));
                            } else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION));
                            }

                            return CommandResult.success();
                        }
                    } else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
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
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f setleader <player>"));
        }

        return CommandResult.success();
    }
}
