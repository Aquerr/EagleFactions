package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerCommand extends AbstractCommand
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public PlayerCommand(final EagleFactions plugin)
    {
        super(plugin);
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
            final Player player = context.cause().first(Player.class).orElse(null);
            if (player != null)
            {
                final Optional<FactionPlayer> optionalFactionPlayer = super.getPlugin().getPlayerManager().getFactionPlayer(player.uniqueId());
                if (!optionalFactionPlayer.isPresent())
                    return CommandResult.success();
                final FactionPlayer factionPlayer = optionalFactionPlayer.get();

                showPlayerInfo(player, factionPlayer);
            }
        }
        return CommandResult.success();
    }

    private void showPlayerInfo(Audience audience, FactionPlayer factionPlayer)
    {
        List<TextComponent> playerInfo = new LinkedList<>();
        String playerFactionName = "";
        if (factionPlayer.getFactionName().isPresent())
            playerFactionName = factionPlayer.getFactionName().get();

        LocalDateTime lastPlayed = factionPlayer.getUser()
                .flatMap(User::player)
                .map(ServerPlayer::lastPlayed)
                .map(instant -> LocalDateTime.ofInstant(instant.get(), ZoneId.systemDefault()))
                .orElse(LocalDateTime.now());

        String formattedDate = DATE_TIME_FORMATTER.format(lastPlayed);
        final boolean isOnline = factionPlayer.getUser()
                .map(User::isOnline)
                .orElse(false);
        final TextComponent online = isOnline ? Component.text("ONLINE", NamedTextColor.GREEN) : Component.text("OFFLINE", NamedTextColor.RED);
        TextComponent info = Component.empty()
                .append(Component.text(Messages.NAME + ": ", NamedTextColor.AQUA).append(Component.text(factionPlayer.getName(), NamedTextColor.GOLD).append(Component.newline()))
                .append(Component.text(Messages.PLAYER_STATUS + ": ", NamedTextColor.AQUA).append(online)).append(Component.newline()))
                .append(Component.text(Messages.FACTION + ": ", NamedTextColor.AQUA).append(Component.text(playerFactionName, NamedTextColor.GOLD)).append(Component.newline()))
                .append(Component.text(Messages.POWER + ": ", NamedTextColor.AQUA).append(Component.text(factionPlayer.getPower() + "/" + factionPlayer.getMaxPower(), NamedTextColor.GOLD))).append(Component.newline())
                .append(Component.text(Messages.LAST_PLAYED + ": ", NamedTextColor.AQUA).append(Component.text(formattedDate, NamedTextColor.GOLD)));

        playerInfo.add(info);

        PaginationList.Builder paginationBuilder = PaginationList.builder()
                .title(Component.text(Messages.PLAYER_INFO, NamedTextColor.GREEN))
                .padding(Component.text("="))
                .contents(playerInfo.toArray(new Component[0]));
        paginationBuilder.sendTo(audience);
    }
}
