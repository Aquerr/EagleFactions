package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;

public class DisbandCommand extends AbstractCommand
{
    public DisbandCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.getOne("faction");

        if (source instanceof Player)
        {
            final Player player = (Player) source;
            Faction faction = optionalFaction.orElseGet(() -> super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                    .orElse(null));
            if (faction == null)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            playerDisband(player, faction);
        }
        else
        {
            final Faction faction = context.requireOne("faction");
            consoleDisband(source, faction);
        }

        return CommandResult.success();
    }

    private void consoleDisband(CommandSource source, Faction faction) throws CommandException
    {
        //Even admins should not be able to disband SafeZone nor WarZone
        if(faction.isSafeZone() || faction.isWarZone())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_FACTION_CANNOT_BE_DISBANDED));
        sendDisbandEventAndDisband(source, faction, false);
    }

    private void playerDisband(final Player player, final Faction faction) throws CommandException
    {
        //Even admins should not be able to disband SafeZone nor WarZone
        if(faction.isSafeZone() || faction.isWarZone())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_FACTION_CANNOT_BE_DISBANDED));

        if (player.getUniqueId().equals(faction.getLeader()))
        {
            sendDisbandEventAndDisband(player, faction, false);
        }
        else
        {
            final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player);
            if (!hasAdminMode)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));
            sendDisbandEventAndDisband(player, faction, true);
        }
    }

    private void sendDisbandEventAndDisband(final CommandSource source, final Faction faction, final boolean forceRemovedByAdmin)
    {
        final boolean isCancelled = EventRunner.runFactionDisbandEventPre(source, faction, forceRemovedByAdmin, false);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().disbandFaction(faction.getName());
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HAS_BEEN_DISBANDED));
            clearAutoClaimAndChatForFactionMembers(faction);
            EventRunner.runFactionDisbandEventPost(source, faction, forceRemovedByAdmin, false);
        }
    }

    private void clearAutoClaimAndChatForFactionMembers(Faction faction)
    {
        for (final UUID memberUUID : faction.getMembers())
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(memberUUID);
            EagleFactionsPlugin.CHAT_LIST.remove(memberUUID);
        }
    }
}
