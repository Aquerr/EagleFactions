package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import io.github.aquerr.eaglefactions.events.EventRunner;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

public class RenameCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final AlphaNumericFactionNameTagValidator alphaNumericFactionNameTagValidator = AlphaNumericFactionNameTagValidator.getInstance();
    private final MessageService messageService;

    public RenameCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String newFactionName = context.requireOne(Parameter.string().key("name").build());

        alphaNumericFactionNameTagValidator.validateFactionName(newFactionName);

        final Player player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        if (newFactionName.equalsIgnoreCase(EagleFactionsPlugin.SAFE_ZONE_NAME) || newFactionName.equalsIgnoreCase(EagleFactionsPlugin.WAR_ZONE_NAME))
            throw messageService.resolveExceptionWithMessage("error.command.create.you-cant-use-this-faction-name");

        if (!faction.getLeader().equals(player.uniqueId()))
            throw messageService.resolveExceptionWithMessage("error.command.perms.leader-required");

        if (super.getPlugin().getFactionLogic().getFactionsNames().contains(newFactionName.toLowerCase()))
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-with-same-name-already-exists");

        if(newFactionName.length() > this.factionsConfig.getMaxNameLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-long", this.factionsConfig.getMaxNameLength());
        if(newFactionName.length() < this.factionsConfig.getMinNameLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-short", this.factionsConfig.getMinNameLength());

        final boolean isCancelled = EventRunner.runFactionRenameEventPre(player, faction, newFactionName);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().renameFaction(faction, newFactionName);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.rename.success", newFactionName));
            EventRunner.runFactionRenameEventPost(player, faction, newFactionName);
        }
        return CommandResult.success();
    }
}
