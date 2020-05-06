package io.github.aquerr.eaglefactions.common.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Optional;

public class RenameCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public RenameCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final String newFactionName = context.requireOne("name");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        if (newFactionName.equalsIgnoreCase("SafeZone") || newFactionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_USE_THIS_FACTION_NAME));

        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        if (!optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));

        if (super.getPlugin().getFactionLogic().getFactionsNames().contains(newFactionName.toLowerCase()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS));

        if(newFactionName.length() > this.factionsConfig.getMaxNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxNameLength() + " " + Messages.CHARS + ")"));
        if(newFactionName.length() < this.factionsConfig.getMinNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinNameLength() + " " + Messages.CHARS + ")"));

        super.getPlugin().getFactionLogic().renameFaction(optionalPlayerFaction.get(), newFactionName);
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.SUCCESSFULLY_RENAMED_FACTION_TO_FACTION_NAME, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, newFactionName)))));

        return CommandResult.success();
    }
}
