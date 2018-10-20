package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
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
import java.util.Set;
import java.util.function.Consumer;

public class MapCommand extends AbstractCommand implements CommandExecutor
{
    public MapCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player) source;
            if (getPlugin().getConfiguration().getConfigFields().getClaimableWorldNames().contains(player.getWorld().getName()))
            {
                generateMap(player);
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_VIEW_MAP_IN_THIS_WORLD));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void generateMap(Player player)
    {
        Set<String> claimsList = getPlugin().getFactionLogic().getAllClaims();
        Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        World world = player.getWorld();

        Text notCapturedMark = Text.of(TextColors.GRAY, "/");
        Text factionMark = Text.of(TextColors.GREEN, "+");
        Text allianceMark = Text.of(TextColors.AQUA, "+");
        Text enemyMark = Text.of(TextColors.RED, "#");
        Text normalFactionMark = Text.of(TextColors.WHITE, "+");
        Text playerLocationMark = Text.of(TextColors.GOLD, "+");

        Vector3i playerPosition = player.getLocation().getChunkPosition();

        List<Text> map = new ArrayList<>();
        List<String> normalFactions = new ArrayList<>();
        List<String> allianceFactions = new ArrayList<>();
        List<String> enemyFactions = new ArrayList<>();
        //String playerFaction = "";

        StringBuilder claimBuilder = new StringBuilder();

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
                claimBuilder.append(world.getUniqueId().toString());
                claimBuilder.append("|");
                claimBuilder.append(chunk.toString());

                if (claimsList.contains(claimBuilder.toString()))
                {
                    Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);

                    if (optionalPlayerFaction.isPresent())
                    {
                        Faction playerFaction = optionalPlayerFaction.get();

                        if (optionalChunkFaction.get().getName().equals(playerFaction.getName()))
                        {
                            textBuilder.append(factionMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
//                            playerFaction = optionalChunkFaction.get();
                        }
                        else if (playerFaction.getAlliances().contains(optionalChunkFaction.get().getName()))
                        {
                            textBuilder.append(allianceMark);
                            if (!allianceFactions.contains(optionalChunkFaction.get().getName()))
                            {
                                allianceFactions.add(optionalChunkFaction.get().getName());
                            }
                        }
                        else if (playerFaction.getEnemies().contains(optionalChunkFaction.get().getName()))
                        {
                            textBuilder.append(enemyMark);
                            if (!enemyFactions.contains(optionalChunkFaction.get().getName()))
                            {
                                enemyFactions.add(optionalChunkFaction.get().getName());
                            }
                        }
                        else
                        {
                            if (optionalChunkFaction.get().getName().equals("SafeZone"))
                            {
                                textBuilder.append(Text.of(TextColors.AQUA, "+"));
                            }
                            else if (optionalChunkFaction.get().getName().equals("WarZone"))
                            {
                                textBuilder.append(Text.of(TextColors.DARK_RED, "#"));
                            }
                            else
                            {
                                textBuilder.append(normalFactionMark);
                            }
                            if (!normalFactions.contains(optionalChunkFaction.get().getName()))
                            {
                                normalFactions.add(optionalChunkFaction.get().getName());
                            }
                        }
                    }
                    else
                    {
                        if (optionalChunkFaction.get().getName().equals("SafeZone"))
                        {
                            textBuilder.append(Text.of(TextColors.AQUA, "+"));
                        }
                        else if (optionalChunkFaction.get().getName().equals("WarZone"))
                        {
                            textBuilder.append(Text.of(TextColors.DARK_RED, "#"));
                        }
                        else
                        {
                            textBuilder.append(normalFactionMark);
                        }
                        if (!normalFactions.contains(optionalChunkFaction.get().getName()))
                        {
                            normalFactions.add(optionalChunkFaction.get().getName());
                        }
                    }
                }
                else
                {
                    if (!getPlugin().getConfiguration().getConfigFields().shouldDelayClaim() &&
                            (EagleFactions.AdminList.contains(player.getUniqueId()) ||
                                    (optionalPlayerFaction.isPresent() &&
                                            (optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()) || optionalPlayerFaction.get().getOfficers().contains(player.getUniqueId())))))
                    {
                        textBuilder.append(notCapturedMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
                    }
                    else
                    {
                        textBuilder.append(notCapturedMark).build();
                    }
                }
                claimBuilder.setLength(0);
            }
            map.add(textBuilder.build());
        }

        String playerPositionClaim = "none";

        Optional<Faction> optionalPlayerPositionFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), playerPosition);

        if (optionalPlayerPositionFaction.isPresent())
        {
            playerPositionClaim = optionalPlayerPositionFaction.get().getName();
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
            player.sendMessage(Text.of(TextColors.GREEN, PluginMessages.YOUR_FACTION + ": ", TextColors.GREEN, optionalPlayerFaction.get().getName()));
        }
        if (!normalFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.WHITE, PluginMessages.FACTIONS + ": ", TextColors.RESET, String.join(",", normalFactions)));
        }
        if (!allianceFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.AQUA, PluginMessages.ALLIANCES + ": " + String.join("," ,allianceFactions)));
        }
        if (!enemyFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.RED, PluginMessages.ENEMIES + ": " + String.join(",", enemyFactions)));
        }

        player.sendMessage(Text.of(PluginMessages.CURRENTLY_STANDING_AT + ": ", TextColors.GOLD, playerPosition.toString(), TextColors.WHITE, " " + PluginMessages.WHICH_IS_CLAIMED_BY + " ", TextColors.GOLD, playerPositionClaim));
    }


    private Consumer<CommandSource> claimByMap(Player player, Vector3i chunk)
    {
        return consumer ->
        {
            //Because faction could have changed we need to get it again here.

            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            World world = player.getWorld();

            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if (playerFaction.getLeader().equals(player.getUniqueId()) || playerFaction.getOfficers().contains(player.getUniqueId()))
                {
                    //We need to check if because player can click on the claim that is already claimed (in the previous map in the chat)
                    if (!getPlugin().getFactionLogic().isClaimed(world.getUniqueId(), chunk))
                    {
                        if (getPlugin().getPowerManager().getFactionPower(playerFaction).doubleValue() > playerFaction.getClaims().size())
                        {
                            if (!EagleFactions.AttackedFactions.containsKey(playerFaction.getName()))
                            {
                                if (!playerFaction.getClaims().isEmpty())
                                {
                                    if (playerFaction.getName().equals("SafeZone") || playerFaction.getName().equals("WarZone"))
                                    {
                                        getPlugin().getFactionLogic().addClaim(playerFaction, world.getUniqueId(), chunk);
                                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                                    }
                                    else
                                    {
                                        if (getPlugin().getConfiguration().getConfigFields().requireConnectedClaims())
                                        {
                                            if (getPlugin().getFactionLogic().isClaimConnected(playerFaction, world.getUniqueId(), chunk))
                                            {
                                                getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                            }
                                            else
                                            {
                                                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));
                                            }
                                        }
                                        else
                                        {
                                            getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                        }
                                    }
                                }
                                else
                                {
                                    getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                }
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_IS_UNDER_ATTACK + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.GOLD, PluginMessages.TWO_MINUTES, TextColors.RED, " " + PluginMessages.TO_BE_ABLE_TO_CLAIM_AGAIN));
                            }
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                        }
                    }
                    else
                    {
                        //Check if faction's home was set in this claim. If yes then remove it.
                        if (playerFaction.getHome() != null)
                        {
                            if (world.getUniqueId().equals(playerFaction.getHome().getWorldUUID()))
                            {
                                Location homeLocation = world.getLocation(playerFaction.getHome().getBlockPosition());

                                if (homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString()))
                                {
                                    getPlugin().getFactionLogic().setHome(world.getUniqueId(), playerFaction, null);
                                }
                            }
                        }

                        getPlugin().getFactionLogic().removeClaim(playerFaction, world.getUniqueId(), chunk);

                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND_HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.UNCLAIMED, TextColors.WHITE, "!"));
                    }
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }

            generateMap(player);
        };
    }
}
