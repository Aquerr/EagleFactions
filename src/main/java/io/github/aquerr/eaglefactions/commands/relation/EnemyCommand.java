package io.github.aquerr.eaglefactions.commands.relation;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import javax.annotation.Nullable;

import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class EnemyCommand extends AbstractCommand
{
    private InvitationManager invitationManager;

    public EnemyCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.invitationManager = plugin.getInvitationManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Faction enemyFaction = context.requireOne(EagleFactionsCommandParameters.faction());
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        final ArmisticeRequest armisticeRequest = findArmisticeRequest(enemyFaction, playerFaction);
        if (armisticeRequest != null)
        {
            armisticeRequest.accept();
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_HAVE_ACCEPTED_ARMISTICE_REQUEST_FROM_FACTION, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Component.text(enemyFaction.getName(), GOLD)))));
        }
        else
        {
            sendRequest(player, playerFaction, enemyFaction);
        }

        return CommandResult.success();
    }

    @Nullable
    private ArmisticeRequest findArmisticeRequest(Faction senderFaction, Faction invitedFaction)
    {
        return EagleFactionsPlugin.RELATION_INVITES.stream()
                .filter(ArmisticeRequest.class::isInstance)
                .map(ArmisticeRequest.class::cast)
                .filter(armisticeRequest -> armisticeRequest.getInvitedFaction().equals(invitedFaction.getName()) && armisticeRequest.getSenderFaction().equals(senderFaction.getName()))
                .findFirst()
                .orElse(null);
    }

    private void sendRequest(final ServerPlayer player, final Faction playerFaction, final Faction targetFaction)
    {
        this.invitationManager.sendArmisticeOrWarRequest(player, playerFaction, targetFaction);
    }
}
