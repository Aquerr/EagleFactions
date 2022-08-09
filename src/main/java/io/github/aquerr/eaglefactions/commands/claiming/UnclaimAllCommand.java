package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class UnclaimAllCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final MessageService messageService;

    public UnclaimAllCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId())
                && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

        final boolean isCancelled = EventRunner.runFactionUnclaimEventPre(player, playerFaction, player.world(), null);
        if (isCancelled)
            return CommandResult.success();

        if(!this.factionsConfig.canPlaceHomeOutsideFactionClaim() && playerFaction.getHome() != null)
        {
            super.getPlugin().getFactionLogic().setHome(playerFaction, null);
        }

        final Faction faction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId()).get();
        super.getPlugin().getFactionLogic().removeAllClaims(faction);
        player.sendMessage(messageService.resolveMessageWithPrefix("command.unclaim-all.success"));
        EventRunner.runFactionUnclaimEventPost(player, playerFaction, player.world(), null);
        return CommandResult.success();
    }

}
