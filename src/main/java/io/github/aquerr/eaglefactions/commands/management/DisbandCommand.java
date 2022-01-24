package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.UUID;

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
        if (context.cause().audience() instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) context.cause().audience();
            Faction faction = requirePlayerFaction(player);
            playerDisband(player, faction);
        }
        else
        {
            final Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());
            consoleDisband(context, faction);
        }

        return CommandResult.success();
    }

    private void consoleDisband(CommandContext context, Faction faction) throws CommandException
    {
        //Even admins should not be able to disband SafeZone nor WarZone
        if(faction.isSafeZone() || faction.isWarZone())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_FACTION_CANNOT_BE_DISBANDED, RED)));
        sendDisbandEventAndDisband(context.cause().audience(), faction, false);
    }

    private void playerDisband(final ServerPlayer player, final Faction faction) throws CommandException
    {
        //Even admins should not be able to disband SafeZone nor WarZone
        if(faction.isSafeZone() || faction.isWarZone())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_FACTION_CANNOT_BE_DISBANDED, RED)));

        if (player.uniqueId().equals(faction.getLeader()))
        {
            sendDisbandEventAndDisband(player, faction, false);
        }
        else
        {
            final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player.user());
            if (!hasAdminMode)
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS, RED)));
            sendDisbandEventAndDisband(player, faction, true);
        }
    }

    private void sendDisbandEventAndDisband(final Audience audience, final Faction faction, final boolean forceRemovedByAdmin)
    {
        final boolean isCancelled = EventRunner.runFactionDisbandEventPre(audience, faction, forceRemovedByAdmin, false);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().disbandFaction(faction.getName());
            audience.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.FACTION_HAS_BEEN_DISBANDED, GREEN)));
            clearAutoClaimAndChatForFactionMembers(faction);
            EventRunner.runFactionDisbandEventPost(audience, faction, forceRemovedByAdmin, false);
        }
    }

    private void clearAutoClaimAndChatForFactionMembers(Faction faction)
    {
        for (final UUID memberUUID : faction.getPlayers())
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(memberUUID);
            EagleFactionsPlugin.CHAT_LIST.remove(memberUUID);
        }
    }
}
