package io.github.aquerr.eaglefactions.commands.general;

import com.google.common.collect.Lists;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class HelpCommand extends AbstractCommand
{
    private final MessageService messageService;

    public HelpCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final int pageNumber = context.one(Parameter.integerNumber().key("page").build()).orElse(1);
        final Map<List<String>, Command.Parameterized> commands = EagleFactionsPlugin.SUBCOMMANDS;
        final List<TextComponent> helpList = Lists.newArrayList();

        for (final Map.Entry<List<String>, Command.Parameterized> command : commands.entrySet())
        {
            if(context.cause().audience() instanceof Player && !command.getValue().canExecute(context.cause()))
                continue;

            final TextComponent commandHelp = Component.empty()
                    .append(Component.empty()
                            .append(Component.text("/f " + String.join(", ", command.getKey()), AQUA)))
                    .append(Component.empty()
                            .append(Component.text(" - ").append(command.getValue().shortDescription(CommandCause.create()).get().append(Component.newline())).color(WHITE)))
                    .append(Component.empty()
                            .append(Component.text(messageService.resolveMessage("command.info.usage", " /f " + String.join(", ", command.getKey()) + " " + getParameters(command.getValue())))));
            helpList.add(commandHelp);
        }

        //Sort commands alphabetically.
        helpList.sort(Comparator.comparing(o -> PlainTextComponentSerializer.plainText().serialize(o)));

        PaginationList.Builder paginationBuilder = Sponge.serviceProvider().paginationService().builder();
        paginationBuilder.title(messageService.resolveComponentWithMessage("command.help.header"))
            .padding(messageService.resolveComponentWithMessage("command.help.padding-character"))
            .contents(helpList.toArray(new Component[0]))
            .linesPerPage(10);
        paginationBuilder.build().sendTo(context.cause().audience(), pageNumber);
        return CommandResult.success();
    }

    private String getParameters(Command.Parameterized command)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (final Parameter parameter : command.parameters())
        {
            if (parameter instanceof Parameter.Value)
            {
                if (parameter.isOptional())
                {
                    stringBuilder.append("[");
                }
                stringBuilder.append("<")
                        .append(((Parameter.Value<?>) parameter).key().key())
                        .append(">");
                if (parameter.isOptional())
                {
                    stringBuilder.append("]");
                }
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

}