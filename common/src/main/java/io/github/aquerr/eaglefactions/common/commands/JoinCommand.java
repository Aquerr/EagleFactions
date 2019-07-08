package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.Invite;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class JoinCommand extends AbstractCommand
{
    public JoinCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Optional<String> optionalFactionName = context.<String>getOne("faction name");

        if (optionalFactionName.isPresent())
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                String rawFactionName = optionalFactionName.get();

                if(!getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId()).isPresent())
                {
                    Faction faction = getPlugin().getFactionLogic().getFactionByName(rawFactionName);
                    if (faction == null)
                    {
                        player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName + "!"));
                        return CommandResult.success();
                    }
                    else
                    {
                        //If player has admin mode then force join.
                        if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                        {
                            getPlugin().getFactionLogic().joinFaction(player.getUniqueId(), faction.getName());
                            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.SUCCESSFULLY_JOINED_FACTION + " ", TextColors.GOLD, faction.getName()));

                            return CommandResult.success();
                        }

                        for (Invite invite: EagleFactionsPlugin.INVITE_LIST)
                        {
                            if(invite.getPlayerUUID().equals(player.getUniqueId()) && invite.getFactionName().equals(faction.getName()))
                            {
                                try
                                {
                                    if(getPlugin().getConfiguration().getConfigFields().isPlayerLimit())
                                    {
                                        int playerCount = 0;
                                        playerCount += faction.getLeader().toString().equals("") ? 0 : 1;
                                        playerCount += faction.getOfficers().isEmpty() ? 0 : faction.getOfficers().size();
                                        playerCount += faction.getMembers().isEmpty() ? 0 : faction.getMembers().size();
                                        playerCount += faction.getRecruits().isEmpty() ? 0 : faction.getRecruits().size();

                                        if(playerCount >= getPlugin().getConfiguration().getConfigFields().getPlayerLimit())
                                        {
                                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_JOIN_THIS_FACTION_BECAUSE_IT_REACHED_ITS_PLAYER_LIMIT));
                                            return CommandResult.success();
                                        }
                                    }

                                    //TODO: Create a listener which will notify all players in faction that someone has joined.
                                    getPlugin().getFactionLogic().joinFaction(player.getUniqueId(), faction.getName());

                                    EagleFactionsPlugin.INVITE_LIST.remove(new Invite(faction.getName(), player.getUniqueId()));

                                    source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.SUCCESSFULLY_JOINED_FACTION + " ", TextColors.GOLD, faction.getName()));
                                    return CommandResult.success();
                                }
                                catch (Exception exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                        }
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_HAVENT_BEEN_INVITED_TO_THIS_FACTION));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_ARE_ALREADY_IN_A_FACTION));
                }
            }
            else
            {
                source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f join <faction name>"));
        }

        return CommandResult.success();
    }
}
