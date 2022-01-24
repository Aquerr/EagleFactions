package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

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
        final Faction faction = requirePlayerFaction(player);

        //Check if player is leader
        if (!faction.getLeader().equals(player.uniqueId()) && !faction.getOfficers().contains(player.uniqueId()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));

        //Check description length
        if(description.length() > 255)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.DESCRIPTION_IS_TOO_LONG + " (" + Messages.MAX + " " + 255 + " " + Messages.CHARS + ")", RED)));

        super.getPlugin().getFactionLogic().setDescription(faction, description);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.FACTION_DESCRIPTION_HAS_BEEN_UPDATED, GREEN)));
        return CommandResult.success();
    }
}
