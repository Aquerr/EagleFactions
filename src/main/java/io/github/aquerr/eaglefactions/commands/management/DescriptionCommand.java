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

public class DescriptionCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final MessageService messageService;

    public DescriptionCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final String description = context.requireOne(Parameter.string().key("description").build());
        final ServerPlayer player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);

        //Check if player is leader
        if (!permsManager.hasPermission(player.uniqueId(), faction, FactionPermission.MANAGE_DESCRIPTION))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        //Check description length
        if(description.length() > 255)
            throw messageService.resolveExceptionWithMessage("error.command.description.too-long", 255);

        super.getPlugin().getFactionLogic().setDescription(faction, description);
        player.sendMessage(messageService.resolveMessageWithPrefix("command.description.success"));
        return CommandResult.success();
    }
}
