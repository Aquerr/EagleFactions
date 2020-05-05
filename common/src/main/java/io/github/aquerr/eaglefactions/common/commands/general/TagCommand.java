package io.github.aquerr.eaglefactions.common.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Optional;

public class TagCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public TagCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final String newFactionTag = context.requireOne("tag");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        //Check if player is leader
        if (!optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));

        //Check if faction with such tag already exists
        if(super.getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(newFactionTag)))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN));

        //Check tag length
        if(newFactionTag.length() > this.factionsConfig.getMaxTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxTagLength() + " " + Messages.CHARS + ")"));
        else if(newFactionTag.length() < this.factionsConfig.getMinTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinTagLength() + " " + Messages.CHARS + ")"));

        //Change tag function
        super.getPlugin().getFactionLogic().changeTag(optionalPlayerFaction.get(), newFactionTag);
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.FACTION_TAG_HAS_BEEN_SUCCESSFULLY_CHANGED_TO, Collections.singletonMap(Placeholders.FACTION_TAG, Text.of(TextColors.GOLD, newFactionTag)))));
        return CommandResult.success();
    }
}
