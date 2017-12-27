package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
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
                String senderFactionName = FactionLogic.getFactionName(senderPlayer.getUniqueId());

                if(senderFactionName != null)
                {
                    if(MainLogic.isPlayerLimit())
                    {
                        int playerCount = 0;
                        Faction faction = FactionLogic.getFaction(senderFactionName);
                        playerCount += faction.Leader.equals("") ? 0 : 1;
                        playerCount += faction.Officers.isEmpty() ? 0 : faction.Officers.size();
                        playerCount += faction.Members.isEmpty() ? 0 : faction.Members.size();

                        if(playerCount >= MainLogic.getPlayerLimit())
                        {
                            senderPlayer.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't invite more players to your faction. Faction's player limit has been reached!"));
                            return CommandResult.success();
                        }
                    }

                    if(FactionLogic.getFactionName(invitedPlayer.getUniqueId()) == null)
                    {
                        try
                        {
                            Invite invite = new Invite(senderFactionName, invitedPlayer.getUniqueId());
                            EagleFactions.InviteList.add(invite);

                            invitedPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction ", TextColors.GOLD, senderFactionName, TextColors.GREEN, " has sent you an invite! You have 2 minutes to accept it!" +
                                    " Type ", TextColors.GOLD, "/f join " + senderFactionName, TextColors.WHITE, " to join."));

                            senderPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, "You invited ", TextColors.GOLD, invitedPlayer.getName(), TextColors.GREEN, " to your faction."));

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
                            }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());

                            return CommandResult.success();
                        }
                        catch (Exception exception)
                        {
                            exception.printStackTrace();
                        }

                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Player is already in a faction!"));
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
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments!"));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f invite <player>"));
        }

        return CommandResult.success();


    }
}
