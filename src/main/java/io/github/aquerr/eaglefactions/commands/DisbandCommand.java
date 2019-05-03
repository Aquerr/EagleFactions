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

public class DisbandCommand extends AbstractCommand
{
    public DisbandCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        Player player = (Player) source;
        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        Faction playerFaction = optionalPlayerFaction.get();

        if(playerFaction.getName().equalsIgnoreCase("SafeZone") || playerFaction.getName().equalsIgnoreCase("WarZone"))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This faction cannot be disbanded!"));

        //If player has adminmode
        if(EagleFactions.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            boolean didSucceed = super.getPlugin().getFactionLogic().disbandFaction(playerFaction.getName());
            if(didSucceed)
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_DISBANDED));
                EagleFactions.AUTO_CLAIM_LIST.remove(player.getUniqueId());
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, PluginMessages.SOMETHING_WENT_WRONG));
            }
            return CommandResult.success();
        }

        //If player is leader
        if(!playerFaction.getLeader().equals(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));

        boolean didSucceed = super.getPlugin().getFactionLogic().disbandFaction(playerFaction.getName());
        if(didSucceed)
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_DISBANDED));
            EagleFactions.AUTO_CLAIM_LIST.remove(player.getUniqueId());
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, PluginMessages.SOMETHING_WENT_WRONG));
        }

        return CommandResult.success();
    }
}
