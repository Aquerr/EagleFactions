package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.messaging.chat.ChatMessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class InfoCommand extends AbstractCommand
{
    private final FactionLogic factionLogic;
    private final MessageService messageService;

    public InfoCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> faction = context.one(EagleFactionsCommandParameters.optionalFaction());
        if (faction.isPresent())
        {
            if (isPlayerFaction(faction.get(), context))
            {
                selfInfo(context, faction.get());
            }
            else
            {
                otherInfo(context, faction.get());
            }
        }
        else
        {
            final ServerPlayer player = requirePlayerSource(context);
            final Faction playerFaction = requirePlayerFaction(player);
            selfInfo(context, playerFaction);
        }
        return CommandResult.success();
    }

    private boolean isPlayerFaction(Faction faction, CommandContext context) throws CommandException
    {
        if (!isServerPlayer(context.cause().audience()))
            return false;
        Faction playerFaction = this.factionLogic.getFactionByPlayerUUID(requirePlayerSource(context).uniqueId())
                .orElse(null);
        return playerFaction != null && playerFaction.getName().equalsIgnoreCase(faction.getName());
    }


    private void selfInfo(final CommandContext context, final Faction faction) throws CommandException
    {
        if (!context.hasPermission(PluginPermissions.INFO_COMMAND) && !context.hasPermission(PluginPermissions.INFO_COMMAND_SELF))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        showFactionInfo(context, faction);
    }

    private void otherInfo(CommandContext context, Faction faction) throws CommandException
    {
        if (!context.hasPermission(PluginPermissions.INFO_COMMAND) && !context.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        showFactionInfo(context, faction);
    }

    private void showFactionInfo(final CommandContext source, final Faction faction)
    {
        final List<Component> factionInfo = new ArrayList<>();

        Component leaderNameText = empty();
        FactionPlayer factionLeader = faction.getLeader()
                .map(FactionMember::getUniqueId)
                .flatMap(super.getPlugin().getPlayerManager()::getFactionPlayer)
                .orElse(null);
        if (factionLeader != null)
        {
            leaderNameText = buildClickablePlayerNickname(faction, factionLeader);
        }

        Component membersList = empty();
        if(!faction.getMembers().isEmpty())
        {
        	membersList = buildPlayerList(faction);
        }

        Component info = text()
                .append(messageService.resolveComponentWithMessage("command.info.name", faction.getName())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.tag", faction.getTag())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.last-online", lastOnline(faction))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.description", faction.getDescription())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.motd", faction.getMessageOfTheDay())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.public", faction.isPublic())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.leader", leaderNameText.color(GOLD))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.members", membersList)).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.power", super.getPlugin().getPowerManager().getFactionPower(faction) + "/" + super.getPlugin().getPowerManager().getFactionMaxPower(faction))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.claims", faction.getClaims().size() + "/" + this.factionLogic.getFactionMaxClaims(faction)))
                .build();

        factionInfo.add(info);

        PaginationService paginationService = Sponge.serviceProvider().paginationService();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(messageService.resolveComponentWithMessage("command.info.faction-info-header"))
                .contents(factionInfo);
        paginationBuilder.sendTo(source.cause().audience());
    }

    private Component lastOnline(final Faction faction)
    {
        if(this.factionLogic.hasOnlinePlayers(faction))
            return messageService.resolveComponentWithMessage("command.info.now");

        final Date date = Date.from(faction.getLastOnline());
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String formattedDate = formatter.format(date);
        return text(formattedDate, RED);
    }

    private Component buildPlayerList(Faction faction)
    {
        return Component.join(JoinConfiguration.separator(text(",")), faction.getMembers().stream()
                .map(recruit -> getPlugin().getPlayerManager().getFactionPlayer(recruit.getUniqueId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(factionPlayer -> buildClickablePlayerNickname(faction, factionPlayer))
                .collect(Collectors.toList()));
    }

    private TextComponent buildClickablePlayerNickname(Faction faction, FactionPlayer factionPlayer)
    {
        TextComponent.Builder textComponentBuilder = Component.text();

        TextComponent rankPrefix = ChatMessageHelper.getRankPrefix(faction, factionPlayer.getUniqueId());
        if (rankPrefix != null)
            textComponentBuilder.append(rankPrefix);

        return textComponentBuilder.append(text(factionPlayer.getName()))
                .hoverEvent(HoverEvent.showText(messageService.resolveComponentWithMessage("command.info.click-to-view-information-about-player")))
                .clickEvent(ClickEvent.runCommand("/f player " + factionPlayer.getName()))
                .build();
    }
}
