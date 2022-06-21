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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Container;

import java.util.Collections;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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
        if (!this.factionsConfig.canUseFactionChest())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.FACTION_CHESTS_ARE_DISABLED, RED)));

        ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());
        if(optionalFaction.isPresent())
        {
            return openOther(player, optionalFaction.get());
        }

        final Faction playerFaction = requirePlayerFaction(player);
        return openSelf(player, playerFaction);
    }

    private CommandResult openSelf(ServerPlayer player, Faction faction) throws CommandException
    {
        if (isAdmin(player))
        {
            return open(player, faction);
        }

        if (!super.getPlugin().getPermsManager().canUseChest(player.uniqueId(), faction))
        {
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_OPEN_FACTION_CHEST, RED)));
        }

        return open(player, faction);
    }

    private CommandResult openOther(ServerPlayer player, Faction faction) throws CommandException
    {
        if (isAdmin(player))
        {
            return open(player, faction);
        }

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (optionalPlayerFaction.isPresent() && optionalPlayerFaction.get().getName().equals(faction.getName()))
        {
            return openSelf(player, faction);
        }
        else
        {
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS, RED)));
        }
    }

    private CommandResult open(ServerPlayer player, Faction faction)
    {
        final boolean isCancelled = EventRunner.runFactionChestEventPre(player, faction);
        if (isCancelled)
            return CommandResult.success();
        openFactionChest(player, faction);
        EventRunner.runFactionChestEventPost(player, faction);
        return CommandResult.success();
    }

    private void openFactionChest(final ServerPlayer player, final Faction faction)
    {
        final Optional<Container> optionalContainer = faction.getChest().getInventory().open(player);
        if(optionalContainer.isPresent())
        {
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_OPENED_FACTION_CHEST, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(faction.getName(), GOLD)))));
        }
    }

    private boolean isAdmin(ServerPlayer player)
    {
        return super.getPlugin().getPlayerManager().hasAdminMode(player.user());
    }
}