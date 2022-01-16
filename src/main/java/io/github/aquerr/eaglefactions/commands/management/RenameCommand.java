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
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Optional;

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
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final String newFactionName = context.requireOne("name");

        alphaNumericFactionNameTagValidator.validateFactionName(newFactionName);

        final Player player = requirePlayerSource(source);
        final Faction faction = requirePlayerFaction(player);

        if (newFactionName.equalsIgnoreCase("SafeZone") || newFactionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_USE_THIS_FACTION_NAME));

        if (!faction.getLeader().equals(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));

        if (super.getPlugin().getFactionLogic().getFactionsNames().contains(newFactionName.toLowerCase()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS));

        if(newFactionName.length() > this.factionsConfig.getMaxNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxNameLength() + " " + Messages.CHARS + ")"));
        if(newFactionName.length() < this.factionsConfig.getMinNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinNameLength() + " " + Messages.CHARS + ")"));

        final boolean isCancelled = EventRunner.runFactionRenameEventPre(player, faction, newFactionName);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().renameFaction(faction, newFactionName);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.SUCCESSFULLY_RENAMED_FACTION_TO_FACTION_NAME, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, newFactionName)))));
            EventRunner.runFactionRenameEventPost(player, faction, newFactionName);
        }
        return CommandResult.success();
    }
}
