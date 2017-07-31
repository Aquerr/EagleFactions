package io.github.aquerr.eaglefactions.commands;


import com.google.common.collect.Lists;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import javafx.scene.control.Pagination;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        source.sendMessage (Text.of (TextColors.AQUA, "[Eagle Factions] ", TextColors.WHITE, "- Version ", PluginInfo.Version));

        Map<List<String>, CommandSpec> commands = EagleFactions.getEagleFactions()._subcommands;
        List<Text> helpList = Lists.newArrayList();

        for (List<String> aliases: commands.keySet())
        {
            CommandSpec commandSpec = commands.get(aliases);

            Text commandHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, aliases.toString(), "\n"))
                            .build())
                    .append(Text.builder()
                            .append(Text.of(TextColors.GRAY, commandSpec.getShortDescription(source).get(),Text.of("\n")))
                            .build())
                    .build();

            helpList.add(commandHelp);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "EagleFaction Help List")).padding(Text.of("-")).contents(helpList);
        paginationBuilder.sendTo(source);

        return CommandResult.success ();
    }

}