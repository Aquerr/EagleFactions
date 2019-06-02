package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.StopWarRequest;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class EnemyCommand extends AbstractCommand
{
    public EnemyCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne(Text.of("faction name"));

        if(optionalFactionName.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player) source;
                String rawFactionName = context.<String>getOne(Text.of("faction name")).get();
                Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                Faction enemyFaction = getPlugin().getFactionLogic().getFactionByName(rawFactionName);

                if(enemyFaction == null)
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName, TextColors.RED, "!"));
                    return CommandResult.success();
                }

                if(optionalPlayerFaction.isPresent())
                {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if(EagleFactions.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                    {
                        if(!playerFaction.getAlliances().contains(enemyFaction.getName()))
                        {
                            if(!playerFaction.getEnemies().contains(enemyFaction.getName()))
                            {
                                getPlugin().getFactionLogic().addEnemy(playerFaction.getName(), enemyFaction.getName());
                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_ADDED_TO_THE_ENEMIES));
                            }
                            else
                            {
                                getPlugin().getFactionLogic().removeEnemy(playerFaction.getName(), enemyFaction.getName());
                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.YOU_REMOVED_WAR_STATE_WITH + " ", TextColors.GOLD, enemyFaction, TextColors.GREEN, "!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_FACTION_IS_YOUR_ALLY + " " + PluginMessages.REMOVE_ALLIANCE_FIRST_TO_DECLARE_A_WAR));
                        }
                        return CommandResult.success();
                    }

                    if(playerFaction.getLeader().equals(player.getUniqueId()) || playerFaction.getOfficers().contains(player.getUniqueId()))
                    {
                        if(!playerFaction.getAlliances().contains(enemyFaction.getName()))
                        {
                            if(!playerFaction.getEnemies().contains(enemyFaction.getName()))
                            {
                                getPlugin().getFactionLogic().addEnemy(playerFaction.getName(), enemyFaction.getName());

                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_FACTION_IS_NOW + " ", TextColors.RED, PluginMessages.ENEMIES + " ", TextColors.WHITE, PluginMessages.WITH + " " + enemyFaction.getName() + "!"));

                                //TODO: Check if player is online
                                Player enemyFactionLeader = getPlugin().getPlayerManager().getPlayer(enemyFaction.getLeader()).get();
                                enemyFactionLeader.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.HAS_DECLARED_YOU_A_WAR + "!"));

                                return CommandResult.success();
                            }
                            else
                            {
                                StopWarRequest checkRemove = new StopWarRequest(enemyFaction.getName(), playerFaction.getName());
                                if(EagleFactions.WAR_STOP_REQUEST_LIST.contains(checkRemove))
                                {
                                    getPlugin().getFactionLogic().removeEnemy(enemyFaction.getName(), playerFaction.getName());
                                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.YOU_HAVE_ACCEPTED_PEACE_REQUEST_FROM + " ", TextColors.GOLD, enemyFaction.getName() + "!"));
                                    EagleFactions.WAR_STOP_REQUEST_LIST.remove(checkRemove);
                                }
                                else if(!EagleFactions.WAR_STOP_REQUEST_LIST.contains(checkRemove))
                                {

                                    StopWarRequest stopWarRequest = new StopWarRequest(playerFaction.getName(), enemyFaction.getName());
                                    if(EagleFactions.WAR_STOP_REQUEST_LIST.contains(stopWarRequest))
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, "You have already sent a war-end request to this faction. Wait for their response!"));
                                        return CommandResult.success();
                                    }
                                    EagleFactions.WAR_STOP_REQUEST_LIST.add(stopWarRequest);

                                    Player enemyFactionLeader = getPlugin().getPlayerManager().getPlayer(enemyFaction.getLeader()).get();
                                    enemyFactionLeader.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.WANTS_TO_END_THE + " ", TextColors.RED, PluginMessages.WAR + " ", TextColors.WHITE, PluginMessages.WITH_YOUR_FACTION, TextColors.GREEN, " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                            " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f enemy " + playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.TO_ACCEPT_IT));
                                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, PluginMessages.YOU_REQUESTED_END_OF_WAR_WITH_FACTION + " ", TextColors.GOLD, enemyFaction.getName(), TextColors.RESET, "!"));

                                    Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                                    taskBuilder.execute(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            EagleFactions.WAR_STOP_REQUEST_LIST.remove(stopWarRequest);
                                        }
                                    }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Enemy").submit(Sponge.getPluginManager().getPlugin(PluginInfo.ID).get().getInstance().get());
                                    return CommandResult.success();
                                }
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_FACTION_IS_YOUR_ALLY + " " + PluginMessages.REMOVE_ALLIANCE_FIRST_TO_DECLARE_A_WAR));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f enemy <faction name>"));
        }

        return CommandResult.success();
    }
}
