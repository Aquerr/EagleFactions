package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
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
import static net.kyori.adventure.text.format.NamedTextColor.*;

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
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND)));
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
        final Component online = isOnline ? text("ONLINE", GREEN) : text("OFFLINE", RED);
        Component info = text()
                .append(text(Messages.NAME + ": ", AQUA)).append(text(factionPlayer.getName(), GOLD)).append(newline())
                .append(text(Messages.PLAYER_STATUS + ": ", AQUA)).append(online).append(newline())
                .append(text(Messages.FACTION + ": ", AQUA)).append(text(playerFactionName, GOLD)).append(newline())
                .append(text(Messages.POWER + ": ", AQUA)).append(text(factionPlayer.getPower() + "/" + factionPlayer.getMaxPower(), GOLD)).append(newline())
                .append(text(Messages.LAST_PLAYED + ": ", AQUA)).append(text(formattedDate, GOLD))
                .build();

        playerInfo.add(info);

        PaginationList.Builder paginationBuilder = PaginationList.builder()
                .title(text(Messages.PLAYER_INFO, GREEN))
                .padding(text("="))
                .contents(playerInfo);
        paginationBuilder.sendTo(audience);
    }
}
