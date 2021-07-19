package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class DisbandCommand extends AbstractCommand
{
    public DisbandCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());

        if (isPlayer(context))
        {
            final ServerPlayer player = (ServerPlayer) context.cause().audience();
            final Faction faction = optionalFaction.orElse(super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId())
                    .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, RED)))));

            //Even admins should not be able to disband SafeZone nor WarZone
            checkIfSafeZoneOrWarZone(faction);

            if (player.uniqueId().equals(faction.getLeader()))
            {
                playerDisband(player, faction, false);
            }
            else
            {
                final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player);
                if (!hasAdminMode)
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS, RED)));
                playerDisband(player, faction, true);
            }
        }
        else
        {
            final Faction selectedFaction = optionalFaction.orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text("Select correct faction!"))));
            checkIfSafeZoneOrWarZone(selectedFaction);
            consoleDisband(context, selectedFaction);
        }

        return CommandResult.success();
    }

    private void playerDisband(final Player player, final Faction faction, final boolean forceRemovedByAdmin)
    {
        final boolean isCancelled = EventRunner.runFactionDisbandEventPre(player, faction, forceRemovedByAdmin, false);
        if(isCancelled)
            return;

        super.getPlugin().getFactionLogic().disbandFaction(faction.getName());
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.FACTION_HAS_BEEN_DISBANDED, GREEN)));
        EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
        EagleFactionsPlugin.CHAT_LIST.remove(player.uniqueId());
        EventRunner.runFactionDisbandEventPost(player, faction, forceRemovedByAdmin, false);
    }

    private void consoleDisband(CommandContext context, final Faction faction)
    {
        final boolean isCancelled = EventRunner.runFactionDisbandEventPre(null, faction, true, false);
        if (isCancelled)
            return;

        super.getPlugin().getFactionLogic().disbandFaction(faction.getName());
        context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(text(Messages.FACTION_HAS_BEEN_DISBANDED, GREEN)));
        EventRunner.runFactionDisbandEventPost(null, faction, true, false);
    }

    private void checkIfSafeZoneOrWarZone(Faction faction) throws CommandException
    {
        if(faction.isSafeZone() || faction.isWarZone())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_FACTION_CANNOT_BE_DISBANDED, RED)));
    }
}
