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

public class DisbandCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player) source;

            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

            if (optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();
                if (EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    boolean didSucceed = FactionLogic.disbandFaction(playerFaction.Name);

                    if (didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_DISBANDED));

                        if (EagleFactions.AutoClaimList.contains(player.getUniqueId()))
                            EagleFactions.AutoClaimList.remove(player.getUniqueId());
                    } else
                    {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, PluginMessages.SOMETHING_WENT_WRONG));
                    }

                    return CommandResult.success();
                }

                if (playerFaction.Leader.equals(player.getUniqueId().toString()))
                {
                    try
                    {
                        boolean didSucceed = FactionLogic.disbandFaction(playerFaction.Name);

                        if (didSucceed)
                        {
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_DISBANDED));

                            if (EagleFactions.AutoClaimList.contains(player.getUniqueId()))
                                EagleFactions.AutoClaimList.remove(player.getUniqueId());
                        } else
                        {
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, PluginMessages.SOMETHING_WENT_WRONG));
                        }

                        return CommandResult.success();
                    } catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                } else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));
                }
            } else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
