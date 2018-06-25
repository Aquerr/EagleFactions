package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class JoinCommand implements CommandExecutor
{

    @Inject
    private Settings settings;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.getOne("faction name");

        if (optionalFactionName.isPresent())
        {
            if (source instanceof Player)
            {
                Player player = (Player) source;
                String rawFactionName = optionalFactionName.get();

                if (!FactionLogic.getFactionByPlayerUUID(player.getUniqueId()).isPresent())
                {
                    Faction faction = FactionLogic.getFactionByName(rawFactionName);
                    if (faction == null)
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName + "!"));
                        return CommandResult.success();
                    } else
                    {
                        //If player has admin mode then force join.
                        if (EagleFactions.AdminList.contains(player.getUniqueId()))
                        {
                            FactionLogic.joinFaction(player.getUniqueId(), faction.Name);
                            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.SUCCESSFULLY_JOINED_FACTION + " ", TextColors.GOLD, faction.Name));

                            return CommandResult.success();
                        }

                        for (Invite invite : EagleFactions.InviteList)
                        {
                            if (invite.getPlayerUUID().equals(player.getUniqueId()) && invite.getFactionName().equals(faction.Name))
                            {
                                try
                                {
                                    if (settings.isPlayerLimit())
                                    {
                                        int playerCount = 0;
                                        playerCount += faction.Leader.equals("") ? 0 : 1;
                                        playerCount += faction.Officers.isEmpty() ? 0 : faction.Officers.size();
                                        playerCount += faction.Members.isEmpty() ? 0 : faction.Members.size();
                                        playerCount += faction.Recruits.isEmpty() ? 0 : faction.Recruits.size();

                                        if (playerCount >= settings.getPlayerLimit())
                                        {
                                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_JOIN_THIS_FACTION_BECAUSE_IT_REACHED_ITS_PLAYER_LIMIT));
                                            return CommandResult.success();
                                        }
                                    }

                                    //TODO: Create a listener which will notify all players in faction that someone has joined.
                                    FactionLogic.joinFaction(player.getUniqueId(), faction.Name);

                                    EagleFactions.InviteList.remove(new Invite(faction.Name, player.getUniqueId()));

                                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.SUCCESSFULLY_JOINED_FACTION + " ", TextColors.GOLD, faction.Name));
                                    return CommandResult.success();
                                } catch (Exception exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                        }
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_HAVENT_BEEN_INVITED_TO_THIS_FACTION));
                    }
                } else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_ALREADY_IN_A_FACTION));
                }
            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f join <faction name>"));
        }

        return CommandResult.success();
    }
}
