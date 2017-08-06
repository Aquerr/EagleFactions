package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Player player = context.<Player>getOne(Text.of("player")).get();

        //TODO: Add check if provided player has entry in server database (if player played on the server).
        //player.hasPlayedBefore() is not a solution for this problem.

        if(player.hasPlayedBefore())
        {

            List<Text> playerInfo = new ArrayList<Text>();

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());
            if(playerFactionName == null) playerFactionName = "";

            Text info = Text.builder()
                    .append(Text.of(TextColors.AQUA, "Name: ", TextColors.GOLD, PlayerService.getPlayerName(player.getUniqueId()).get() + "\n"))
                    .append(Text.of(TextColors.AQUA, "Last Played: ", TextColors.GOLD, player.getJoinData().lastPlayed().get() + "\n"))
                    .append(Text.of(TextColors.AQUA, "Faction: ", TextColors.GOLD, playerFactionName))
                   // .append(Text.of(TextColors.AQUA, "Power: ", TextColors.GOLD, PowerService.getPlayerPower() + "\n"))
                    .build();

            playerInfo.add(info);

            PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
            PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Player Info")).padding(Text.of("=")).contents(playerInfo);
            paginationBuilder.sendTo(source);

            CommandResult.success();

        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "This player has not played on this server!"));
        }

        return CommandResult.success();
    }
}
