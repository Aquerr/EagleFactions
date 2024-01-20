package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.TextComponent;
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
    private final PermsManager permsManager;

    public TagCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String newFactionTag = context.requireOne(Parameter.string().key("tag").build());

        alphaNumericFactionNameTagValidator.validateTag(newFactionTag);

        final Player player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        //Check if player is leader
        if (!permsManager.hasPermission(player.uniqueId(), faction, FactionPermission.MANAGE_TAG_NAME))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        //Check if faction with such tag already exists
        if(super.getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(newFactionTag)))
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-already-taken");

        //Check tag length
        if(newFactionTag.length() > this.factionsConfig.getMaxTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-long", this.factionsConfig.getMaxTagLength());
        else if(newFactionTag.length() < this.factionsConfig.getMinTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-short", this.factionsConfig.getMinTagLength());

        //Change tag function

        final TextComponent oldTag = faction.getTag();
        final boolean isCancelled = EventRunner.runFactionTagUpdateEventPre(player, faction, oldTag, newFactionTag);
        if (!isCancelled)
        {
            super.getPlugin().getFactionLogic().changeTag(faction, newFactionTag);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.tag.success"));
            EventRunner.runFactionTagUpdateEventPost(player, faction, oldTag, newFactionTag);
        }
        return CommandResult.success();
    }
}
