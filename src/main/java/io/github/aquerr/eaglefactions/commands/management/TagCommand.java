package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
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

public class TagCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final AlphaNumericFactionNameTagValidator alphaNumericFactionNameTagValidator = AlphaNumericFactionNameTagValidator.getInstance();

    public TagCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String newFactionTag = context.requireOne(Parameter.string().key("tag").build());

        alphaNumericFactionNameTagValidator.validateTag(newFactionTag);

        final Player player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        //Check if player is leader
        if (!faction.getLeader().equals(player.uniqueId()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS, RED)));

        //Check if faction with such tag already exists
        if(super.getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(newFactionTag)))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN, RED)));

        //Check tag length
        if(newFactionTag.length() > this.factionsConfig.getMaxTagLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxTagLength() + " " + Messages.CHARS + ")", RED)));
        else if(newFactionTag.length() < this.factionsConfig.getMinTagLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinTagLength() + " " + Messages.CHARS + ")", RED)));

        //Change tag function
        super.getPlugin().getFactionLogic().changeTag(faction, newFactionTag);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_TAG_HAS_BEEN_SUCCESSFULLY_CHANGED_TO, GREEN, Collections.singletonMap(Placeholders.FACTION_TAG, text(newFactionTag, GOLD)))));
        return CommandResult.success();
    }
}
