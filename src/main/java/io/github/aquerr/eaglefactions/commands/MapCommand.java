package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class MapCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;
            World world = player.getWorld();

            Text notCapturedMark = Text.of(TextColors.GRAY, "/");
            Text factionMark = Text.of(TextColors.GREEN, "+");
            Text allianceMark = Text.of(TextColors.AQUA, "+");
            Text enemyMark = Text.of(TextColors.RED, "#");
            Text normalFactionMark = Text.of(TextColors.WHITE, "+");
            Text playerLocationMark = Text.of(TextColors.GOLD, "+");

            //World world = player.getWorld();
            Vector3i playerPosition = player.getLocation().getChunkPosition();

            List<Text> map = new ArrayList<>();
            String normalFactions = "";
            String allianceFactions = "";
            String enemyFactions = "";
            String playerFaction = "";

            //Map resolution
            int mapWidth = 20;
            int mapHeight = 8;

            //Half map resolution + 1 (for player column/row in the center)
            //Needs to be an odd number so the map will have equal distance to the left and right.
            int halfMapWidth = mapWidth / 2;
            int halfMapHeight = mapHeight / 2;

            for (int row = -halfMapHeight; row <= halfMapHeight; row++)
            {
                Text.Builder textBuilder = Text.builder();

                for (int column = -halfMapWidth; column <= halfMapWidth; column++)
                {
                    if(row == 0 && column == 0)
                    {
                        //TODO: Faction that player is standing at is not showed in the list.
                        textBuilder.append(playerLocationMark);
                        continue;
                    }

                    Vector3i chunk = playerPosition.add(column, 0, row);

                    if (FactionLogic.isClaimed(world.getUniqueId(), chunk))
                    {
                        String factionName = FactionLogic.getFactionNameByChunk(world.getUniqueId(), chunk);
                        String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());
                        if (playerFactionName != null)
                        {
                            if (factionName.equals(playerFactionName))
                            {
                                textBuilder.append(factionMark);
                                playerFaction = factionName;
                            }
                            else if (FactionLogic.getAlliances(playerFactionName).contains(factionName))
                            {
                                textBuilder.append(allianceMark);
                                if (!allianceFactions.contains(factionName))
                                {
                                    allianceFactions += factionName + ", ";
                                }
                            }
                            else if (FactionLogic.getEnemies(playerFactionName).contains(factionName))
                            {
                                textBuilder.append(enemyMark);
                                if (!enemyFactions.contains(factionName))
                                {
                                    enemyFactions += factionName + ", ";
                                }
                            }
                            else
                            {
                                if (factionName.equals("SafeZone"))
                                {
                                    textBuilder.append(Text.of(TextColors.AQUA, "+"));
                                }
                                else if (factionName.equals("WarZone"))
                                {
                                    textBuilder.append(Text.of(TextColors.DARK_RED, "#"));
                                }
                                else
                                {
                                    textBuilder.append(normalFactionMark);
                                }
                                if (!normalFactions.contains(factionName))
                                {
                                    normalFactions += factionName + ", ";
                                }
                            }
                        }
                        else
                        {
                            if (factionName.equals("SafeZone"))
                            {
                                textBuilder.append(Text.of(TextColors.AQUA, "+"));
                            }
                            else if (factionName.equals("WarZone"))
                            {
                                textBuilder.append(Text.of(TextColors.DARK_RED, "#"));
                            }
                            else
                            {
                                textBuilder.append(normalFactionMark);
                            }
                            if (!normalFactions.contains(factionName))
                            {
                                normalFactions += factionName + ", ";
                            }
                        }
                    }
                    else
                    {
                        textBuilder.append(notCapturedMark);
                    }
                }
                map.add(textBuilder.build());
            }

            String playerPositionCalim = "none";

            if(FactionLogic.isClaimed(world.getUniqueId(), playerPosition))
            {
                playerPositionCalim = FactionLogic.getFactionNameByChunk(world.getUniqueId(), playerPosition);
            }

            //Print map
            player.sendMessage(Text.of(TextColors.GREEN, "=====Factions Map====="));
            for (Text text: map)
            {
                player.sendMessage(Text.of(text));
            }
            player.sendMessage(Text.of(TextColors.GREEN, "====================="));

            //PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
            //PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Factions Map")).contents(map);
            //paginationBuilder.sendTo(source);

            //Print factions on map
            if(!playerFaction.equals(""))
            {
                player.sendMessage(Text.of(TextColors.GREEN, "Your faction: ", TextColors.GREEN, playerFaction));
            }
            if(!normalFactions.isEmpty())
            {
                player.sendMessage(Text.of(TextColors.WHITE, "Factions: ", TextColors.RESET, normalFactions.substring(0, normalFactions.length() - 2)));
            }
            if(!allianceFactions.isEmpty())
            {
                player.sendMessage(Text.of(TextColors.AQUA, "Alliances: " + allianceFactions.substring(0, allianceFactions.length() - 2)));
            }
            if(!enemyFactions.isEmpty())
            {
                player.sendMessage(Text.of(TextColors.RED, "Enemies: " + enemyFactions.substring(0, enemyFactions.length() - 2)));
            }

            player.sendMessage(Text.of("Currently standing at: ", TextColors.GOLD, playerPosition.toString(), TextColors.WHITE, " which is claimed by ", TextColors.GOLD, playerPositionCalim));
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
