package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class MotdCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final MessageService messageService;

    public MotdCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String motd = context.requireOne(Parameter.string().key("motd").build());
        final ServerPlayer player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        //Check if player is leader
        if (!permsManager.hasPermission(player.uniqueId(), faction, FactionPermission.MANAGE_MOTD))
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }

        //Check motd length
        if(motd.length() > 255)
        {
            throw messageService.resolveExceptionWithMessage("error.command.motd.too-long", 255);
        }

        super.getPlugin().getFactionLogic().setMessageOfTheDay(faction, motd);
        player.sendMessage(messageService.resolveMessageWithPrefix("command.motd.success"));
        return CommandResult.success();
    }
}
