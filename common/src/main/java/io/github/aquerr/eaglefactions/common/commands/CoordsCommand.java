package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
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

public class CoordsCommand extends AbstractCommand
{
    public CoordsCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            final Player player = (Player)source;
            final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            final List<Text> teamCoords = new ArrayList<>();

            if(optionalPlayerFaction.isPresent())
            {
                final Faction playerFaction = optionalPlayerFaction.get();
                if(playerFaction.getHome() != null)
                {
                    Text textBuilder = Text.builder()
                            .append(Text.of( PluginMessages.FACTIONS_HOME + ": " + playerFaction.getHome().getWorldUUID().toString() + '|' + playerFaction.getHome().getBlockPosition().toString()))
                            .build();

                    teamCoords.add(textBuilder);
                }

                if (!playerFaction.getLeader().toString().equals(""))
                {
                    final Optional<Player> leader = getPlugin().getPlayerManager().getPlayer(playerFaction.getLeader());
                    if(leader.isPresent())
                    {
                        Text textBuilder = Text.builder()
                                .append(Text.of(PluginMessages.LEADER + ": " + leader.get().getName() + " " + leader.get().getLocation().getBlockPosition().toString()))
                                .build();

                        teamCoords.add(textBuilder);
                    }
                }

                if(!playerFaction.getOfficers().isEmpty())
                {
                    for (final UUID officerUUID: playerFaction.getOfficers())
                    {
                        final Optional<Player> officer = getPlugin().getPlayerManager().getPlayer(officerUUID);
                        if(officer.isPresent())
                        {
                            Text textBuilder = Text.builder()
                                    .append(Text.of(PluginMessages.OFFICER + ": " + officer.get().getName() + " " + officer.get().getLocation().getBlockPosition().toString()))
                                    .build();

                            teamCoords.add(textBuilder);
                        }
                    }
                }

                if(!playerFaction.getMembers().isEmpty())
                {
                    for (final UUID memberUUID: playerFaction.getMembers())
                    {
                        final Optional<Player> member = getPlugin().getPlayerManager().getPlayer(memberUUID);
                        if(member.isPresent())
                        {
                            Text textBuilder = Text.builder()
                                    .append(Text.of(PluginMessages.MEMBER + ": " + member.get().getName() + " " + member.get().getLocation().getBlockPosition().toString()))
                                    .build();

                            teamCoords.add(textBuilder);
                        }
                    }
                }

                if(!playerFaction.getRecruits().isEmpty())
                {
                    for (final UUID recruitUUID: playerFaction.getRecruits())
                    {
                        final Optional<Player> recruit = getPlugin().getPlayerManager().getPlayer(recruitUUID);
                        if(recruit.isPresent())
                        {
                            Text textBuilder = Text.builder()
                                    .append(Text.of(PluginMessages.RECRUIT + ": " + recruit.get().getName() + " " + recruit.get().getLocation().getBlockPosition().toString()))
                                    .build();

                            teamCoords.add(textBuilder);
                        }
                    }
                }

                final PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                final PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, PluginMessages.TEAM_COORDS)).contents(teamCoords);
                paginationBuilder.sendTo(source);

            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }

        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
