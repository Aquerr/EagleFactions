package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListCommand extends AbstractCommand implements CommandExecutor
{
    public ListCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Set<Faction> factionsList = new HashSet<Faction>(getPlugin().getFactionLogic().getFactions().values());
        List<Text> helpList = new ArrayList<>();

        Text tagPrefix = getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix();
        Text tagSufix = getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix();

        for(Faction faction : factionsList)
        {
            Text tag = Text.builder().append(tagPrefix).append(faction.getTag()).append(tagSufix, Text.of(" ")).build();

            Text factionHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, "- ")).append(tag).append(Text.of(faction.getName(), " (", getPlugin().getPowerManager().getFactionPower(faction), "/", getPlugin().getPowerManager().getFactionMaxPower(faction), ")"))
                            .build())
                    .onClick(TextActions.runCommand("/f info " + faction.getName()))
                    .onHover(TextActions.showText(Text.of(TextStyles.ITALIC, TextColors.BLUE, "Click", TextColors.RESET, " for more info...")))
                    .build();

            helpList.add(factionHelp);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, PluginMessages.FACTIONS_LIST)).padding(Text.of("-")).contents(helpList);
        paginationBuilder.sendTo(source);

        return CommandResult.success();
    }
}
