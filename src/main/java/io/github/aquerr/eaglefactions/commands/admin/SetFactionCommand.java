package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
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
        Faction faction = context.requireOne(EagleFactionsCommandParameters.optionalFaction());
        Rank rank = context.one(EagleFactionsCommandParameters.factionRank()).orElse(null);

        super.getPlugin().getFactionLogic().setFaction(player.uniqueId(), faction.getName(), rank.getName());
        context.cause().audience().sendMessage(messageService.resolveMessageWithPrefix("command.set-faction.player-faction-changed"));
        return CommandResult.success();
    }
}
