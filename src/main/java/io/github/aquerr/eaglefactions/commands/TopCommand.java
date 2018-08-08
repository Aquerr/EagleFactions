package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
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

import java.util.*;

public class TopCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        List<Faction> factionsList = new ArrayList<>(FactionLogic.getFactions().values());
        List<Text> helpList = new ArrayList<>();
        int index = 0;

        factionsList.sort((o1, o2) -> PowerManager.getFactionPower(o2).compareTo(PowerManager.getFactionPower(o1)));

        //This should show only top 10 factions on the server.

        for(Faction faction : factionsList)
        {
            if(faction.getName().equalsIgnoreCase("safezone") || faction.getName().equalsIgnoreCase("warzone")) continue;
            if(index == 11) break;

            index++;
            String tag = "";
            if(faction.getTag() != null && !faction.getTag().equals("")) tag = "[" + faction.getTag() + "] ";

            Text factionHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, index + ". " + tag + faction.getName() + " (" + PowerManager.getFactionPower(faction) + "/" + PowerManager.getFactionMaxPower(faction) + ")"))
                            .build())
                    .build();

            helpList.add(factionHelp);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, PluginMessages.FACTIONS_LIST)).padding(Text.of("-")).contents(helpList);
        paginationBuilder.sendTo(source);

        return CommandResult.success();
    }
}