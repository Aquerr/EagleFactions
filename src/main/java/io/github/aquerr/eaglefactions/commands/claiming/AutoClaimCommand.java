package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AutoClaimCommand extends AbstractCommand
{
    private final MessageService messageService;

    public AutoClaimCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        if (!faction.getLeader().equals(player.uniqueId()) && !faction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

        if (EagleFactionsPlugin.AUTO_CLAIM_LIST.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.auto-claim.turned-off"));
        }
        else
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.add(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.auto-claim.turned-on"));
        }

        return CommandResult.success();
    }
}
