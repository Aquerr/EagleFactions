package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ListCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        HashSet<Object> factionsList = new HashSet<>(FactionLogic.getFactions());
        List<Text> helpList = new ArrayList<>();

        for(Object faction: factionsList)
        {
            Text factionHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, "- " + faction.toString()))
                            .build())
                    .build();

            helpList.add(factionHelp);
            //source.sendMessage(factionHelp);

        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Factions List")).padding(Text.of("-")).contents(helpList);
        paginationBuilder.sendTo(source);

        //source.sendMessage(helpList);

        return CommandResult.success();
    }
}
