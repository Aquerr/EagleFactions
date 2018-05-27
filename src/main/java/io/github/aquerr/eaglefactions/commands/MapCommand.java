package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MapCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player) source;
            if (MainLogic.getClaimableWorldNames().contains(player.getWorld().getName()))
            {
                generateMap(player);
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_VIEW_MAP_IN_THIS_WORLD));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void generateMap(Player player)
    {
        List<String> claimsList = FactionLogic.getAllClaims();
        Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

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
        //String playerFaction = "";

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
                if (row == 0 && column == 0)
                {
                    //TODO: Faction that player is standing at is not showed in the list.
                    textBuilder.append(playerLocationMark);
                    continue;
                }

                Vector3i chunk = playerPosition.add(column, 0, row);

                if (claimsList.contains(world.getUniqueId().toString() + '|' + chunk.toString()))
                {
                    Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), chunk);

                    if (optionalPlayerFaction.isPresent())
                    {
                        Faction playerFaction = optionalPlayerFaction.get();

                        if (optionalChunkFaction.get().Name.equals(playerFaction.Name))
                        {
                            textBuilder.append(factionMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
//                            playerFaction = optionalChunkFaction.get();
                        } else if (playerFaction.Alliances.contains(optionalChunkFaction.get().Name))
                        {
                            textBuilder.append(allianceMark);
                            if (!allianceFactions.contains(optionalChunkFaction.get().Name))
                            {
                                allianceFactions += optionalChunkFaction.get().Name + ", ";
                            }
                        } else if (playerFaction.Enemies.contains(optionalChunkFaction.get().Name))
                        {
                            textBuilder.append(enemyMark);
                            if (!enemyFactions.contains(optionalChunkFaction.get().Name))
                            {
                                enemyFactions += optionalChunkFaction.get().Name + ", ";
                            }
                        } else
                        {
                            if (optionalChunkFaction.get().Name.equals("SafeZone"))
                            {
                                textBuilder.append(Text.of(TextColors.AQUA, "+"));
                            } else if (optionalChunkFaction.get().Name.equals("WarZone"))
                            {
                                textBuilder.append(Text.of(TextColors.DARK_RED, "#"));
                            } else
                            {
                                textBuilder.append(normalFactionMark);
                            }
                            if (!normalFactions.contains(optionalChunkFaction.get().Name))
                            {
                                normalFactions += optionalChunkFaction.get().Name + ", ";
                            }
                        }
                    } else
                    {
                        if (optionalChunkFaction.get().Name.equals("SafeZone"))
                        {
                            textBuilder.append(Text.of(TextColors.AQUA, "+"));
                        } else if (optionalChunkFaction.get().Name.equals("WarZone"))
                        {
                            textBuilder.append(Text.of(TextColors.DARK_RED, "#"));
                        } else
                        {
                            textBuilder.append(normalFactionMark);
                        }
                        if (!normalFactions.contains(optionalChunkFaction.get().Name))
                        {
                            normalFactions += optionalChunkFaction.get().Name + ", ";
                        }
                    }
                }
                else
                {
                    if (!MainLogic.isDelayedClaimingToggled() &&
                            (EagleFactions.AdminList.contains(player.getUniqueId()) ||
                                    (optionalPlayerFaction.isPresent() &&
                                            (optionalPlayerFaction.get().Leader.equals(player.getUniqueId().toString()) || optionalPlayerFaction.get().Officers.contains(player.getUniqueId().toString())))))
                    {
                        textBuilder.append(notCapturedMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
                    } else
                    {
                        textBuilder.append(notCapturedMark).build();
                    }
                }
            }
            map.add(textBuilder.build());
        }

        String playerPositionClaim = "none";

        Optional<Faction> optionalPlayerPositionFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), playerPosition);

        if (optionalPlayerPositionFaction.isPresent())
        {
            playerPositionClaim = optionalPlayerPositionFaction.get().Name;
        }

        //Print map
        player.sendMessage(Text.of(TextColors.GREEN, PluginMessages.FACTIONS_MAP_HEADER));
        for (Text text : map)
        {
            player.sendMessage(Text.of(text));
        }
        player.sendMessage(Text.of(TextColors.GREEN, PluginMessages.FACTIONS_MAP_FOOTER));

        //Print factions on map
        if (optionalPlayerFaction.isPresent())
        {
            player.sendMessage(Text.of(TextColors.GREEN, PluginMessages.YOUR_FACTION + ": ", TextColors.GREEN, optionalPlayerFaction.get().Name));
        }
        if (!normalFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.WHITE, PluginMessages.FACTIONS + ": ", TextColors.RESET, normalFactions.substring(0, normalFactions.length() - 2)));
        }
        if (!allianceFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.AQUA, PluginMessages.ALLIANCES + ": " + allianceFactions.substring(0, allianceFactions.length() - 2)));
        }
        if (!enemyFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.RED, PluginMessages.ENEMIES + ": " + enemyFactions.substring(0, enemyFactions.length() - 2)));
        }

        player.sendMessage(Text.of(PluginMessages.CURRENTLY_STANDING_AT + ": ", TextColors.GOLD, playerPosition.toString(), TextColors.WHITE, " " + PluginMessages.WHICH_IS_CLAIMED_BY + " ", TextColors.GOLD, playerPositionClaim));
    }


    private Consumer<CommandSource> claimByMap(Player player, Vector3i chunk)
    {
        return consumer ->
        {
            //Because faction could have changed we need to get it again here.

            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());
            World world = player.getWorld();

            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if (playerFaction.Leader.equals(player.getUniqueId().toString()) || playerFaction.Officers.contains(player.getUniqueId().toString()))
                {
                    //We need to check if because player can click on the claim that is already claimed (in the previous map in the chat)
                    if (!FactionLogic.isClaimed(world.getUniqueId(), chunk))
                    {
                        if (PowerManager.getFactionPower(playerFaction).doubleValue() > playerFaction.Claims.size())
                        {
                            if (!EagleFactions.AttackedFactions.containsKey(playerFaction.Name))
                            {
                                if (!playerFaction.Claims.isEmpty())
                                {
                                    if (playerFaction.Name.equals("SafeZone") || playerFaction.Name.equals("WarZone"))
                                    {
                                        FactionLogic.addClaim(playerFaction, world.getUniqueId(), chunk);
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                                    }
                                    else
                                    {
                                        if (MainLogic.requireConnectedClaims())
                                        {
                                            if (FactionLogic.isClaimConnected(playerFaction, world.getUniqueId(), chunk))
                                            {
                                                FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                            }
                                            else
                                            {
                                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));
                                            }
                                        }
                                        else
                                        {
                                            FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                        }
                                    }
                                }
                                else
                                {
                                    FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                }
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOUR_FACTION_IS_UNDER_ATTACK + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.GOLD, PluginMessages.TWO_MINUTES, TextColors.RED, " " + PluginMessages.TO_BE_ABLE_TO_CLAIM_AGAIN));
                            }
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                        }
                    }
                    else
                    {
                        //Check if faction's home was set in this claim. If yes then remove it.
                        if (playerFaction.Home != null)
                        {
                            if (world.getUniqueId().equals(playerFaction.Home.WorldUUID))
                            {
                                Location homeLocation = world.getLocation(playerFaction.Home.BlockPosition);

                                if (homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString()))
                                {
                                    FactionLogic.setHome(world.getUniqueId(), playerFaction, null);
                                }
                            }
                        }

                        FactionLogic.removeClaim(playerFaction, world.getUniqueId(), chunk);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND_HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.UNCLAIMED, TextColors.WHITE, "!"));
                    }
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }

            generateMap(player);
        };
    }
}
