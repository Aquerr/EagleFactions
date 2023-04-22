package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class FlagsCommand extends AbstractCommand
{
    private final MessageService messageService;

    public FlagsCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = getPlugin().getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Faction faction = context.requireOne(EagleFactionsCommandParameters.optionalFaction());
        List<Component> componentList = new ArrayList<>();
        for (final ProtectionFlag protectionFlag : new TreeSet<>(faction.getProtectionFlags()))
        {
            componentList.add(formatProtectionFlag(faction, protectionFlag));
        }

        PaginationList.builder()
                .title(Component.text("Protection flags", NamedTextColor.GREEN))
                .contents(componentList)
                .build()
            .sendTo(context.cause().audience());
        return CommandResult.success();
    }

    private TextComponent formatProtectionFlag(Faction faction, ProtectionFlag protectionFlag)
    {
        return Component.text()
                .append(Component.text(protectionFlag.getType().getName(), NamedTextColor.AQUA))
                .append(Component.text(": "))
                .append(Component.text(protectionFlag.getValue(), protectionFlag.getValue() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .clickEvent(ClickEvent.runCommand(String.format("/f setflag %s %s %s", faction.getName(), protectionFlag.getType(), !protectionFlag.getValue())))
                .hoverEvent(HoverEvent.showText(messageService.resolveComponentWithMessage("command.flags.set-to", String.valueOf(!protectionFlag.getValue()).toUpperCase())))
                .build();
    }
}
