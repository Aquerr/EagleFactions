package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Color;

import java.util.Optional;

public class TagColorCommand extends AbstractCommand
{
    public TagColorCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        if (!getPlugin().getConfiguration().getChatConfig().canColorTags())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.TAG_COLORING_IS_TURNED_OFF_ON_THIS_SERVER, NamedTextColor.RED)));

        final Color color = context.requireOne(Parameter.color().key("color").build());
        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        Faction playerFaction = optionalPlayerFaction.get();
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));

        super.getPlugin().getFactionLogic().changeTagColor(playerFaction, TextColor.color(color));
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.TAG_COLOR_HAS_BEEN_CHANGED, NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
