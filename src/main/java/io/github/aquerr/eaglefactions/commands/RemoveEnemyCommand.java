package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.RemoveEnemy;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RemoveEnemyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        String enemyFactionName = context.<String>getOne(Text.of("faction name")).get();

        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    if(FactionLogic.getFactionsNames().contains(enemyFactionName))
                    {
                        if(FactionLogic.getEnemies(playerFactionName).contains(enemyFactionName))
                        {
                            RemoveEnemy checkRemove = new RemoveEnemy(enemyFactionName, playerFactionName);
                            if(EagleFactions.RemoveEnemyList.contains(checkRemove))
                            {
                                FactionLogic.removeEnemy(enemyFactionName, playerFactionName);

                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "You have accepted peace request from ", TextColors.GOLD, enemyFactionName + "!"));

                                EagleFactions.RemoveEnemyList.remove(checkRemove);
                            }
                            else if(!EagleFactions.RemoveEnemyList.contains(checkRemove))
                            {
                                RemoveEnemy removeEnemy = new RemoveEnemy(playerFactionName, enemyFactionName);
                                EagleFactions.RemoveEnemyList.add(removeEnemy);

                                Player enemyFactionLeader = PlayerService.getPlayer(UUID.fromString(FactionLogic.getLeader(enemyFactionName))).get();

                                enemyFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction ", TextColors.GOLD, playerFactionName, TextColors.WHITE, " wants to end the ", TextColors.RED, "war ", TextColors.WHITE, "with your faction!", TextColors.GREEN, " You have 2 minutes to accept it!"));
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.WHITE, "You requested war-end with faction ", TextColors.GOLD, enemyFactionName, TextColors.WHITE, " to the alliance."));

                                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                taskBuilder.execute(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(EagleFactions.RemoveEnemyList.contains(removeEnemy) && EagleFactions.RemoveEnemyList != null)
                                        {
                                            EagleFactions.RemoveEnemyList.remove(removeEnemy);
                                        }
                                    }
                                }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Enemy").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());

                                CommandResult.success();

                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are not in the war with this faction!"));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, enemyFactionName + "!"));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to invite players!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
