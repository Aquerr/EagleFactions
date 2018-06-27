package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
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

import java.util.ArrayList;
import java.util.List;

public class ListCommand implements CommandExecutor
{
    @Inject
    private FactionsCache cache;

    @Inject
    private Settings settings;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        List<Faction> factionsList = cache.getFactions();
        List<Text> helpList = new ArrayList<>();

        Text tagPrefix = settings.getFactionPrefixStart();
        Text tagSufix = settings.getFactionPrefixEnd();

        for (Faction faction : factionsList)
        {
            Text tag = Text.builder().append(tagPrefix).append(faction.Tag).append(tagSufix, Text.of(" ")).build();

            Text factionHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, "- ")).append(tag).append(Text.of(faction.Name, " (", PowerManager.getFactionPower(faction), "/", PowerManager.getFactionMaxPower(faction), ")"))
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
