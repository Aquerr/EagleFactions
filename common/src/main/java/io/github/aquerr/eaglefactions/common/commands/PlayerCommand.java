package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerCommand extends AbstractCommand
{
    public PlayerCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<FactionPlayer> optionalPlayer = context.getOne("player");

        //TODO: This command should work even for players that are offline.
        //TODO: Add check if provided player has played on this server.
        //player.hasPlayedBefore() is not a solution for this problem.

        if (optionalPlayer.isPresent())
        {
            final FactionPlayer player = optionalPlayer.get();
            showPlayerInfo(source, player);
        }
        else
        {
            if (source instanceof Player)
            {
                final Player player = (Player) source;
                final Optional<FactionPlayer> optionalFactionPlayer = super.getPlugin().getPlayerManager().getFactionPlayer(player.getUniqueId());
                if (!optionalFactionPlayer.isPresent())
                    return CommandResult.success();
                final FactionPlayer factionPlayer = optionalFactionPlayer.get();

                showPlayerInfo(source, factionPlayer);
            }
        }
        return CommandResult.success();
    }

    private void showPlayerInfo(CommandSource source, FactionPlayer factionPlayer)
    {
        List<Text> playerInfo = new ArrayList<>();
        String playerFactionName = "";
        if (factionPlayer.getFactionName().isPresent())
            playerFactionName = factionPlayer.getFactionName().get();

        LocalDateTime lastPlayed = LocalDateTime.now();

        final Optional<User> optionalUser = factionPlayer.getUser();
        if (optionalUser.isPresent())
        {
            final Optional<JoinData> joinData = optionalUser.get().get(JoinData.class);
            if (joinData.isPresent())
            {
                final JoinData data = joinData.get();
                final Instant instant = data.lastPlayed().get();
                lastPlayed = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = dateTimeFormatter.format(lastPlayed);

        Text info = Text.builder()
                .append(Text.of(TextColors.AQUA, Messages.NAME + ": ", TextColors.GOLD, factionPlayer.getName() + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.LAST_PLAYED + ": ", TextColors.GOLD, formattedDate + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.FACTION + ": ", TextColors.GOLD, playerFactionName + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.POWER + ": ", TextColors.GOLD, factionPlayer.getPower() + "/" + factionPlayer.getMaxPower()))
                .build();

        playerInfo.add(info);

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, Messages.PLAYER_INFO)).padding(Text.of("=")).contents(playerInfo);
        paginationBuilder.sendTo(source);
    }
}
