package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class DescriptionCommand extends AbstractCommand
{
    public DescriptionCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Optional<String> optionalDescription = context.<String>getOne("description");

        if (!optionalDescription.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, Messages.USAGE + " /f desc <description>"));
            return CommandResult.success();
        }

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        //Check if player is leader
        if (!optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()) && !optionalPlayerFaction.get().getOfficers().contains(player.getUniqueId()))
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
            return CommandResult.success();
        }

        final String description = optionalDescription.get();

        //Check description length
        if(description.length() > 255)
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.DESCRIPTION_IS_TOO_LONG + " (" + Messages.MAX + " " + 255 + " " + Messages.CHARS + ")"));
            return CommandResult.success();
        }

        super.getPlugin().getFactionLogic().setDescription(optionalPlayerFaction.get(), description);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GREEN, Messages.FACTION_DESCRIPTION_HAS_BEEN_UPDATED)));
        return CommandResult.success();
    }
}
