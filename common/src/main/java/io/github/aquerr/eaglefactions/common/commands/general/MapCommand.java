package io.github.aquerr.eaglefactions.common.commands.general;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
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

import java.util.*;
import java.util.function.Consumer;

public class MapCommand extends AbstractCommand
{
    private final ProtectionConfig protectionConfig;
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;

    public MapCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            final Player player = (Player) source;
            if (this.protectionConfig.getClaimableWorldNames().contains(player.getWorld().getName()))
            {
                generateMap(player);
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_VIEW_MAP_IN_THIS_WORLD));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void generateMap(Player player)
    {
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        final boolean showPlayerFactionClaimsOnly = this.factionsConfig.shouldShowOnlyPlayerFactionsClaimsInMap();

        final World world = player.getWorld();

        final Text notCapturedMark = Text.of(TextColors.GRAY, "/");
        final Text factionMark = Text.of(TextColors.GREEN, "+");
        final Text truceMark = Text.of(TextColors.GRAY, "+");
        final Text allianceMark = Text.of(TextColors.AQUA, "+");
        final Text enemyMark = Text.of(TextColors.RED, "#");
        final Text normalFactionMark = Text.of(TextColors.WHITE, "+");
        final Text playerLocationMark = Text.of(TextColors.GOLD, "+");

        final Vector3i playerPosition = player.getLocation().getChunkPosition();

        final List<Text> map = new ArrayList<>();
        final List<String> normalFactions = new ArrayList<>();
        final List<String> truceFactions = new ArrayList<>();
        final List<String> allianceFactions = new ArrayList<>();
        final List<String> enemyFactions = new ArrayList<>();
        //String playerFaction = "";

        //Map resolution
        int mapWidth = 18;
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
                    //TODO: FACTION that player is standing at is not showed in the list.
                    textBuilder.append(playerLocationMark);
                    continue;
                }

                final Vector3i chunk = playerPosition.add(column, 0, row);
                final UUID uuid = world.getUniqueId();

                final Optional<Faction> optionalFaction = this.factionLogic.getFactionByChunk(uuid, chunk);
                if (optionalFaction != null && optionalFaction.isPresent())
                {
                    final Faction chunkFaction = optionalFaction.get();
                    if (optionalPlayerFaction.isPresent())
                    {
                        Faction playerFaction = optionalPlayerFaction.get();

                        if (chunkFaction.getName().equals(playerFaction.getName()))
                        {
                            textBuilder.append(factionMark.toBuilder().onClick(TextActions.executeCallback(claimByMap(player, chunk))).build());
//                            playerFaction = optionalChunkFaction.get();
                        }
                        else if (!showPlayerFactionClaimsOnly && playerFaction.getAlliances().contains(chunkFaction.getName()))
                        {
                            textBuilder.append(allianceMark);
                            if (!allianceFactions.contains(chunkFaction.getName()))
                            {
                                allianceFactions.add(chunkFaction.getName());
                            }
                        }
                        else if(!showPlayerFactionClaimsOnly && playerFaction.getTruces().contains(chunkFaction.getName()))
                        {
                            textBuilder.append(truceMark);
                            if(!truceFactions.contains(chunkFaction.getName()))
                            {
                                truceFactions.add(chunkFaction.getName());
                            }
                        }
                        else if (!showPlayerFactionClaimsOnly && playerFaction.getEnemies().contains(chunkFaction.getName()))
                        {
                            textBuilder.append(enemyMark);
                            if (!enemyFactions.contains(chunkFaction.getName()))
                            {
                                enemyFactions.add(chunkFaction.getName());
                            }
                        }
                        else
                        {
                            if (chunkFaction.isSafeZone())
                            {
                                textBuilder.append(Text.of(TextColors.AQUA, "+"));
                            }
                            else if (chunkFaction.isWarZone())
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
                            if (!showPlayerFactionClaimsOnly && !normalFactions.contains(chunkFaction.getName()))
                            {
                                normalFactions.add(chunkFaction.getName());
                            }
                        }
                    }
                    else
                    {
                        if (chunkFaction.isSafeZone())
                        {
                            textBuilder.append(Text.of(TextColors.AQUA, "+"));
                        }
                        else if (chunkFaction.isWarZone())
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
                        if (!showPlayerFactionClaimsOnly && !normalFactions.contains(chunkFaction.getName()))
                        {
                            normalFactions.add(chunkFaction.getName());
                        }
                    }

//                    Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something went really wrong..."));
//                    Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Chunk exists in claim list but not in the factions' list."));
//                    Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Chunk: ", TextColors.GOLD, claim.toString()));
//                    Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Player that used map: ", TextColors.GOLD, player.toString()));
//                    Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Check if this claim exists in the ", TextColors.GOLD, "factions.conf."));
//                    Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "And report this bug to the plugin owner."));
                }
                else
                {
                    if(!this.factionsConfig.shouldDelayClaim()
                            && (super.getPlugin().getPlayerManager().hasAdminMode(player)
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
        player.sendMessage(Text.of(TextColors.GREEN, Messages.FACTIONS_MAP_HEADER));
        for (Text text : map)
        {
            player.sendMessage(Text.of(text));
        }
        player.sendMessage(Text.of(TextColors.GREEN, Messages.FACTIONS_MAP_FOOTER));

        //Print factions on map
        if (optionalPlayerFaction.isPresent())
        {
            player.sendMessage(Text.of(TextColors.GREEN, Messages.YOUR_FACTION + ": ", TextColors.GREEN, optionalPlayerFaction.get().getName()));
        }
        if (!normalFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.WHITE, Messages.FACTIONS + ": ", TextColors.RESET, String.join(",", normalFactions)));
        }
        if(!truceFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.AQUA, Messages.TRUCES + ": " + String.join(",", truceFactions)));
        }
        if (!allianceFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.AQUA, Messages.ALLIANCES + ": " + String.join(",", allianceFactions)));
        }
        if (!enemyFactions.isEmpty())
        {
            player.sendMessage(Text.of(TextColors.RED, Messages.ENEMIES + ": " + String.join(",", enemyFactions)));
        }

        player.sendMessage(Text.of(MessageLoader.parseMessage(Messages.CURRENTLY_STANDING_AT_CLAIM_WHICH_IS_CLAIMED_BY, TextColors.RESET, ImmutableMap.of(Placeholders.CLAIM, Text.of(TextColors.GOLD, playerPosition.toString()), Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, playerPositionClaim)))));
    }


    private Consumer<CommandSource> claimByMap(Player player, Vector3i chunk)
    {
        return consumer ->
        {
            //Because faction could have changed we need to get it again here.

            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            final World world = player.getWorld();
            final Claim claim = new Claim(player.getWorld().getUniqueId(), chunk);
            final boolean hasFactionsAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player);

            if(!optionalPlayerFaction.isPresent())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                return;
            }

            final Faction playerFaction = optionalPlayerFaction.get();
            final boolean hasClaimPermission = super.getPlugin().getPermsManager().canClaim(player.getUniqueId(), playerFaction);
            final boolean isFactionAttacked = EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName());

            if(!hasFactionsAdminMode && !hasClaimPermission)
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));
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
                        final Location<World> homeLocation = world.getLocation(playerFaction.getHome().getBlockPosition());
                        if (homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString()))
                        {
                            super.getPlugin().getFactionLogic().setHome(playerFaction, null);
                        }
                    }
                }
                super.getPlugin().getFactionLogic().removeClaim(playerFaction, new Claim(world.getUniqueId(), chunk));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.LAND_HAS_BEEN_SUCCESSFULLY_UNCLAIMED));
            }
            else
            {
                if(isFactionAttacked)
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, TextColors.RED, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()))))));
                    return;
                }

                if(super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size())
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                    return;
                }

                if (playerFaction.isSafeZone() || playerFaction.isWarZone())
                {
                    super.getPlugin().getFactionLogic().addClaim(playerFaction, claim);
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
                }
                else
                {
                    if(this.factionsConfig.requireConnectedClaims())
                    {
                        if(super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, claim))
                        {
                            super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.CLAIMS_NEED_TO_BE_CONNECTED));
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
