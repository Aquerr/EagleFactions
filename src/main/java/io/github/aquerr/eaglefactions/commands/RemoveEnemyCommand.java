package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.RemoveEnemy;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RemoveEnemyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalEnemyFactionName = context.<String>getOne("faction name");

        if (optionalEnemyFactionName.isPresent())
        {
            if (source instanceof Player)
            {
                Player player = (Player) source;
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

                Faction enemyFaction = FactionLogic.getFactionByName(optionalEnemyFactionName.get());

                if (enemyFaction == null)
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, optionalEnemyFactionName.get() + "!"));
                    return CommandResult.success();
                }
                else
                {
                    if (optionalPlayerFaction.isPresent())
                    {
                        Faction playerFaction = optionalPlayerFaction.get();
                        if (EagleFactions.AdminList.contains(player.getUniqueId()))
                        {
                            if (playerFaction.Enemies.contains(enemyFaction.Name))
                            {
                                FactionLogic.removeEnemy(enemyFaction.Name, playerFaction.Name);
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_REMOVED_WAR_STATE_WITH + " ", TextColors.GOLD, enemyFaction, TextColors.GREEN, "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_NOT_IN_THE_WAR_WITH_THIS_FACTION));
                            }
                            return CommandResult.success();
                        }

                        if (playerFaction.Leader.equals(player.getUniqueId().toString()) || playerFaction.Officers.contains(player.getUniqueId().toString()))
                        {
                            if (playerFaction.Enemies.contains(enemyFaction.Name))
                            {
                                RemoveEnemy checkRemove = new RemoveEnemy(enemyFaction.Name, playerFaction.Name);
                                if (EagleFactions.RemoveEnemyList.contains(checkRemove))
                                {
                                    FactionLogic.removeEnemy(enemyFaction.Name, playerFaction.Name);
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_HAVE_ACCEPTED_PEACE_REQUEST_FROM + " ", TextColors.GOLD, enemyFaction + "!"));
                                    EagleFactions.RemoveEnemyList.remove(checkRemove);
                                }
                                else if (!EagleFactions.RemoveEnemyList.contains(checkRemove))
                                {
                                    RemoveEnemy removeEnemy = new RemoveEnemy(playerFaction.Name, enemyFaction.Name);
                                    EagleFactions.RemoveEnemyList.add(removeEnemy);

                                    Player enemyFactionLeader = PlayerManager.getPlayer(UUID.fromString(enemyFaction.Leader)).get();

                                    enemyFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.Name, TextColors.WHITE, " " + PluginMessages.WANTS_TO_END_THE + " ", TextColors.RED, PluginMessages.WAR + " ", TextColors.WHITE, PluginMessages.WITH_YOUR_FACTION, TextColors.GREEN, " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                            " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f remove enemy " + playerFaction.Name, TextColors.WHITE, " " + PluginMessages.TO_ACCEPT_IT));
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_REQUESTED_END_OF_WAR_WITH_FACTION + " ", TextColors.GOLD, enemyFaction.Name, TextColors.RESET, "!"));

                                    Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                    taskBuilder.execute(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            if (EagleFactions.RemoveEnemyList.contains(removeEnemy) && EagleFactions.RemoveEnemyList != null)
                                            {
                                                EagleFactions.RemoveEnemyList.remove(removeEnemy);
                                            }
                                        }
                                    }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Enemy").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());

                                    return CommandResult.success();
                                }
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_NOT_IN_THE_WAR_WITH_THIS_FACTION));
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
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f enemy remove <faction name>"));
        }

        return CommandResult.success();
    }
}
