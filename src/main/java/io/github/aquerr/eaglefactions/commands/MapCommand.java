package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import sun.java2d.loops.GeneralRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MapCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;
            generateMap(player);
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }

    private void generateMap(Player player)
    {
            World world = player.getWorld();

            Text notCapturedMark = Text.of(TextColors.GRAY, "/");
            Text factionMark = Text.of(TextColors.GREEN, "+");
            Text allianceMark = Text.of(TextColors.AQUA, "+");
            Text enemyMark = Text.of(TextColors.RED, "#");
            Text normalFactionMark = Text.of(TextColors.WHITE, "+");
            Text playerLocationMark = Text.of(TextColors.GOLD, "+");

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
                                textBuilder.append(factionMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
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
                        if(MainLogic.isDelayedClaimingToggled()) textBuilder.append(notCapturedMark).build();
                        else textBuilder.append(notCapturedMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
                    }
                }
                map.add(textBuilder.build());
            }

            String playerPositionClaim = "none";

            if(FactionLogic.isClaimed(world.getUniqueId(), playerPosition))
            {
                playerPositionClaim = FactionLogic.getFactionNameByChunk(world.getUniqueId(), playerPosition);
            }

            //Print map
            player.sendMessage(Text.of(TextColors.GREEN, "=====Factions Map====="));
            for (Text text: map)
            {
                player.sendMessage(Text.of(text));
            }
            player.sendMessage(Text.of(TextColors.GREEN, "====================="));

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

            player.sendMessage(Text.of("Currently standing at: ", TextColors.GOLD, playerPosition.toString(), TextColors.WHITE, " which is claimed by ", TextColors.GOLD, playerPositionClaim));
        }


    private Consumer<CommandSource> claimByMap(Player player, Vector3i chunk)
    {
        String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());
        World world = player.getWorld();

        return consumer ->
        {

            if (playerFactionName != null)
            {
                if (FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    if (!FactionLogic.isClaimed(world.getUniqueId(), chunk))
                    {
                        if (FactionLogic.getFaction(playerFactionName).Power.doubleValue() > FactionLogic.getClaims(playerFactionName).size())
                        {
                            if(!EagleFactions.AttackedFactions.contains(playerFactionName))
                            {
                                if(!FactionLogic.getClaims(playerFactionName).isEmpty())
                                {
                                    if(playerFactionName.equals("SafeZone") || playerFactionName.equals("WarZone"))
                                    {
                                        FactionLogic.addClaim(playerFactionName, world.getUniqueId(), chunk);
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                                    }
                                    else
                                    {
                                        if(MainLogic.requireConnectedClaims())
                                        {
                                            if(FactionLogic.isClaimConnected(playerFactionName, world.getUniqueId(), chunk))
                                            {
                                                FactionLogic.startClaiming(player, playerFactionName, world.getUniqueId(), chunk);
                                            }
                                            else
                                            {
                                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Claims needs to be connected!"));
                                            }
                                        }
                                        else
                                        {
                                            FactionLogic.startClaiming(player, playerFactionName, world.getUniqueId(), chunk);
                                        }
                                    }
                                }
                                else
                                {
                                    FactionLogic.startClaiming(player, playerFactionName, world.getUniqueId(), chunk);
                                }
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction is under attack! You need to wait ", TextColors.GOLD, "2 minutes", TextColors.RED, " to be able to claim again!"));
                            }
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction does not have power to claim more land!"));
                        }
                    }
                    else
                    {
                        //Check if faction's home was set in this claim. If yes then remove it.
                        if (FactionLogic.getHome(playerFactionName) != null)
                        {
                            Location homeLocation = world.getLocation(FactionLogic.getHome(playerFactionName).BlockPosition);

                            if (homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString()))
                            {
                                FactionLogic.setHome(world.getUniqueId(), playerFactionName, null);
                            }
                        }

                        FactionLogic.removeClaim(playerFactionName, world.getUniqueId(), chunk);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land has been successfully ", TextColors.GOLD, "unclaimed", TextColors.WHITE, "!"));
                    }
                }
                else if (EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    if (!FactionLogic.isClaimed(world.getUniqueId(), chunk))
                    {
                        FactionLogic.addClaim(playerFactionName, world.getUniqueId(), chunk);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                    }
                    else
                    {
                        //Check if faction's home was set in this claim. If yes then remove it.
                        if (FactionLogic.getHome(playerFactionName) != null)
                        {
                            Location homeLocation = world.getLocation(FactionLogic.getHome(playerFactionName).BlockPosition);

                            if (homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString()))
                            {
                                FactionLogic.setHome(world.getUniqueId(), playerFactionName, null);
                            }
                        }

                        FactionLogic.removeClaim(playerFactionName, world.getUniqueId(), chunk);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land has been successfully ", TextColors.GOLD, "unclaimed", TextColors.WHITE, "!"));
                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to claim lands!"));
            }

            generateMap(player);
        };
    }
}
