package io.github.aquerr.eaglefactions.commands;

import com.google.common.collect.Lists;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
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
        Map<List<String>, CommandSpec> commands = EagleFactions.Subcommands;
        List<Text> helpList = Lists.newArrayList();

        for (List<String> aliases: commands.keySet())
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

            Text commandHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, "/f " + aliases.toString().replace("[","").replace("]","")))
                            .build())
                    .append(Text.builder()
                            .append(Text.of(TextColors.WHITE, " - " + commandSpec.getShortDescription(source).get().toPlain() + "\n"))
                            .build())
                    .append(Text.builder()
                            .append(Text.of(TextColors.GRAY, PluginMessages.USAGE + " /f " + aliases.toString().replace("[","").replace("]","") + " " + commandSpec.getUsage(source).toPlain()))
                            .build())
                    .build();

                   // .append(Text.builder()
                   //         .append(Text.of(TextColors.GRAY, " - " + commandSpec.getShortDescription(source).get().toPlain()))
                   //         .build())
//
                  //  .build();

            helpList.add(commandHelp);
        }

        //Sort commands alphabetically.
        helpList.sort(Text::compareTo);

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, PluginMessages.EAGLEFACTIONS_COMMAND_LIST)).padding(Text.of("-")).contents(helpList).linesPerPage(14);
        paginationBuilder.sendTo(source);

        return CommandResult.success();
    }

}