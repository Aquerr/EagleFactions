package io.github.aquerr.eaglefactions.commands.relation;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;

public class EnemyCommand extends AbstractCommand
{
    private InvitationManager invitationManager;

    public EnemyCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.invitationManager = plugin.getInvitationManager();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Faction enemyFaction = context.requireOne(Text.of("faction"));
        final Player player = requirePlayerSource(source);
        final Faction playerFaction = requirePlayerFaction(player);
        final ArmisticeRequest armisticeRequest = findArmisticeRequest(enemyFaction, playerFaction);
        if (armisticeRequest != null)
        {
            armisticeRequest.accept();
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_HAVE_ACCEPTED_ARMISTICE_REQUEST_FROM_FACTION, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, enemyFaction.getName())))));
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

    private void sendRequest(final Player player, final Faction playerFaction, final Faction targetFaction)
    {
        this.invitationManager.sendArmisticeOrWarRequest(player, playerFaction, targetFaction);
    }
}
