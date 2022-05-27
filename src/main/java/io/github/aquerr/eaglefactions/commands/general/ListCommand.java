package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListCommand extends AbstractCommand
{
    public ListCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        CompletableFuture.runAsync(() ->{
            Collection<Faction> factionsList = super.getPlugin().getFactionLogic().getFactions().values();
            List<TextComponent> helpList = new LinkedList<>();

            TextComponent tagPrefix = getPlugin().getConfiguration().getChatConfig().getFactionStartPrefix();
            TextComponent tagSuffix = getPlugin().getConfiguration().getChatConfig().getFactionEndPrefix();

            for(final Faction faction : factionsList)
            {
                TextComponent tag = Component.empty()
                        .append(tagPrefix)
                        .append(faction.getTag())
                        .append(tagSuffix.append(Component.text(" ")));

                TextComponent factionHelp = Component.empty()
                        .append(Component.empty()
                                .append(Component.text("- ", NamedTextColor.AQUA))
                                .append(tag)
                                .append(Component.text(faction.getName() + " (" + getPlugin().getPowerManager().getFactionPower(faction) + "/" + getPlugin().getPowerManager().getFactionMaxPower(faction) + ")")))
                        .clickEvent(ClickEvent.runCommand("/f info " + faction.getName()))
                        .hoverEvent(Component.text("Click", Style.style().decoration(TextDecoration.ITALIC, true).color(NamedTextColor.BLUE).build()).append(Component.text(" for more info...", Style.style().decoration(TextDecoration.ITALIC, true).build())));

                helpList.add(factionHelp);
            }

            PaginationList.Builder paginationBuilder = PaginationList.builder()
                    .title(Component.text(Messages.FACTIONS_LIST, NamedTextColor.GREEN))
                    .padding(Component.text("-"))
                    .contents(helpList.toArray(new Component[0]));
            paginationBuilder.sendTo(context.cause().audience());
        });
        return CommandResult.success();
    }
}
