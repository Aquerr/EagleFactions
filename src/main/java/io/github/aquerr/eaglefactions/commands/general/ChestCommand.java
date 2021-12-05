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
        if (!this.factionsConfig.canUseFactionChest())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_CHESTS_ARE_DISABLED));

        Player player = requirePlayerSource(source);
        final Optional<Faction> optionalFaction = context.getOne("faction");
        if(optionalFaction.isPresent())
        {
            return openOther(player, optionalFaction.get());
        }

        final Faction playerFaction = requirePlayerFaction(player);
        return openSelf(player, playerFaction);
    }

    private CommandResult openSelf(Player player, Faction faction) throws CommandException
    {
        if (isAdmin(player))
        {
            return open(player, faction);
        }

        if (!super.getPlugin().getPermsManager().canUseChest(player.getUniqueId(), faction))
        {
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_OPEN_FACTION_CHEST));
        }

        return open(player, faction);
    }

    private CommandResult openOther(Player player, Faction faction) throws CommandException
    {
        if (isAdmin(player))
        {
            return open(player, faction);
        }

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (optionalPlayerFaction.isPresent() && optionalPlayerFaction.get().getName().equals(faction.getName()))
        {
            return openSelf(player, faction);
        }
        else
        {
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));
        }
    }

    private CommandResult open(Player player, Faction faction)
    {
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

    private boolean isAdmin(Player player)
    {
        return super.getPlugin().getPlayerManager().hasAdminMode(player);
    }
}