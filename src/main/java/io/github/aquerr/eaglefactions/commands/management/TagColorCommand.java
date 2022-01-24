package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

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
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.TAG_COLORING_IS_TURNED_OFF_ON_THIS_SERVER, RED)));

        final ServerPlayer player = requirePlayerSource(context);
        final NamedTextColor color = NamedTextColor.ofExact(context.requireOne(Parameter.color().key("color").build()).rgb());
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, RED)));

        Faction playerFaction = optionalPlayerFaction.get();
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId())
                && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));

        super.getPlugin().getFactionLogic().changeTagColor(playerFaction, color);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.TAG_COLOR_HAS_BEEN_CHANGED, GREEN)));
        return CommandResult.success();
    }
}
