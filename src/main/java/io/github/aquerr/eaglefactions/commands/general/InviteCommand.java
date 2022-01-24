package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class InviteCommand extends AbstractCommand
{
    private final FactionLogic factionLogic;
    private final PermsManager permsManager;
    private final FactionsConfig factionsConfig;
    private final InvitationManager invitationManager;

    public InviteCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.invitationManager = plugin.getInvitationManager();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer invitedPlayer = context.requireOne(CommonParameters.PLAYER);
        final ServerPlayer senderPlayer = requirePlayerSource(context);
        final Faction senderFaction = requirePlayerFaction(senderPlayer);
        if (!this.permsManager.canInvite(senderPlayer.uniqueId(), senderFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_INVITE_PLAYERS_TO_FACTION, RED)));

        if(hasReachedPlayerLimit(senderFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION + " " + Messages.FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED, RED)));

        if(this.factionLogic.getFactionByPlayerUUID(invitedPlayer.uniqueId()).isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PLAYER_IS_ALREADY_IN_A_FACTION, RED)));

        this.invitationManager.sendInvitation(senderPlayer, invitedPlayer, senderFaction);
        return CommandResult.success();
    }

    private boolean hasReachedPlayerLimit(Faction faction)
    {
        return this.factionsConfig.isPlayerLimit() && faction.getPlayers().size() >= this.factionsConfig.getPlayerLimit();
    }
}
