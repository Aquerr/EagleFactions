package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TopCommand extends AbstractCommand
{
    public TopCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        CompletableFuture.runAsync(() -> {
            final List<Faction> factionsList = new ArrayList<>(getPlugin().getFactionLogic().getFactions().values());
            final List<Text> helpList = new ArrayList<>();
            int index = 0;
            final Text tagPrefix = getPlugin().getConfiguration().getChatConfig().getFactionStartPrefix();
            final Text tagSuffix = getPlugin().getConfiguration().getChatConfig().getFactionEndPrefix();

            factionsList.sort((o1, o2) -> {
                final float firstFactionPower = super.getPlugin().getPowerManager().getFactionPower(o1);
                final float secondFactionPower = super.getPlugin().getPowerManager().getFactionPower(o2);
                return Float.compare(secondFactionPower, firstFactionPower);
            });

            //This should show only top 10 factions on the server.

            for(final Faction faction : factionsList)
            {
                if(faction.isSafeZone() || faction.isWarZone()) continue;
                if(index == 11) break;

                index++;

                final Text tag = Text.builder().append(tagPrefix).append(faction.getTag()).append(tagSuffix, Text.of(" ")).build();

                final Text factionHelp = Text.builder()
                        .append(Text.builder()
                                .append(Text.of(TextColors.AQUA, "- ")).append(tag).append(Text.of(faction.getName(), " (", getPlugin().getPowerManager().getFactionPower(faction), "/", getPlugin().getPowerManager().getFactionMaxPower(faction), ")"))
                                .build())
                        .build();

                helpList.add(factionHelp);
            }

            final PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
            final PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, Messages.FACTIONS_LIST)).padding(Text.of("-")).contents(helpList);
            paginationBuilder.sendTo(source);
        });
        return CommandResult.success();
    }
}