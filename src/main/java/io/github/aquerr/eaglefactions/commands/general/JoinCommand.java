package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class JoinCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final InvitationManager invitationManager;

    public JoinCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.invitationManager = plugin.getInvitationManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());

        final ServerPlayer player = requirePlayerSource(context);
        if (super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId()).isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_ARE_ALREADY_IN_A_FACTION, RED)));

        //If player has admin mode then force join.
        if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
        {
            invitationManager.joinAndNotify(player, faction);
            return CommandResult.success();
        }

        //TODO: Should public factions bypass this restriction?
        if(hasReachedPlayerLimit(faction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_JOIN_THIS_FACTION_BECAUSE_IT_REACHED_ITS_PLAYER_LIMIT, RED)));

        if(!faction.isPublic())
        {
            FactionInvite factionInvite = EagleFactionsPlugin.INVITE_LIST.stream()
                    .filter(invite -> invite.getInvitedPlayerUniqueId().equals(player.uniqueId()) && invite.getSenderFaction().equals(faction.getName()))
                    .findFirst()
                    .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_HAVENT_BEEN_INVITED_TO_THIS_FACTION, RED))));

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
