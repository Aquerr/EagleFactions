package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.UUID;

public class AddEnemyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        String rawFactionName = context.<String>getOne(Text.of("faction name")).get();

        if(source instanceof Player)
        {
            Player player = (Player)source;
            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            String enemyFactionName = FactionLogic.getRealFactionName(rawFactionName);
            if (enemyFactionName == null)
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, rawFactionName + "!"));
                return CommandResult.success();
            }

            if(playerFactionName != null)
            {
                if(EagleFactions.AdminList.contains(player.getUniqueId().toString()))
                {
                    if(!FactionLogic.getAlliances(playerFactionName).contains(enemyFactionName))
                    {
                        if(!FactionLogic.getEnemies(playerFactionName).contains(enemyFactionName))
                        {
                            FactionLogic.addEnemy(playerFactionName, enemyFactionName);
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction has been added to the enemies!"));
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This faction is already your enemy!"));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This faction is your ally! Remove alliance first to declare a war!"));
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

                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Your faction is now ", TextColors.RED, "enemies ", TextColors.WHITE, "with " + enemyFactionName));

                                //TODO: Check if player is online
                                Player enemyFactionLeader = PlayerService.getPlayer(UUID.fromString(FactionLogic.getLeader(enemyFactionName))).get();
                                enemyFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, "Faction ", TextColors.GOLD, playerFactionName, TextColors.WHITE, " has declared you a ", TextColors.RED, "War!"));

                                CommandResult.success();
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This faction is already your enemy!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This faction is your ally! Remove alliance first to declare a war!"));
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
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to use this command!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
