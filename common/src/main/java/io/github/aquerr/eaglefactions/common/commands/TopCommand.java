package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

public class TopCommand extends AbstractCommand
{
    public TopCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final List<Faction> factionsList = new ArrayList<>(getPlugin().getFactionLogic().getFactions().values());
        final List<Text> helpList = new ArrayList<>();
        int index = 0;
        final Text tagPrefix = getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix();
        final Text tagSufix = getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix();

        factionsList.sort((o1, o2) -> {
            if (super.getPlugin().getPowerManager().getFactionPower(o2) > super.getPlugin().getPowerManager().getFactionPower(o1))
            {
                return 1;
            }
            else
            {
                return -1;
            }
        });

        //This should show only top 10 factions on the server.

        for(final Faction faction : factionsList)
        {
            if(faction.getName().equalsIgnoreCase("safezone") || faction.getName().equalsIgnoreCase("warzone")) continue;
            if(index == 11) break;

            index++;

            Text tag = Text.builder().append(tagPrefix).append(faction.getTag()).append(tagSufix, Text.of(" ")).build();

            Text factionHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, "- ")).append(tag).append(Text.of(faction.getName(), " (", getPlugin().getPowerManager().getFactionPower(faction), "/", getPlugin().getPowerManager().getFactionMaxPower(faction), ")"))
                            .build())
                    .build();

            helpList.add(factionHelp);
        }

        final PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        final PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, PluginMessages.FACTIONS_LIST)).padding(Text.of("-")).contents(helpList);
        paginationBuilder.sendTo(source);

        return CommandResult.success();
    }
}