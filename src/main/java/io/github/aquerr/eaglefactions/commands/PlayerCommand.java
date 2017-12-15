package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalPlayer = context.<Player>getOne("player");

        //TODO: This command should work even for players that are offline.
        //TODO: Add check if provided player has played on this server.
        //player.hasPlayedBefore() is not a solution for this problem.

        if(optionalPlayer.isPresent())
        {
            Player player = optionalPlayer.get();
            showPlayerInfo(player);
        }
        else
        {
            if(source instanceof Player)
            {
                Player player = (Player)source;
                showPlayerInfo(player);
            }
        }
        return CommandResult.success();
    }

    private void showPlayerInfo(Player player)
    {
        if(player.hasPlayedBefore())
        {
            List<Text> playerInfo = new ArrayList<Text>();

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());
            if(playerFactionName == null) playerFactionName = "";

            Date lastPlayed = Date.from(player.getJoinData().lastPlayed().get());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String formattedDate = formatter.format(lastPlayed);

            //TODO: Show if player is online or offline.

            Text info = Text.builder()
                    .append(Text.of(TextColors.AQUA, "Name: ", TextColors.GOLD, PlayerService.getPlayerName(player.getUniqueId()).get() + "\n"))
                    .append(Text.of(TextColors.AQUA, "Last Played: ", TextColors.GOLD, formattedDate + "\n"))
                    .append(Text.of(TextColors.AQUA, "Faction: ", TextColors.GOLD, playerFactionName + "\n"))
                    .append(Text.of(TextColors.AQUA, "Power: ", TextColors.GOLD, PowerService.getPlayerPower(player.getUniqueId()) + "/" + PowerService.getPlayerMaxPower(player.getUniqueId())))
                    .build();

            playerInfo.add(info);

            PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
            PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Player Info")).padding(Text.of("=")).contents(playerInfo);
            paginationBuilder.sendTo(player);
        }
        else
        {
            player.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "This player has not played on this server!"));
        }
    }
}
