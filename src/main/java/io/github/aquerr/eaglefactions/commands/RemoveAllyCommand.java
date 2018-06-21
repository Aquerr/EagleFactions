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

/**
 * Created by Aquerr on 2017-08-04.
 */
public class RemoveAllyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne("faction name");

        if (optionalFactionName.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

                String rawFactionName = optionalFactionName.get();
                Faction removedFaction = FactionLogic.getFactionByName(rawFactionName);

                if (removedFaction == null)
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName + "!"));
                    return CommandResult.success();
                }
                else
                {
                    if(optionalPlayerFaction.isPresent())
                    {
                        Faction playerFaction = optionalPlayerFaction.get();

                        if(EagleFactions.AdminList.contains(player.getUniqueId()))
                        {
                            if(!playerFaction.Alliances.contains(removedFaction.Name))
                            {
                                FactionLogic.removeAlly(playerFaction.Name, removedFaction.Name);
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, PluginMessages.YOU_DISBANDED_YOUR_ALLIANCE_WITH + " ", TextColors.GOLD, removedFaction.Name, TextColors.GREEN, "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOUR_FACTION_IS_NOT_IN_THE_ALLIANCE_WITH + " ", TextColors.GOLD, removedFaction.Name + "!"));
                            }

                            return CommandResult.success();
                        }

                        if(playerFaction.Leader.equals(player.getUniqueId().toString()) || playerFaction.Officers.contains(player.getUniqueId().toString()))
                        {
                            if(playerFaction.Alliances.contains(removedFaction.Name))
                            {
                                FactionLogic.removeAlly(playerFaction.Name, removedFaction.Name);
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, PluginMessages.YOU_DISBANDED_YOUR_ALLIANCE_WITH + " ", TextColors.GOLD, removedFaction.Name, TextColors.GREEN, "!"));
                                return CommandResult.success();
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOUR_FACTION_IS_NOT_IN_THE_ALLIANCE_WITH + " ", TextColors.GOLD, removedFaction.Name + "!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                    }
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
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f ally remove <faction name>"));
        }

        return CommandResult.success();
    }
}
