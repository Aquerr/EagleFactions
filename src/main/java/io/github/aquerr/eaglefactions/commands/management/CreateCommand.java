package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.creation.FactionCreationManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public class CreateCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final AlphaNumericFactionNameTagValidator alphaNumericFactionNameTagValidator = AlphaNumericFactionNameTagValidator.getInstance();
    private final MessageService messageService;
    private final FactionCreationManager factionCreationManager;

    public CreateCommand(EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
        this.factionCreationManager = plugin.getFactionCreationManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        if (isServerPlayer(context.cause().audience()))
        {
            validateNotInFaction((Player) context.cause().audience());
        }

        final String factionName = context.requireOne(Parameter.string().key("name").build());
        final String factionTag = context.requireOne(Parameter.string().key("tag").build());

        alphaNumericFactionNameTagValidator.validate(factionName, factionTag);

        if (getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(factionTag)))
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-already-taken");

        if (factionName.equalsIgnoreCase(EagleFactionsPlugin.SAFE_ZONE_NAME) || factionName.equalsIgnoreCase(EagleFactionsPlugin.WAR_ZONE_NAME))
            throw messageService.resolveExceptionWithMessage("error.command.create.you-cant-use-this-faction-name");

        //Check tag length
        if (factionTag.length() > this.factionsConfig.getMaxTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-long", this.factionsConfig.getMaxTagLength());
        else if (factionTag.length() < this.factionsConfig.getMinTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-short", this.factionsConfig.getMinTagLength());

        if (getPlugin().getFactionLogic().getFactionsNames().contains(factionName.toLowerCase()))
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-with-same-name-already-exists");

        //Check name length
        if (factionName.length() > this.factionsConfig.getMaxNameLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-long", this.factionsConfig.getMaxNameLength());
        else if (factionName.length() < this.factionsConfig.getMinNameLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-short", this.factionsConfig.getMinNameLength());

        try
        {
            this.factionCreationManager.createFaction(context.cause().audience(), factionName, factionTag);
        }
        catch (Exception e)
        {
            throw messageService.resolveExceptionWithMessage("error.command.create.could-not-create-faction-with-reason", e.getMessage());
        }

        return CommandResult.success();
    }

    private void validateNotInFaction(Player player) throws CommandException
    {
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (optionalPlayerFaction.isPresent())
            throw messageService.resolveExceptionWithMessage("error.command.join.you-are-already-in-a-faction");
    }
}
