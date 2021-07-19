package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class UnclaimAllCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public UnclaimAllCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

        //Check if player is in the faction.
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        final Faction playerFaction = optionalPlayerFaction.get();
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));

        final boolean isCancelled = EventRunner.runFactionUnclaimEventPre(player, playerFaction, player.world(), null);
        if (isCancelled)
            return CommandResult.success();

        if(!this.factionsConfig.canPlaceHomeOutsideFactionClaim() && playerFaction.getHome() != null)
        {
            super.getPlugin().getFactionLogic().setHome(playerFaction, null);
        }

        final Faction faction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId()).get();
        super.getPlugin().getFactionLogic().removeAllClaims(faction);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.SUCCESSFULLY_REMOVED_ALL_CLAIMS, NamedTextColor.GREEN)));
        EventRunner.runFactionUnclaimEventPost(player, playerFaction, player.world(), null);
        return CommandResult.success();
    }

}
