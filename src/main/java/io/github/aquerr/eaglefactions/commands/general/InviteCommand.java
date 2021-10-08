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
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Player invitedPlayer = context.requireOne("player");
        final Player senderPlayer = requirePlayerSource(source);
        final Faction senderFaction = requirePlayerFaction(senderPlayer);
        if (!this.permsManager.canInvite(senderPlayer.getUniqueId(), senderFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_INVITE_PLAYERS_TO_FACTION));

        if(hasReachedPlayerLimit(senderFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION + " " + Messages.FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED));

        if(this.factionLogic.getFactionByPlayerUUID(invitedPlayer.getUniqueId()).isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYER_IS_ALREADY_IN_A_FACTION));

        this.invitationManager.sendInvitation(senderPlayer, invitedPlayer, senderFaction);
        return CommandResult.success();
    }

    private boolean hasReachedPlayerLimit(Faction faction)
    {
        return this.factionsConfig.isPlayerLimit() && faction.getPlayers().size() >= this.factionsConfig.getPlayerLimit();
    }
}
