package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.HomeConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
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
    private final HomeConfig homeConfig;
    private final MessageService messageService;
    private final PermsManager permsManager;

    public UnclaimAllCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.homeConfig = plugin.getConfiguration().getHomeConfig();
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        if (!permsManager.hasPermission(player.uniqueId(), playerFaction, FactionPermission.TERRITORY_CLAIM)
                && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

        final boolean isCancelled = EventRunner.runFactionUnclaimEventPre(player, playerFaction, player.world(), null);
        if (isCancelled)
            return CommandResult.success();

        if(!this.homeConfig.canPlaceHomeOutsideFactionClaim() && playerFaction.getHome() != null)
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
