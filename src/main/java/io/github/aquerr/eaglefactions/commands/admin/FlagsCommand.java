package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.List;

public class FlagsCommand extends AbstractCommand
{
    public FlagsCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());
        List<Component> componentList = new ArrayList<>();
        for (final ProtectionFlag protectionFlag : faction.getProtectionFlags())
        {
            componentList.add(Component.text(protectionFlag.getType() + ": " + protectionFlag.getValue()));
        }

        PaginationList.builder()
                .title(Component.text("Protection flags"))
                .contents(componentList)
                .build()
            .sendTo(context.cause().audience());
        return CommandResult.success();
    }
}
