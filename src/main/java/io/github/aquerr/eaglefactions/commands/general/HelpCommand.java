package io.github.aquerr.eaglefactions.commands.general;

import com.google.common.collect.Lists;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;

public class HelpCommand extends AbstractCommand
{
    public HelpCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final int pageNumber = context.<Integer>getOne(Text.of("page")).orElse(1);
        final Map<List<String>, CommandSpec> commands = EagleFactionsPlugin.SUBCOMMANDS;
        final List<Text> helpList = Lists.newArrayList();

        for (final List<String> aliases: commands.keySet())
        {
            CommandSpec commandSpec = commands.get(aliases);

            if(source instanceof Player)
            {
                Player player = (Player)source;

                if(!commandSpec.testPermission(player))
                {
                    continue;
                }
            }

            final Text commandHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, "/f " + aliases.toString().replace("[","").replace("]","")))
                            .build())
                    .append(Text.builder()
                            .append(Text.of(TextColors.WHITE, " - " + commandSpec.getShortDescription(source).get().toPlain() + "\n"))
                            .build())
                    .append(Text.builder()
                            .append(Text.of(TextColors.GRAY, Messages.USAGE + " /f " + aliases.toString().replace("[","").replace("]","") + " " + commandSpec.getUsage(source).toPlain()))
                            .build())
                    .build();
            helpList.add(commandHelp);
        }

        //Sort commands alphabetically.
        helpList.sort(Text::compareTo);

        PaginationList.Builder paginationBuilder = PaginationList.builder().title(Text.of(TextColors.GREEN, Messages.EAGLEFACTIONS_COMMAND_LIST)).padding(Text.of("-")).contents(helpList).linesPerPage(14);
        paginationBuilder.build().sendTo(source, pageNumber);
        return CommandResult.success();
    }

}