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
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ChestCommand extends AbstractCommand
{
    public ChestCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        if (!super.getPlugin().getConfiguration().getConfigFields().canUseFactionChest())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Factions' chests are turned off on this server."));

        final Player player = (Player) source;
        Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if (!optionalFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction faction = optionalFaction.get();

        final Optional<Container> optionalContainer = player.openInventory(faction.getChest().toInventory(), Cause.of(NamedCause.source(EagleFactions.getPlugin())));
        if (optionalContainer.isPresent())
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Text.of("You opened faction's chest!")));

        return CommandResult.success();
    }
}