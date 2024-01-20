package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AutoClaimCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final MessageService messageService;

    public AutoClaimCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        if (!permsManager.hasPermission(player.uniqueId(), faction, FactionPermission.TERRITORY_CLAIM)
                && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        if (EagleFactionsPlugin.AUTO_CLAIM_LIST.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.auto-claim.disabled"));
        }
        else
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.add(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.auto-claim.enabled"));
        }

        return CommandResult.success();
    }
}
