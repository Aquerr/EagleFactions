package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CoordsCommand
{
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            String factionName = FactionLogic.getFactionName(player.getUniqueId());

            List<Text> teamCoords = new ArrayList<>();

            if(factionName != null)
            {
                if(FactionLogic.getHome(factionName) != null)
                {
                    Text textBuilder = Text.builder()
                            .append(Text.of("Home: " + FactionLogic.getHome(factionName).toString()))
                            .build();

                    teamCoords.add(textBuilder);
                }

                if (!FactionLogic.getLeader(factionName).equals(""))
                {
                    Optional<Player> leader = PlayerService.getPlayer(UUID.fromString(FactionLogic.getLeader(factionName)));

                    if(leader.isPresent())
                    {
                        Text textBuilder = Text.builder()
                                .append(Text.of("Leader: " + leader.get().getName() + " " + leader.get().getLocation().getBlockPosition().toString()))
                                .build();

                        teamCoords.add(textBuilder);
                    }
                }

                if(!FactionLogic.getOfficers(factionName).isEmpty())
                {
                    for (String officerName: FactionLogic.getOfficers(factionName))
                    {
                        Optional<Player> officer = PlayerService.getPlayer(UUID.fromString(officerName));

                        if(officer.isPresent())
                        {
                            Text textBuilder = Text.builder()
                                    .append(Text.of("Officer: " + officer.get().getName() + " " + officer.get().getLocation().getBlockPosition().toString()))
                                    .build();

                            teamCoords.add(textBuilder);
                        }
                    }
                }

                if(!FactionLogic.getMembers(factionName).isEmpty())
                {
                    for (String memberName: FactionLogic.getMembers(factionName))
                    {
                        Optional<Player> member = PlayerService.getPlayer(UUID.fromString(memberName));

                        if(member.isPresent())
                        {
                            Text textBuilder = Text.builder()
                                    .append(Text.of("Officer: " + member.get().getName() + " " + member.get().getLocation().getBlockPosition().toString()))
                                    .build();

                            teamCoords.add(textBuilder);
                        }
                    }
                }

                PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Faction Coords")).contents(teamCoords);
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
