package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerCommand extends AbstractCommand
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final MessageService messageService;

    public PlayerCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<FactionPlayer> optionalPlayer = context.one(EagleFactionsCommandParameters.factionPlayer());
        if (optionalPlayer.isPresent())
        {
            final FactionPlayer player = optionalPlayer.get();
            showPlayerInfo(context.cause().audience(), player);
        }
        else
        {
            if (context.cause().audience() instanceof Player)
            {
                final Player player = (Player) context.cause().audience();
                final Optional<FactionPlayer> optionalFactionPlayer = super.getPlugin().getPlayerManager().getFactionPlayer(player.uniqueId());
                if (!optionalFactionPlayer.isPresent())
                    return CommandResult.success();
                final FactionPlayer factionPlayer = optionalFactionPlayer.get();

                showPlayerInfo(player, factionPlayer);
            }
            else
            {
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);
            }
        }
        return CommandResult.success();
    }

    private void showPlayerInfo(Audience audience, FactionPlayer factionPlayer)
    {
        List<Component> playerInfo = new ArrayList<>();
        String playerFactionName = "";
        if (factionPlayer.getFactionName().isPresent())
            playerFactionName = factionPlayer.getFactionName().get();

        LocalDateTime lastPlayed = LocalDateTime.now();

        final Optional<User> optionalUser = factionPlayer.getUser();
        if (optionalUser.isPresent())
        {
            final Optional<Instant> optionalLastPlayedInstant = optionalUser.get().get(Keys.LAST_DATE_PLAYED);
            if (optionalLastPlayedInstant.isPresent())
            {
                lastPlayed = LocalDateTime.ofInstant(optionalLastPlayedInstant.get(), ZoneId.systemDefault());
            }
        }

        String formattedDate = DATE_TIME_FORMATTER.format(lastPlayed);
        final boolean isOnline = optionalUser.isPresent() && optionalUser.get().isOnline();
        final Component online = isOnline ? messageService.resolveComponentWithMessage("command.player.online")
                : messageService.resolveComponentWithMessage("command.player.offline");
        Component info = text()
                .append(messageService.resolveComponentWithMessage("command.player.name", factionPlayer.getName())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.player.player-status", online)).append(newline())
                .append(messageService.resolveComponentWithMessage("command.player.faction", playerFactionName)).append(newline())
                .append(messageService.resolveComponentWithMessage("command.player.power", factionPlayer.getPower() + "/" + factionPlayer.getMaxPower())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.player.last-played", formattedDate))
                .build();

        playerInfo.add(info);

        PaginationList.Builder paginationBuilder = PaginationList.builder()
                .title(messageService.resolveComponentWithMessage("command.player.header"))
                .padding(messageService.resolveComponentWithMessage("command.player.padding-character"))
                .contents(playerInfo);
        paginationBuilder.sendTo(audience);
    }
}
