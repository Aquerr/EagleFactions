package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;

public class AddEnemyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne(Text.of("faction name"));

        if(optionalFactionName.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                String rawFactionName = context.<String>getOne(Text.of("faction name")).get();
                String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());
                String enemyFactionName = FactionLogic.getRealFactionName(rawFactionName);

                if (enemyFactionName == null)
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName, TextColors.RED, "!"));
                    return CommandResult.success();
                }

                if(playerFactionName != null)
                {
                    if(EagleFactions.AdminList.contains(player.getUniqueId()))
                    {
                        if(!FactionLogic.getAlliances(playerFactionName).contains(enemyFactionName))
                        {
                            if(!FactionLogic.getEnemies(playerFactionName).contains(enemyFactionName))
                            {
                                FactionLogic.addEnemy(playerFactionName, enemyFactionName);
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_ADDED_TO_THE_ENEMIES));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_FACTION_IS_ALREADY_YOUR_ENEMY));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_FACTION_IS_YOUR_ALLY + " " + PluginMessages.REMOVE_ALLIANCE_FIRST_TO_DECLARE_A_WAR));
                        }
                        return CommandResult.success();
                    }

                    if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                    {
                        if(FactionLogic.getFactionsNames().contains(enemyFactionName))
                        {
                            if(!FactionLogic.getAlliances(playerFactionName).contains(enemyFactionName))
                            {
                                if(!FactionLogic.getEnemies(playerFactionName).contains(enemyFactionName))
                                {
                                    FactionLogic.addEnemy(playerFactionName, enemyFactionName);

                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_FACTION_IS_NOW + " ", TextColors.RED, PluginMessages.ENEMIES + " ", TextColors.WHITE, "with " + enemyFactionName));

                                    //TODO: Check if player is online
                                    Player enemyFactionLeader = PlayerManager.getPlayer(UUID.fromString(FactionLogic.getLeader(enemyFactionName))).get();
                                    enemyFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, "Faction ", TextColors.GOLD, playerFactionName, TextColors.WHITE, " has declared you a ", TextColors.RED, "War!"));

                                    CommandResult.success();
                                }
                                else
                                {
                                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_FACTION_IS_ALREADY_YOUR_ENEMY));
                                }
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_FACTION_IS_YOUR_ALLY + " " + PluginMessages.REMOVE_ALLIANCE_FIRST_TO_DECLARE_A_WAR));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, enemyFactionName + "!"));
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
            else
            {
                source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f enemy add <faction name>"));
            return CommandResult.success();
        }

        return CommandResult.success();
    }
}
