package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Container;

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
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());

        if (!this.factionsConfig.canUseFactionChest())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.FACTION_CHESTS_ARE_DISABLED, NamedTextColor.RED)));

        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

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
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS, NamedTextColor.RED)));

            final Faction faction = optionalFaction.get();
            final boolean isCancelled = EventRunner.runFactionChestEventPre(player, faction);
            if (isCancelled)
                return CommandResult.success();
            openFactionChest(player, faction);
            EventRunner.runFactionChestEventPost(player, faction);
            return CommandResult.success();
        }

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        final Faction faction = optionalPlayerFaction.get();
        final boolean isCancelled = EventRunner.runFactionChestEventPre(player, faction);
        if (isCancelled)
            return CommandResult.success();
        openFactionChest(player, faction);
        EventRunner.runFactionChestEventPost(player, faction);
        return CommandResult.success();
    }

    private void openFactionChest(final ServerPlayer player, final Faction faction)
    {
        final Optional<Container> optionalContainer = player.openInventory(faction.getChest().getInventory());
        if(optionalContainer.isPresent())
        {
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_OPENED_FACTION_CHEST, NamedTextColor.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Component.text(faction.getName(), NamedTextColor.GOLD)))).color(NamedTextColor.GREEN));
        }
    }
}