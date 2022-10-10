package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

public class SetFlagCommand extends AbstractCommand
{
    private final FactionLogic factionLogic;
    private final MessageService messageService;

    public SetFlagCommand(EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());
        ProtectionFlagType flagType = context.requireOne(Parameter.enumValue(ProtectionFlagType.class).key("flag").build());
        boolean value = context.requireOne(Parameter.bool().key("value").build());
        factionLogic.setFactionProtectionFlag(faction, flagType, value);
        context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix("command.flags.flag-has-been-set"));
        return CommandResult.success();
    }
}
