package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class KickCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalSelectedPlayer = context.<Player>getOne(Text.of("player"));

        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    if(optionalSelectedPlayer.isPresent())
                    {
                        Player selectedPlayer = optionalSelectedPlayer.get();

                        if(FactionLogic.getFactionName(selectedPlayer.getUniqueId()).equals(playerFactionName))
                        {
                            if(!FactionLogic.getLeader(playerFactionName).equals(selectedPlayer.getUniqueId().toString()))
                            {
                                if(!FactionLogic.getOfficers(playerFactionName).contains(selectedPlayer.getUniqueId().toString()) && FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                                {
                                    FactionLogic.kickPlayer(selectedPlayer.getUniqueId(), playerFactionName);

                                    //TODO: Add listener that will inform players in a faction that someone has left their faction.
                                    // player.sendMessage(Text.of(PluginInfo.PluginPrefix,TextColors.GREEN, "You left faction ", TextColors.GOLD, playerFactionName));

                                    if(EagleFactions.AutoClaimList.contains(selectedPlayer.getUniqueId().toString())) EagleFactions.AutoClaimList.remove(selectedPlayer.getUniqueId().toString());

                                    CommandResult.success();
                                }
                                else
                                {
                                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't kick this player!"));
                                }
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't kick this player!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This player is not in your faction!"));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no such player."));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You needs to be the leader or an officer to kick players from the faction."));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are not in a faction!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
