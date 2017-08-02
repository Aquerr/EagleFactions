package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.managers.FactionManager;
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

import java.util.concurrent.TimeUnit;

public class InviteCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Player invitedPlayer = context.<Player>getOne("player").get();

        if(source instanceof Player)
        {
            Player factionPlayer = (Player)source;

            String playerFactionName = FactionManager.getFaction(factionPlayer.getUniqueId());

            if(playerFactionName != null)
            {
                if(FactionManager.getFaction(invitedPlayer.getUniqueId()) == null)
                {
                    try
                    {
                        //TODO: Create an invitation here and send it to the invited player.
                        Invite invite = new Invite(playerFactionName, invitedPlayer.getUniqueId());
                        EagleFactions.InviteList.add(invite);

                        invitedPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction ", TextColors.GOLD, playerFactionName, TextColors.GREEN, " has sent you an invite! You have 2 minutes to accept it!"));

                        factionPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, "You invited ", TextColors.GOLD, invitedPlayer.getName(), TextColors.GREEN, " to your faction."));

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
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Player already is in a faction!"));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to invite players!"));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Must be an ingame player to use this command!"));
        }

        return CommandResult.success();


    }
}
