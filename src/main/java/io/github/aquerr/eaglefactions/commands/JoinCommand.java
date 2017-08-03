package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class JoinCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        String factionName = context.<String>getOne("faction name").get();

        if(source instanceof Player)
        {
            Player player = (Player)source;

            if(FactionLogic.getFaction(player.getUniqueId()) == null)
            {

                if(FactionLogic.getFactions().contains(factionName))
                {
                    //Invite invite = new Invite(factionName, player.getUniqueId());

                    //player.sendMessage(Text.of("Trying join faction with following invite..."));
                    //player.sendMessage(Text.of(invite.getFactionName() + " | " + invite.getPlayerUUID()));
//
                    //boolean inviteExists = EagleFactions.InviteList.contains(invite);
                    //boolean inviteListNull = EagleFactions.InviteList == null;
//
//
                    //player.sendMessage(Text.of("Does invite list contain your invite? = " + inviteExists));
                    //player.sendMessage(Text.of("Is invite list null? = " + inviteListNull));



                    //if(EagleFactions.InviteList.contains(invite) && EagleFactions.InviteList != null)
                    //{
                        for (Invite invite: EagleFactions.InviteList)
                        {
                            if(invite.getPlayerUUID().equals(player.getUniqueId()) && invite.getFactionName().equals(factionName))
                            {
                                try
                                {

                                    //TODO: Create a listener which will notify all players in faction that someone has joined.
                                    FactionLogic.joinFaction(player.getUniqueId(), factionName);

                                    //TODO: Remove invite from InviteList if player joins a faction.

                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully joined faction ", TextColors.GOLD, factionName));
                                    return CommandResult.success();
                                }
                                catch (Exception exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                        }

                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You haven't been invited to this faction."));

                    //else if(!EagleFactions.InviteList.contains(invite))
                    //{
                    //    for (Invite test: EagleFactions.InviteList)
                    //    {
                    //        source.sendMessage(Text.of(test.getFactionName() + " | " + test.getPlayerUUID().toString() + "\n"));
                    //    }
//
                    //    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You haven't been invited to this faction."));
                    //}
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, factionName));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You already are in a faction."));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
