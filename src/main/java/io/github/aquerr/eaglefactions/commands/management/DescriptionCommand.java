package io.github.aquerr.eaglefactions.commands.management;

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
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class DescriptionCommand extends AbstractCommand
{
    public DescriptionCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final String description = context.requireOne(Parameter.string().key("description").build());
        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        //Check if player is leader
        if (!optionalPlayerFaction.get().getLeader().equals(player.uniqueId()) && !optionalPlayerFaction.get().getOfficers().contains(player.uniqueId()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));

        //Check description length
        if(description.length() > 255)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.DESCRIPTION_IS_TOO_LONG + " (" + Messages.MAX + " " + 255 + " " + Messages.CHARS + ")", NamedTextColor.RED)));

        super.getPlugin().getFactionLogic().setDescription(optionalPlayerFaction.get(), description);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.FACTION_DESCRIPTION_HAS_BEEN_UPDATED, NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
