package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.AllyInvite;
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

/**
 * Created by Aquerr on 2017-08-04.
 */
public class AddAllyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        String invitedFactionName = context.<String>getOne(Text.of("faction name")).get();

        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    if(FactionLogic.getFactionsNames().contains(invitedFactionName))
                    {
                        if(!FactionLogic.getEnemies(playerFactionName).contains(invitedFactionName))
                        {
                            if(!FactionLogic.getAlliances(playerFactionName).contains(invitedFactionName))
                            {
                                AllyInvite checkInvite = new AllyInvite(invitedFactionName, playerFactionName);

                                Player invitedFactionLeader = PlayerService.getPlayer(UUID.fromString(FactionLogic.getLeader(invitedFactionName))).get();

                                if(EagleFactions.AllayInviteList.contains(checkInvite))
                                {
                                    FactionLogic.addAllay(playerFactionName, invitedFactionName);

                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "You have accepted an invitation from ", TextColors.GOLD, invitedFactionName + "!"));

                                    invitedFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction ", TextColors.GOLD, playerFactionName, TextColors.WHITE, " accepted your invite to the alliance."));

                                    EagleFactions.AllayInviteList.remove(checkInvite);
                                }
                                else if(!EagleFactions.AllayInviteList.contains(checkInvite))
                                {
                                    AllyInvite invite = new AllyInvite(playerFactionName, invitedFactionName);
                                    EagleFactions.AllayInviteList.add(invite);

                                    invitedFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, "Faction ", TextColors.GOLD, playerFactionName, TextColors.WHITE, " has sent you an invite to the ", TextColors.AQUA, "alliance, ", TextColors.WHITE, "! You have 2 minutes to accept it!"));
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.WHITE, "You invited faction ", TextColors.GOLD, invitedFactionName, TextColors.WHITE, " to the alliance."));

                                    Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                    taskBuilder.execute(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            if(EagleFactions.AllayInviteList.contains(invite) && EagleFactions.AllayInviteList != null)
                                            {
                                                EagleFactions.AllayInviteList.remove(invite);
                                            }
                                        }
                                    }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());

                                    CommandResult.success();

                                }
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are in alliance with this faction!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are in WAR with this faction! Send a request for peace to this faction first!"));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, invitedFactionName + "!"));
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
