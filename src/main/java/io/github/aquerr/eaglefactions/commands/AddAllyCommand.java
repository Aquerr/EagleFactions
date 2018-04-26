package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.AllyInvite;
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

/**
 * Created by Aquerr on 2017-08-04.
 */
public class AddAllyCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne(Text.of("faction name"));

        if (optionalFactionName.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                String rawFactionName = optionalFactionName.get();
                String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());
                String invitedFactionName = FactionLogic.getRealFactionName(rawFactionName);

                if (invitedFactionName == null)
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName, TextColors.RED, "!"));
                    return CommandResult.success();
                }

                if (playerFactionName != null)
                {
                    if (EagleFactions.AdminList.contains(player.getUniqueId()))
                    {
                        if (!FactionLogic.getEnemies(playerFactionName).contains(invitedFactionName))
                        {
                            if (!FactionLogic.getAlliances(playerFactionName).contains(invitedFactionName))
                            {
                                FactionLogic.addAlly(playerFactionName, invitedFactionName);
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_ADDED_TO_THE_ALLIANCE));
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_IN_ALLIANCE_WITH_THIS_FACTION));
                            }
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + PluginMessages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));
                        }
                        return CommandResult.success();
                    }

                    if (FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                    {
                        if(!FactionLogic.getEnemies(playerFactionName).contains(invitedFactionName))
                        {
                            if(!FactionLogic.getAlliances(playerFactionName).contains(invitedFactionName))
                            {
                                AllyInvite checkInvite = new AllyInvite(invitedFactionName, playerFactionName);

                                //TODO: Check if player is online
                                Player invitedFactionLeader = PlayerManager.getPlayer(UUID.fromString(FactionLogic.getLeader(invitedFactionName))).get();

                                if(EagleFactions.AllayInviteList.contains(checkInvite))
                                {
                                    FactionLogic.addAlly(playerFactionName, invitedFactionName);

                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_HAVE_ACCEPTED_AN_INVITATION_FROM + " ", TextColors.GOLD, invitedFactionName + "!"));

                                    invitedFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.FACTION + " ", TextColors.GOLD, playerFactionName, TextColors.WHITE, " " + PluginMessages.ACCEPTED_YOUR_YOUR_INVITE_TO_THE_ALLIANCE));

                                    EagleFactions.AllayInviteList.remove(checkInvite);
                                }
                                else if(!EagleFactions.AllayInviteList.contains(checkInvite))
                                {
                                    AllyInvite invite = new AllyInvite(playerFactionName, invitedFactionName);
                                    EagleFactions.AllayInviteList.add(invite);

                                    invitedFactionLeader.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.WHITE, PluginMessages.FACTION + " ", TextColors.GOLD, playerFactionName, TextColors.WHITE, " " + PluginMessages.HAS_SENT_YOU_AN_INVITE_TO_THE + " ", TextColors.AQUA, PluginMessages.ALLIANCE, TextColors.WHITE, "! " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                            " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f ally add " + playerFactionName, TextColors.WHITE, " " + PluginMessages.TO_ACCEPT_INVITATION));

                                    //TODO: Send message about invitation to officers.

                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.WHITE, PluginMessages.YOU_HAVE_INVITED_FACTION + " ", TextColors.GOLD, invitedFactionName, TextColors.WHITE, " " + PluginMessages.TO_THE_ALLIANCE));

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
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_IN_ALLIANCE_WITH_THIS_FACTION));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + PluginMessages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));
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
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f ally add <faction name>"));
            return CommandResult.success();
        }

        return CommandResult.success();
    }
}
