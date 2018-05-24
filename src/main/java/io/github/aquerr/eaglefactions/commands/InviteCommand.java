package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.FlagManager;
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

public class InviteCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalInvitedPlayer = context.<Player>getOne("player");

        if(optionalInvitedPlayer.isPresent())
        {
            if(source instanceof Player)
            {
                Player senderPlayer = (Player)source;
                Player invitedPlayer = optionalInvitedPlayer.get();
                Optional<Faction> optionalSenderFaction = FactionLogic.getFactionByPlayerUUID(senderPlayer.getUniqueId());

                if(optionalSenderFaction.isPresent())
                {
                    Faction senderFaction = optionalSenderFaction.get();

                    if (FlagManager.canInvite(senderPlayer, senderFaction))
                    {
                        if(MainLogic.isPlayerLimit())
                        {
                            int playerCount = 0;
                            playerCount += senderFaction.Leader.equals("") ? 0 : 1;
                            playerCount += senderFaction.Officers.isEmpty() ? 0 : senderFaction.Officers.size();
                            playerCount += senderFaction.Members.isEmpty() ? 0 : senderFaction.Members.size();

                            if(playerCount >= MainLogic.getPlayerLimit())
                            {
                                senderPlayer.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION + " " + PluginMessages.FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED));
                                return CommandResult.success();
                            }
                        }

                        if(!FactionLogic.getFactionByPlayerUUID(invitedPlayer.getUniqueId()).isPresent())
                        {
                            try
                            {
                                Invite invite = new Invite(senderFaction.Name, invitedPlayer.getUniqueId());
                                EagleFactions.InviteList.add(invite);

                                invitedPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, senderFaction.Name, TextColors.GREEN, " " + PluginMessages.HAS_SENT_YOU_AN_INVITE + " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                        " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f join " + senderFaction.Name, TextColors.WHITE, " " + PluginMessages.TO_JOIN));

                                senderPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, PluginMessages.YOU_INVITED + " ", TextColors.GOLD, invitedPlayer.getName(), TextColors.GREEN, " " + PluginMessages.TO_YOUR_FACTION));

                                //TODO: Create a separate listener for removing invitations.

                                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                taskBuilder.execute(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(EagleFactions.InviteList.contains(invite) && EagleFactions.InviteList != null)
                                        {
                                            EagleFactions.InviteList.remove(invite);
                                        }
                                    }
                                }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(EagleFactions.getEagleFactions());

                                return CommandResult.success();
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                            }

                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PLAYER_IS_ALREADY_IN_A_FACTION));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_INVITE_PLAYERS_TO_FACTION));
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
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f invite <player>"));
        }

        return CommandResult.success();


    }
}
