package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class TopCommand extends AbstractCommand
{
    public TopCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        CompletableFuture.runAsync(() -> {
            final List<Faction> factionsList = new ArrayList<>(getPlugin().getFactionLogic().getFactions().values());
            final List<Component> helpList = new LinkedList<>();
            final Component tagPrefix = getPlugin().getConfiguration().getChatConfig().getFactionStartPrefix();
            final Component tagSuffix = getPlugin().getConfiguration().getChatConfig().getFactionEndPrefix();

            factionsList.sort((o1, o2) -> {
                final float firstFactionPower = super.getPlugin().getPowerManager().getFactionPower(o1);
                final float secondFactionPower = super.getPlugin().getPowerManager().getFactionPower(o2);
                return Float.compare(secondFactionPower, firstFactionPower);
            });

            //This should show only top 10 factions on the server.
            int factionCount = 0;
            for(final Faction faction : factionsList)
            {
                if(faction.isSafeZone() || faction.isWarZone()) continue;
                if(factionCount == 11) break;

                final Component tag = tagPrefix.append(faction.getTag())
                        .append(tagSuffix.append(Component.space()));
                final Component factionHelp = text("- ", AQUA)
                        .append(tag)
                        .append(text(faction.getName() + " (" + super.getPlugin().getPowerManager().getFactionPower(faction) + "/" + getPlugin().getPowerManager().getFactionMaxPower(faction) + ")"));
                helpList.add(factionHelp);
                factionCount++;
            }

            final PaginationList.Builder paginationBuilder = PaginationList.builder()
                    .title(text(Messages.FACTIONS_LIST, GREEN))
                    .padding(text("-"))
                    .contents(helpList);
            paginationBuilder.sendTo(context.cause().audience());
        });
        return CommandResult.success();
    }
}