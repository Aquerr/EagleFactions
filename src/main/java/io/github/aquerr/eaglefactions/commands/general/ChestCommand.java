package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
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
        final Optional<Faction> optionalFaction = context.getOne("faction");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        if (!this.factionsConfig.canUseFactionChest())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_CHESTS_ARE_DISABLED));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(optionalFaction.isPresent())
        {
            if(optionalPlayerFaction.isPresent() && optionalPlayerFaction.get().getName().equals(optionalFaction.get().getName()))
            {
                final boolean isCancelled = EventRunner.runFactionChestEventPre(player, optionalPlayerFaction.get());
                if (isCancelled)
                    return CommandResult.success();
                openFactionChest(player, optionalPlayerFaction.get());
                EventRunner.runFactionChestEventPost(player, optionalPlayerFaction.get());
                return CommandResult.success();
            }

            if(!super.getPlugin().getPlayerManager().hasAdminMode(player))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));

            final Faction faction = optionalFaction.get();
            final boolean isCancelled = EventRunner.runFactionChestEventPre(player, faction);
            if (isCancelled)
                return CommandResult.success();
            openFactionChest(player, faction);
            EventRunner.runFactionChestEventPost(player, faction);
            return CommandResult.success();
        }

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction faction = optionalPlayerFaction.get();
        final boolean isCancelled = EventRunner.runFactionChestEventPre(player, faction);
        if (isCancelled)
            return CommandResult.success();
        openFactionChest(player, faction);
        EventRunner.runFactionChestEventPost(player, faction);
        return CommandResult.success();
    }

    private void openFactionChest(final Player player, final Faction faction)
    {
        final Optional<Container> optionalContainer = player.openInventory(faction.getChest().getInventory());
        if(optionalContainer.isPresent())
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_OPENED_FACTION_CHEST, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));
        }
    }
}