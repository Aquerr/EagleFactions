package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
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

public class RenameCommand extends AbstractCommand implements CommandExecutor
{
    public RenameCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne("faction name");

        if (!optionalFactionName.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f rename <faction name>"));
            return CommandResult.success();
        }
        else
        {
            if (!(source instanceof Player))
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
                return CommandResult.success();
            }

            Player player = (Player) source;
            String newFactionName = optionalFactionName.get();

            //Check if new name is safezone or warzone
            if (newFactionName.equalsIgnoreCase("SafeZone") || newFactionName.equalsIgnoreCase("WarZone"))
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_USE_THIS_FACTION_NAME));
                return CommandResult.success();
            }

            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            if (!optionalPlayerFaction.isPresent())
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                return CommandResult.success();
            }

            //Check if player is leader
            if (!optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()))
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));
                return CommandResult.success();
            }

            //Check if factions with such name already exists
            if (getPlugin().getFactionLogic().getFactionsNames().contains(newFactionName.toLowerCase()))
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS));
                return CommandResult.success();
            }

            //Check name length
            if(newFactionName.length() > getPlugin().getConfiguration().getConfigFields().getMaxNameLength())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + PluginMessages.MAX + " " + getPlugin().getConfiguration().getConfigFields().getMaxNameLength() + " " + PluginMessages.CHARS + ")"));
                return CommandResult.success();
            }
            if(newFactionName.length() < getPlugin().getConfiguration().getConfigFields().getMinNameLength())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + PluginMessages.MIN + " " + getPlugin().getConfiguration().getConfigFields().getMinNameLength() + " " + PluginMessages.CHARS + ")"));
                return CommandResult.success();
            }

            //Rename function

            super.getPlugin().getFactionLogic().renameFaction(optionalPlayerFaction.get(), newFactionName);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully renamed faction to " + newFactionName + "!"));
        }

        return CommandResult.success();
    }
}
