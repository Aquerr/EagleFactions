package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
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
public class PlayerCommand extends AbstractCommand
{
    public PlayerCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<Player> optionalPlayer = context.<Player>getOne("player");

        //TODO: This command should work even for players that are offline.
        //TODO: Add check if provided player has played on this server.
        //player.hasPlayedBefore() is not a solution for this problem.

        if(optionalPlayer.isPresent())
        {
            final Player player = optionalPlayer.get();
            showPlayerInfo(source, player);
        }
        else
        {
            if(source instanceof Player)
            {
                final Player player = (Player)source;
                showPlayerInfo(source, player);
            }
        }
        return CommandResult.success();
    }

    private void showPlayerInfo(CommandSource source, Player player)
    {
        //TODO: hasPlayedBefore prevents new players from using /f player
//        if(player.hasPlayedBefore())
//        {
            List<Text> playerInfo = new ArrayList<Text>();

            String playerFactionName = "";
            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            if(optionalPlayerFaction.isPresent()) playerFactionName = optionalPlayerFaction.get().getName();

            Date lastPlayed = Date.from(player.getJoinData().lastPlayed().get());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String formattedDate = formatter.format(lastPlayed);

            //TODO: Show if player is online or offline.

            Text info = Text.builder()
                    .append(Text.of(TextColors.AQUA, Messages.NAME + ": ", TextColors.GOLD, getPlugin().getPlayerManager().getPlayerName(player.getUniqueId()).get() + "\n"))
                    .append(Text.of(TextColors.AQUA, Messages.LAST_PLAYED + ": ", TextColors.GOLD, formattedDate + "\n"))
                    .append(Text.of(TextColors.AQUA, Messages.FACTION + ": ", TextColors.GOLD, playerFactionName + "\n"))
                    .append(Text.of(TextColors.AQUA, Messages.POWER + ": ", TextColors.GOLD, getPlugin().getPowerManager().getPlayerPower(player.getUniqueId()) + "/" + getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId())))
                    .build();

            playerInfo.add(info);

            PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
            PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, Messages.PLAYER_INFO)).padding(Text.of("=")).contents(playerInfo);
            paginationBuilder.sendTo(source);
//        }
//        else
//        {
//            player.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLAYER_HAS_NOT_PLAYED_ON_THIS_SERVER));
//        }
    }
}
