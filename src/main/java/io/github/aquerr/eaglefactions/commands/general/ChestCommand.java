package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Container;

import java.util.Optional;

public class ChestCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final MessageService messageService;

    public ChestCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        if (!this.factionsConfig.canUseFactionChest())
            throw messageService.resolveExceptionWithMessage("error.command.chest.chests-are-disabled");

        ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.optionalFaction());
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
            throw messageService.resolveExceptionWithMessage("error.command.chest.players-with-your-rank-cant-open-faction-chests");
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
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_ADMIN_MODE_REQUIRED);
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
            player.sendMessage(messageService.resolveMessageWithPrefix("command.chest.you-opened-faction-chest"));
        }
    }

    private boolean isAdmin(ServerPlayer player)
    {
        return super.getPlugin().getPlayerManager().hasAdminMode(player.user());
    }
}