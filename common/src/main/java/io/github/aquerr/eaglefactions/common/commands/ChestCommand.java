package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ChestCommand extends AbstractCommand
{
    public ChestCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<String> optionalFactionName = context.<String>getOne("faction name");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        if (!super.getPlugin().getConfiguration().getConfigFields().canUseFactionChest())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Factions' chests are turned off on this server."));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(optionalFactionName.isPresent())
        {
            if(optionalPlayerFaction.isPresent() && optionalPlayerFaction.get().getName().equals(optionalFactionName.get()))
            {
                final boolean isCancelled = EventRunner.runFactionChestEvent(player, optionalPlayerFaction.get());
                if (isCancelled)
                    return CommandResult.success();
                openFactionChest(player, optionalPlayerFaction.get());
                return CommandResult.success();
            }

            if(!EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You need to toggle faction's admin-mode first to view other faction's chest."));

            final Faction nullableFaction = super.getPlugin().getFactionLogic().getFactionByName(optionalFactionName.get());
            if(nullableFaction != null)
            {
                final boolean isCancelled = EventRunner.runFactionChestEvent(player, nullableFaction);
                if (isCancelled)
                    return CommandResult.success();
                openFactionChest(player, nullableFaction);
                return CommandResult.success();
            }
        }

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction faction = optionalPlayerFaction.get();
        final boolean isCancelled = EventRunner.runFactionChestEvent(player, faction);
        if (isCancelled)
            return CommandResult.success();
        openFactionChest(player, faction);
        return CommandResult.success();
    }

    private void openFactionChest(final Player player, final Faction faction)
    {
        final Optional<Container> optionalContainer = player.openInventory(faction.getChest().toInventory());
        if(optionalContainer.isPresent())
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Text.of("You opened " + faction.getName() + " faction's chest!")));
        }
    }
}