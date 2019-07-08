package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.AllyRequest;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
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

public class AllyCommand extends AbstractCommand
{
    public AllyCommand(EagleFactionsPlugin plugin)
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
                String rawFactionName = optionalFactionName.get();
                Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                Faction selectedFaction = getPlugin().getFactionLogic().getFactionByName(rawFactionName);

                if(selectedFaction == null)
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName, TextColors.RED, "!"));
                    return CommandResult.success();
                }

                if(optionalPlayerFaction.isPresent())
                {
                    Faction playerFaction = optionalPlayerFaction.get();

                    if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                    {
                        if(playerFaction.getEnemies().contains(selectedFaction.getName()))
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + PluginMessages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));
                        }
                        else
                        {
                            if(playerFaction.getAlliances().contains(selectedFaction.getName()))
                            {
                                //Remove ally
                                getPlugin().getFactionLogic().removeAlly(playerFaction.getName(), selectedFaction.getName());
                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, PluginMessages.YOU_DISBANDED_YOUR_ALLIANCE_WITH + " ", TextColors.GOLD, selectedFaction.getName(), TextColors.GREEN, "!"));
                            }
                            else
                            {
                                //Add ally
                                getPlugin().getFactionLogic().addAlly(playerFaction.getName(), selectedFaction.getName());
                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_ADDED_TO_THE_ALLIANCE));
                            }
                        }
                        return CommandResult.success();
                    }

                    if(!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()))
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
                    }
                    else
                    {
                        if(playerFaction.getEnemies().contains(selectedFaction.getName()))
                        {
                            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + PluginMessages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));
                        }
                        else
                        {
                            if(playerFaction.getAlliances().contains(selectedFaction.getName()))
                            {
                                //Remove ally
                                getPlugin().getFactionLogic().removeAlly(playerFaction.getName(), selectedFaction.getName());
                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, PluginMessages.YOU_DISBANDED_YOUR_ALLIANCE_WITH + " ", TextColors.GOLD, selectedFaction.getName(), TextColors.GREEN, "!"));
                            }
                            else
                            {
                                AllyRequest checkInvite = new AllyRequest(selectedFaction.getName(), playerFaction.getName());

                                //TODO: Check if player is online
                                Optional<Player> optionalInvitedFactionLeader = getPlugin().getPlayerManager().getPlayer(selectedFaction.getLeader());

                                if(EagleFactionsPlugin.ALLY_INVITE_LIST.contains(checkInvite))
                                {
                                    getPlugin().getFactionLogic().addAlly(playerFaction.getName(), selectedFaction.getName());

                                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.YOU_HAVE_ACCEPTED_AN_INVITATION_FROM + " ", TextColors.GOLD, selectedFaction.getName() + "!"));
                                    optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.ACCEPTED_YOUR_YOUR_INVITE_TO_THE_ALLIANCE)));

                                    EagleFactionsPlugin.ALLY_INVITE_LIST.remove(checkInvite);
                                }
                                else if(!EagleFactionsPlugin.ALLY_INVITE_LIST.contains(checkInvite))
                                {
                                    AllyRequest invite = new AllyRequest(playerFaction.getName(), selectedFaction.getName());
                                    if(EagleFactionsPlugin.ALLY_INVITE_LIST.contains(invite))
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, "You have already invited this factions to the alliance. Wait for their response!"));
                                        return CommandResult.success();
                                    }
                                    EagleFactionsPlugin.ALLY_INVITE_LIST.add(invite);

                                    optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.HAS_SENT_YOU_AN_INVITE_TO_THE + " ", TextColors.AQUA, PluginMessages.ALLIANCE, TextColors.WHITE, "! " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                            " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f ally " + playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.TO_ACCEPT_INVITATION)));

                                    //TODO: Send message about invitation to officers.

                                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, PluginMessages.YOU_HAVE_INVITED_FACTION + " ", TextColors.GOLD, selectedFaction.getName(), TextColors.WHITE, " " + PluginMessages.TO_THE_ALLIANCE));

                                    Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                                    taskBuilder.execute(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            EagleFactionsPlugin.ALLY_INVITE_LIST.remove(invite);
                                        }
                                    }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(Sponge.getPluginManager().getPlugin(PluginInfo.ID).get().getInstance().get());
                                    return CommandResult.success();
                                }
                            }
                        }
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
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f ally <faction name>"));
            return CommandResult.success();
        }

        return CommandResult.success();
    }
}
