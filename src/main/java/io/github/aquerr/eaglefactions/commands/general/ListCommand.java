package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListCommand extends AbstractCommand
{
    private final MessageService messageService;

    public ListCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
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
                        .hoverEvent(messageService.resolveComponentWithMessage("command.list.faction-list.click-for-more-info"));

                helpList.add(factionHelp);
            }

            PaginationList.Builder paginationBuilder = PaginationList.builder()
                    .title(messageService.resolveComponentWithMessage("command.list.faction-list.header"))
                    .padding(messageService.resolveComponentWithMessage("command.list.faction-list.padding-character"))
                    .contents(helpList.toArray(new Component[0]));
            paginationBuilder.sendTo(context.cause().audience());
        });
        return CommandResult.success();
    }
}
