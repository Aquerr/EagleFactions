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
                openFactionChest(player, optionalPlayerFaction.get());
                return CommandResult.success();
            }

            if(!EagleFactions.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You need to toggle faction's admin-mode first to view other faction's chest."));

            final Faction nullableFaction = super.getPlugin().getFactionLogic().getFactionByName(optionalFactionName.get());
            if(nullableFaction != null)
            {
                openFactionChest(player, nullableFaction);
                return CommandResult.success();
            }
        }

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction faction = optionalPlayerFaction.get();
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