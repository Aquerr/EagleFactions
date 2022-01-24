package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collections;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class RenameCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final AlphaNumericFactionNameTagValidator alphaNumericFactionNameTagValidator = AlphaNumericFactionNameTagValidator.getInstance();

    public RenameCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String newFactionName = context.requireOne(Parameter.string().key("name").build());

        alphaNumericFactionNameTagValidator.validateFactionName(newFactionName);

        final Player player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        if (newFactionName.equalsIgnoreCase("SafeZone") || newFactionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_USE_THIS_FACTION_NAME, RED)));

        if (!faction.getLeader().equals(player.uniqueId()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS, RED)));

        if (super.getPlugin().getFactionLogic().getFactionsNames().contains(newFactionName.toLowerCase()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS, RED)));

        if(newFactionName.length() > this.factionsConfig.getMaxNameLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxNameLength() + " " + Messages.CHARS + ")", RED)));
        if(newFactionName.length() < this.factionsConfig.getMinNameLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinNameLength() + " " + Messages.CHARS + ")", RED)));

        final boolean isCancelled = EventRunner.runFactionRenameEventPre(player, faction, newFactionName);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().renameFaction(faction, newFactionName);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.SUCCESSFULLY_RENAMED_FACTION_TO_FACTION_NAME, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(newFactionName, GOLD)))));
            EventRunner.runFactionRenameEventPost(player, faction, newFactionName);
        }
        return CommandResult.success();
    }
}
