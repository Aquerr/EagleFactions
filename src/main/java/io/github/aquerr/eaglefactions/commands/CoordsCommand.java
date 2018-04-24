package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
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
import java.util.Optional;
import java.util.UUID;

public class CoordsCommand implements CommandExecutor
{
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            Faction playerFaction = FactionLogic.getFaction(FactionLogic.getFactionName(player.getUniqueId()));

            List<Text> teamCoords = new ArrayList<>();

            if(playerFaction != null)
            {
                if(FactionLogic.getHome(playerFaction) != null)
                {
                    Text textBuilder = Text.builder()
                            .append(Text.of("Faction's Home: " + FactionLogic.getHome(playerFaction).toString()))
                            .build();

                    teamCoords.add(textBuilder);
                }

                if (!playerFaction.Leader.equals(""))
                {
                    Optional<Player> leader = PlayerManager.getPlayer(UUID.fromString(playerFaction.Leader));

                    if(leader.isPresent())
                    {
                        Text textBuilder = Text.builder()
                                .append(Text.of("Leader: " + leader.get().getName() + " " + leader.get().getLocation().getBlockPosition().toString()))
                                .build();

                        teamCoords.add(textBuilder);
                    }
                }

                if(!playerFaction.Officers.isEmpty())
                {
                    for (String officerName: playerFaction.Officers)
                    {
                        Optional<Player> officer = PlayerManager.getPlayer(UUID.fromString(officerName));

                        if(officer.isPresent())
                        {
                            Text textBuilder = Text.builder()
                                    .append(Text.of("Officer: " + officer.get().getName() + " " + officer.get().getLocation().getBlockPosition().toString()))
                                    .build();

                            teamCoords.add(textBuilder);
                        }
                    }
                }

                if(!playerFaction.Members.isEmpty())
                {
                    for (String memberName: playerFaction.Members)
                    {
                        Optional<Player> member = PlayerManager.getPlayer(UUID.fromString(memberName));

                        if(member.isPresent())
                        {
                            Text textBuilder = Text.builder()
                                    .append(Text.of("Member: " + member.get().getName() + " " + member.get().getLocation().getBlockPosition().toString()))
                                    .build();

                            teamCoords.add(textBuilder);
                        }
                    }
                }

                PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Team Coords")).contents(teamCoords);
                paginationBuilder.sendTo(source);

            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to use this command!"));
            }

        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
