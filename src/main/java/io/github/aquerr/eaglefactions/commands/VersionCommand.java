package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.net.MalformedURLException;
import java.net.URL;

public class VersionCommand extends AbstractCommand
{
    private final MessageService messageService;

    public VersionCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        try
        {
            context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix(
                    "command.version.version",
                    PluginInfo.NAME,
                    PluginInfo.VERSION,
                    PluginInfo.AUTHOR)
                    .hoverEvent(HoverEvent.showText(messageService.resolveComponentWithMessage("command.version.github")))
                    .clickEvent(ClickEvent.openUrl(new URL(PluginInfo.URL))));
        }
        catch(final MalformedURLException e)
        {
            e.printStackTrace();
        }
        return CommandResult.success();
    }
}
