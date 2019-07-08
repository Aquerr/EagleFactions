package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
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

public class MapCommand extends AbstractCommand
{
    public MapCommand(EagleFactionsPlugin plugin)
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
        Set<Claim> claimsList = super.getPlugin().getFactionLogic().getAllClaims();
        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        boolean showPlayerFactionClaimsOnly = super.getPlugin().getConfiguration().getConfigFields().shouldShowOnlyPlayerFactionsClaimsInMap();

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

        //Map resolution
        int mapWidth = 18;
        int mapHeight = 7;

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
                    //TODO: FACTION that player is standing at is not showed in the list.
                    textBuilder.append(playerLocationMark);
                    continue;
                }

                Vector3i chunk = playerPosition.add(column, 0, row);
                Claim claim = new Claim(world.getUniqueId(), chunk);

                if (claimsList.contains(claim))
                {
                    Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);

                    //TODO: This check is unnecessary however code is crashing sometimes without it.
                    if(optionalChunkFaction.isPresent())
                    {
                        if (optionalPlayerFaction.isPresent())
                        {
                            Faction playerFaction = optionalPlayerFaction.get();

                            if (optionalChunkFaction.get().getName().equals(playerFaction.getName()))
                            {
                                textBuilder.append(factionMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
//                            playerFaction = optionalChunkFaction.get();
                            }
                            else if (!showPlayerFactionClaimsOnly && playerFaction.getAlliances().contains(optionalChunkFaction.get().getName()))
                            {
                                textBuilder.append(allianceMark);
                                if (!allianceFactions.contains(optionalChunkFaction.get().getName()))
                                {
                                    allianceFactions.add(optionalChunkFaction.get().getName());
                                }
                            }
                            else if (!showPlayerFactionClaimsOnly && playerFaction.getEnemies().contains(optionalChunkFaction.get().getName()))
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
                                    if(!showPlayerFactionClaimsOnly)
                                    {
                                        textBuilder.append(normalFactionMark);
                                    }
                                    else
                                    {
                                        textBuilder.append(notCapturedMark);
                                    }
                                }
                                if (!showPlayerFactionClaimsOnly && !normalFactions.contains(optionalChunkFaction.get().getName()))
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
                                if(!showPlayerFactionClaimsOnly)
                                {
                                    textBuilder.append(normalFactionMark);
                                }
                                else
                                {
                                    textBuilder.append(notCapturedMark);
                                }
                            }
                            if (!showPlayerFactionClaimsOnly && !normalFactions.contains(optionalChunkFaction.get().getName()))
                            {
                                normalFactions.add(optionalChunkFaction.get().getName());
                            }
                        }
                    }
                    else
                    {
                        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something went really wrong..."));
                        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Chunk exists in claim list but not in the factions' list."));
                        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Chunk: ", TextColors.GOLD, claim.toString()));
                        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Player that used map: ", TextColors.GOLD, player.toString()));
                        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Check if this claim exists in the ", TextColors.GOLD, "factions.conf."));
                        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "And report this bug to the plugin owner."));
                    }
                }
                else
                {
                    if(!super.getPlugin().getConfiguration().getConfigFields().shouldDelayClaim()
                            && (EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId())
                                || (optionalPlayerFaction.isPresent()
                                    && (optionalPlayerFaction.get().getLeader().equals(player.getUniqueId())
                                        || optionalPlayerFaction.get().getOfficers().contains(player.getUniqueId())))))
                    {
                        textBuilder.append(notCapturedMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
                    }
                    else
                    {
                        textBuilder.append(notCapturedMark);
                    }
                }
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

            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            final World world = player.getWorld();
            final Claim claim = new Claim(player.getWorld().getUniqueId(), chunk);
            final boolean hasFactionsAdminMode = EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId());

            if(!optionalPlayerFaction.isPresent())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                return;
            }

            final Faction playerFaction = optionalPlayerFaction.get();
            final boolean hasClaimPermission = super.getPlugin().getFlagManager().canClaim(player.getUniqueId(), playerFaction);
            final boolean isFactionAttacked = EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName());

            if(!hasFactionsAdminMode && !hasClaimPermission)
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));
                return;
            }

            //If claimed then unclaim
            if(super.getPlugin().getFactionLogic().isClaimed(world.getUniqueId(), chunk))
            {
                //Check if faction's home was set in this claim. If yes then remove it.
                if (playerFaction.getHome() != null)
                {
                    if (world.getUniqueId().equals(playerFaction.getHome().getWorldUUID()))
                    {
                        Location homeLocation = world.getLocation(playerFaction.getHome().getBlockPosition());
                        if (homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString()))
                        {
                            super.getPlugin().getFactionLogic().setHome(world.getUniqueId(), playerFaction, null);
                        }
                    }
                }
                super.getPlugin().getFactionLogic().removeClaim(playerFaction, new Claim(world.getUniqueId(), chunk));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND_HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.UNCLAIMED, TextColors.WHITE, "!"));
            }
            else
            {
                if(isFactionAttacked)
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_IS_UNDER_ATTACK + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.GOLD, PluginMessages.TWO_MINUTES, TextColors.RED, " " + PluginMessages.TO_BE_ABLE_TO_CLAIM_AGAIN));
                    return;
                }

                if(super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size())
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                    return;
                }

                if (playerFaction.getName().equalsIgnoreCase("SafeZone") || playerFaction.getName().equalsIgnoreCase("WarZone"))
                {
                    super.getPlugin().getFactionLogic().addClaim(playerFaction, claim);
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                }
                else
                {
                    if (super.getPlugin().getConfiguration().getConfigFields().requireConnectedClaims())
                    {
                        if (super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, claim))
                        {
                            super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));
                        }
                    }
                    else
                    {
                        super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                    }
                }
            }
            generateMap(player);
        };
    }
}
