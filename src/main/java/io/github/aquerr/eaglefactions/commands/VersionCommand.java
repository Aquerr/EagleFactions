package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.net.MalformedURLException;
import java.net.URL;

public class VersionCommand extends AbstractCommand
{
    public VersionCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        try
        {
            context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX
                    .append(Component.text(PluginInfo.NAME, NamedTextColor.AQUA))
                    .append(Component.text(" - ", NamedTextColor.WHITE))
                    .append(Component.text(Messages.VERSION + " " + PluginInfo.VERSION, NamedTextColor.GOLD))
                    .append(Component.text(" made by ", NamedTextColor.WHITE))
                    .append(Component.text(PluginInfo.AUTHOR, NamedTextColor.GOLD))
            .hoverEvent(HoverEvent.showText(Component.text("Click to view Github", NamedTextColor.BLUE)))
            .clickEvent(ClickEvent.openUrl(new URL(PluginInfo.URL))));
        }
        catch(final MalformedURLException e)
        {
            e.printStackTrace();
        }
        return CommandResult.success();
    }
}
