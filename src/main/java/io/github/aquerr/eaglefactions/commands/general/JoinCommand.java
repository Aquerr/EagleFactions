package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class JoinCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final InvitationManager invitationManager;
    private final MessageService messageService;

    public JoinCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.invitationManager = plugin.getInvitationManager();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());

        final ServerPlayer player = requirePlayerSource(context);
        if (super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId()).isPresent())
            throw messageService.resolveExceptionWithMessage("error.command.join.you-are-already-in-a-faction");

        //If player has admin mode then force join.
        if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
        {
            invitationManager.joinAndNotify(player, faction);
            return CommandResult.success();
        }

        //TODO: Should public factions bypass this restriction?
        if(hasReachedPlayerLimit(faction))
            throw messageService.resolveExceptionWithMessage("error.command.join.you-cant-join-this-faction-player-limit-reached");

        if(!faction.isPublic())
        {
            FactionInvite factionInvite = EagleFactionsPlugin.INVITE_LIST.stream()
                    .filter(invite -> invite.getInvitedPlayerUniqueId().equals(player.uniqueId()) && invite.getSenderFaction().equals(faction.getName()))
                    .findFirst()
                    .orElseThrow(() -> messageService.resolveExceptionWithMessage("error.command.join.you-have-not-been-invited-to-this-faction"));

            factionInvite.accept();
            return CommandResult.success();
        }

        this.invitationManager.joinAndNotify(player, faction);
        return CommandResult.success();
    }

    private boolean hasReachedPlayerLimit(Faction faction)
    {
        return this.factionsConfig.isPlayerLimit() && faction.getPlayers().size() >= this.factionsConfig.getPlayerLimit();
    }
}
