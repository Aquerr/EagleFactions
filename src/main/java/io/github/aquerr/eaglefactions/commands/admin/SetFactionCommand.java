package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

public class SetFactionCommand extends AbstractCommand
{
    private final MessageService messageService;

    public SetFactionCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = getPlugin().getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Player player = context.requireOne(CommonParameters.PLAYER);
        Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());
        FactionMemberType factionMemberType = context.requireOne(Parameter.enumValue(FactionMemberType.class).key("rank").build());

        if (factionMemberType == FactionMemberType.ALLY || factionMemberType == FactionMemberType.NONE || factionMemberType == FactionMemberType.TRUCE)
            throw messageService.resolveExceptionWithMessage("error.command.set-faction.rank-not-valid");

        super.getPlugin().getFactionLogic().setFaction(player.uniqueId(), faction.getName(), factionMemberType);
        context.cause().audience().sendMessage(messageService.resolveMessageWithPrefix("command.set-faction.player-faction-changed"));
        return CommandResult.success();
    }
}
