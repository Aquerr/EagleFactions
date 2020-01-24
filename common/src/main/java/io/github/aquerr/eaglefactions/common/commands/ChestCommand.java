package io.github.aquerr.eaglefactions.common.commands;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Optional;

public class ChestCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public ChestCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<String> optionalFactionName = context.<String>getOne("faction name");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        if (!this.factionsConfig.canUseFactionChest())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_CHESTS_ARE_DISABLED));

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
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));

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
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction faction = optionalPlayerFaction.get();
        final boolean isCancelled = EventRunner.runFactionChestEvent(player, faction);
        if (isCancelled)
            return CommandResult.success();
        openFactionChest(player, faction);
        return CommandResult.success();
    }

    private void openFactionChest(final Player player, final Faction faction)
    {
        final Optional<Container> optionalContainer = player.openInventory(faction.getChest().getInventory());
        if(optionalContainer.isPresent())
        {
//            player.sendMessage(Messages.YOU_OPENED_FACTION_CHEST.apply(ImmutableMap.of("FACTION_NAME", Text.of(faction.getName()))).build());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_OPENED_FACTION_CHEST, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));
        }
    }
}