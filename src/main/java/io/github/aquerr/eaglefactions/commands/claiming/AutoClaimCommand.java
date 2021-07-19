package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class AutoClaimCommand extends AbstractCommand
{
    public AutoClaimCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        final Faction playerFaction = optionalPlayerFaction.get();
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));

        if(EagleFactionsPlugin.AUTO_CLAIM_LIST.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.AUTO_CLAIM_HAS_BEEN_TURNED_OFF, NamedTextColor.GREEN)));
        }
        else
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.add(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.AUTO_CLAIM_HAS_BEEN_TURNED_ON, NamedTextColor.GREEN)));
        }

        return CommandResult.success();
    }
}
