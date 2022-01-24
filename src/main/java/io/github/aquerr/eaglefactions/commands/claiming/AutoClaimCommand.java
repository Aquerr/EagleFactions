package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

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
        final Faction faction = requirePlayerFaction(player);

        if (!faction.getLeader().equals(player.uniqueId()) && !faction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));

        if (EagleFactionsPlugin.AUTO_CLAIM_LIST.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.AUTO_CLAIM_HAS_BEEN_TURNED_OFF, GREEN)));
        }
        else
        {
            EagleFactionsPlugin.AUTO_CLAIM_LIST.add(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.AUTO_CLAIM_HAS_BEEN_TURNED_ON, GREEN)));
        }

        return CommandResult.success();
    }
}
