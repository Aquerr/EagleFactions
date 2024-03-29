package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class EnemyCommand extends AbstractCommand
{
    private final InvitationManager invitationManager;
    private final MessageService messageService;

    public EnemyCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.invitationManager = plugin.getInvitationManager();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Faction enemyFaction = context.requireOne(EagleFactionsCommandParameters.optionalFaction());
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        final ArmisticeRequest armisticeRequest = findArmisticeRequest(enemyFaction, playerFaction);
        if (armisticeRequest != null)
        {
            try
            {
                armisticeRequest.accept();
            }
            catch (Exception exception)
            {
                throw new CommandException(Component.text(exception.getMessage()));
            }
            player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-have-accepted-armistice-request-from-faction", enemyFaction.getName()));
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
                .filter(armisticeRequest -> armisticeRequest.getInvited().getName().equals(invitedFaction.getName()) && armisticeRequest.getSender().getName().equals(senderFaction.getName()))
                .findFirst()
                .orElse(null);
    }

    private void sendRequest(final ServerPlayer player, final Faction playerFaction, final Faction targetFaction)
    {
        this.invitationManager.sendArmisticeOrWarRequest(player, playerFaction, targetFaction);
    }
}
