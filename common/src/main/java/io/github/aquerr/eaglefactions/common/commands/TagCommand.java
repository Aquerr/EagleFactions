package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
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

public class TagCommand extends AbstractCommand
{
    public TagCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<String> optionalNewTag = context.<String>getOne("tag");

        if (!optionalNewTag.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f tag <new_tag>"));
            return CommandResult.success();
        }
        else
        {
            if (!(source instanceof Player))
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
                return CommandResult.success();
            }

            final Player player = (Player) source;
            final String newFactionTag = optionalNewTag.get();

            final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
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

            //Check if faction with such tag already exists
            if(super.getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(newFactionTag)))
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN));
                return CommandResult.success();
            }

            //Check tag length
            if(newFactionTag.length() > getPlugin().getConfiguration().getConfigFields().getMaxTagLength())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + PluginMessages.MAX + " " + getPlugin().getConfiguration().getConfigFields().getMaxTagLength() + " " + PluginMessages.CHARS + ")"));
                return CommandResult.success();
            }
            if(newFactionTag.length() < getPlugin().getConfiguration().getConfigFields().getMinTagLength())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + PluginMessages.MIN + " " + getPlugin().getConfiguration().getConfigFields().getMinTagLength() + " " + PluginMessages.CHARS + ")"));
                return CommandResult.success();
            }

            //Change tag function

            super.getPlugin().getFactionLogic().changeTag(optionalPlayerFaction.get(), newFactionTag);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully changed faction's tag to " + newFactionTag + "!"));
        }

        return CommandResult.success();
    }
}
