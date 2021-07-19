package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;
import java.util.Optional;

public class RenameCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public RenameCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String newFactionName = context.requireOne(Parameter.string().key("name").build());

        final ServerPlayer player = requirePlayerSource(context);
        if (newFactionName.equalsIgnoreCase("SafeZone") || newFactionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANT_USE_THIS_FACTION_NAME, NamedTextColor.RED)));

        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        if (!optionalPlayerFaction.get().getLeader().equals(player.uniqueId()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS, NamedTextColor.RED)));

        if (super.getPlugin().getFactionLogic().getFactionsNames().contains(newFactionName.toLowerCase()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS, NamedTextColor.RED)));

        if(newFactionName.length() > this.factionsConfig.getMaxNameLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxNameLength() + " " + Messages.CHARS + ")", NamedTextColor.RED)));
        if(newFactionName.length() < this.factionsConfig.getMinNameLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinNameLength() + " " + Messages.CHARS + ")", NamedTextColor.RED)));

        final boolean isCancelled = EventRunner.runFactionRenameEventPre(player, optionalPlayerFaction.get(), newFactionName);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().renameFaction(optionalPlayerFaction.get(), newFactionName);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.SUCCESSFULLY_RENAMED_FACTION_TO_FACTION_NAME, NamedTextColor.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Component.text(newFactionName, NamedTextColor.GOLD)))));
            EventRunner.runFactionRenameEventPost(player, optionalPlayerFaction.get(), newFactionName);
        }
        return CommandResult.success();
    }
}
