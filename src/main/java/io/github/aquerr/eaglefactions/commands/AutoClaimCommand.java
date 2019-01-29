package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class AutoClaimCommand extends AbstractCommand
{
    public AutoClaimCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if(playerFaction.getLeader().equals(player.getUniqueId()) || playerFaction.getOfficers().contains(player.getUniqueId()))
                {
                    if(EagleFactions.AutoClaimList.contains(player.getUniqueId()))
                    {
                        EagleFactions.AutoClaimList.remove(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.OFF));

                        return CommandResult.success();
                    }
                    else
                    {
                        EagleFactions.AutoClaimList.add(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.ON));

                        return CommandResult.success();
                    }
                }
                else if(EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    if(EagleFactions.AutoClaimList.contains(player.getUniqueId()))
                    {
                        EagleFactions.AutoClaimList.remove(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.OFF));

                        return CommandResult.success();
                    }
                    else
                    {
                        EagleFactions.AutoClaimList.add(player.getUniqueId());
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, "AutoClaim", TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.ON));

                        return CommandResult.success();
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
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


        return CommandResult.success();
    }
}
