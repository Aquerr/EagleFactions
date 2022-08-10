package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

public class TagCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final AlphaNumericFactionNameTagValidator alphaNumericFactionNameTagValidator = AlphaNumericFactionNameTagValidator.getInstance();
    private final MessageService messageService;

    public TagCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
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
            throw messageService.resolveExceptionWithMessage("error.command.perms.leader-required");

        //Check if faction with such tag already exists
        if(super.getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(newFactionTag)))
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-already-taken");

        //Check tag length
        if(newFactionTag.length() > this.factionsConfig.getMaxTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-long", this.factionsConfig.getMaxTagLength());
        else if(newFactionTag.length() < this.factionsConfig.getMinTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-short", this.factionsConfig.getMinTagLength());

        //Change tag function
        super.getPlugin().getFactionLogic().changeTag(faction, newFactionTag);
        player.sendMessage(messageService.resolveMessageWithPrefix("command.tag.success"));
        return CommandResult.success();
    }
}
