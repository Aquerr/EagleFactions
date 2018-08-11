package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.StopWarRequest;
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
import java.util.concurrent.TimeUnit;

public class RemoveEnemyCommand extends AbstractCommand implements CommandExecutor
{
    public RemoveEnemyCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalEnemyFactionName = context.<String>getOne("faction name");

        if (optionalEnemyFactionName.isPresent())
        {
            if (source instanceof Player)
            {
                Player player = (Player) source;
                Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

                Faction enemyFaction = getPlugin().getFactionLogic().getFactionByName(optionalEnemyFactionName.get());

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
                            if (playerFaction.getEnemies().contains(enemyFaction.getName()))
                            {
                                getPlugin().getFactionLogic().removeEnemy(enemyFaction.getName(), playerFaction.getName());
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_REMOVED_WAR_STATE_WITH + " ", TextColors.GOLD, enemyFaction, TextColors.GREEN, "!"));
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_NOT_IN_THE_WAR_WITH_THIS_FACTION));
                            }
                            return CommandResult.success();
                        }

                        if (playerFaction.getLeader().equals(player.getUniqueId()) || playerFaction.getOfficers().contains(player.getUniqueId()))
                        {
                            if (playerFaction.getEnemies().contains(enemyFaction.getName()))
                            {
                                StopWarRequest checkRemove = new StopWarRequest(enemyFaction.getName(), playerFaction.getName());
                                if (EagleFactions.stopWarRequestList.contains(checkRemove))
                                {
                                    getPlugin().getFactionLogic().removeEnemy(enemyFaction.getName(), playerFaction.getName());
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_HAVE_ACCEPTED_PEACE_REQUEST_FROM + " ", TextColors.GOLD, enemyFaction.getName() + "!"));
                                    EagleFactions.stopWarRequestList.remove(checkRemove);
                                }
                                else if (!EagleFactions.stopWarRequestList.contains(checkRemove))
                                {
                                    StopWarRequest stopWarRequest = new StopWarRequest(playerFaction.getName(), enemyFaction.getName());
                                    EagleFactions.stopWarRequestList.add(stopWarRequest);

                                    Player enemyFactionLeader = PlayerManager.getPlayer(enemyFaction.getLeader()).get();

                                    enemyFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.WANTS_TO_END_THE + " ", TextColors.RED, PluginMessages.WAR + " ", TextColors.WHITE, PluginMessages.WITH_YOUR_FACTION, TextColors.GREEN, " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                            " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f enemy remove " + playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.TO_ACCEPT_IT));
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.YOU_REQUESTED_END_OF_WAR_WITH_FACTION + " ", TextColors.GOLD, enemyFaction.getName(), TextColors.RESET, "!"));

                                    Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                    taskBuilder.execute(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            if (EagleFactions.stopWarRequestList.contains(stopWarRequest) && EagleFactions.stopWarRequestList != null)
                                            {
                                                EagleFactions.stopWarRequestList.remove(stopWarRequest);
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
