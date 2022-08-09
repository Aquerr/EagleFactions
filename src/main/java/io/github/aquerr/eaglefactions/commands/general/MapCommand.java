package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class MapCommand extends AbstractCommand
{
    private final ProtectionConfig protectionConfig;
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final MessageService messageService;

    public MapCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        if (this.protectionConfig.getClaimableWorldNames().contains(player.world().key().asString()))
        {
            generateMap(player);
        }
        else
        {
            throw messageService.resolveExceptionWithMessage("error.command.map.you-cant-view-map-in-this-world");
        }

        return CommandResult.success();
    }

    private void generateMap(ServerPlayer player)
    {
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        final boolean showPlayerFactionClaimsOnly = this.factionsConfig.shouldShowOnlyPlayerFactionsClaimsInMap();

        final ServerWorld world = player.world();

        final TextComponent notCapturedMark = text("/", GRAY);
        final TextComponent factionMark = text("+", GREEN);
        final TextComponent truceMark = text("+", GRAY);
        final TextComponent allianceMark = text("+", AQUA);
        final TextComponent enemyMark = text("#", RED);
        final TextComponent normalFactionMark = text("+", WHITE);
        final TextComponent playerLocationMark = text("+", GOLD);
        final TextComponent safeZoneMarker = text("+", AQUA);
        final TextComponent warZoneMarker = text("#", DARK_RED);

        final Vector3i playerPosition = player.serverLocation().chunkPosition();

        final List<Component> map = new ArrayList<>();
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
            TextComponent.Builder textBuilder = text();

            for (int column = -halfMapWidth; column <= halfMapWidth; column++)
            {
                if (row == 0 && column == 0)
                {
                    //TODO: FACTION that player is standing at is not showed in the list.
                    textBuilder.append(playerLocationMark);
                    continue;
                }

                final Vector3i chunk = playerPosition.add(column, 0, row);
                final UUID uuid = world.uniqueId();

                final Optional<Faction> optionalFaction = this.factionLogic.getFactionByChunk(uuid, chunk);
                if (optionalFaction != null && optionalFaction.isPresent())
                {
                    final Faction chunkFaction = optionalFaction.get();
                    if (optionalPlayerFaction.isPresent())
                    {
                        Faction playerFaction = optionalPlayerFaction.get();

                        if (chunkFaction.getName().equals(playerFaction.getName()))
                        {
                            textBuilder.append(factionMark.toBuilder().clickEvent(SpongeComponents.executeCallback((cause) -> claimByMap(player, chunk))).build());
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
                                textBuilder.append(safeZoneMarker);
                            }
                            else if (chunkFaction.isWarZone())
                            {
                                textBuilder.append(warZoneMarker);
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
                            textBuilder.append(safeZoneMarker);
                        }
                        else if (chunkFaction.isWarZone())
                        {
                            textBuilder.append(warZoneMarker);
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
                    if(!this.factionsConfig.shouldDelayClaim()
                            && (super.getPlugin().getPlayerManager().hasAdminMode(player.user())
                                || (optionalPlayerFaction.isPresent()
                                    && (optionalPlayerFaction.get().getLeader().equals(player.uniqueId())
                                        || optionalPlayerFaction.get().getOfficers().contains(player.uniqueId())))))
                    {
                        textBuilder.append(notCapturedMark.toBuilder().clickEvent(SpongeComponents.executeCallback((cause) -> claimByMap(player, chunk))).build());
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

        Optional<Faction> optionalPlayerPositionFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), playerPosition);

        if (optionalPlayerPositionFaction.isPresent())
        {
            playerPositionClaim = optionalPlayerPositionFaction.get().getName();
        }

        //Print map
        player.sendMessage(messageService.resolveComponentWithMessage("command.map.header"));
        for (Component text : map)
        {
            player.sendMessage(text);
        }
        player.sendMessage(messageService.resolveComponentWithMessage("command.map.footer"));

        //Print factions on map
        if (optionalPlayerFaction.isPresent())
        {
            player.sendMessage(messageService.resolveComponentWithMessage("command.map.your-faction", optionalPlayerPositionFaction.get().getName()));
        }
        if (!normalFactions.isEmpty())
        {
            player.sendMessage(messageService.resolveComponentWithMessage("command.map.factions", String.join(",", normalFactions)));
        }
        if(!truceFactions.isEmpty())
        {
            player.sendMessage(messageService.resolveComponentWithMessage("command.map.truces", String.join(",", truceFactions)));
        }
        if (!allianceFactions.isEmpty())
        {
            player.sendMessage(messageService.resolveComponentWithMessage("command.map.alliances", String.join(",", allianceFactions)));
        }
        if (!enemyFactions.isEmpty())
        {
            player.sendMessage(messageService.resolveComponentWithMessage("command.map.enemies", String.join(",", enemyFactions)));
        }

        player.sendMessage(messageService.resolveComponentWithMessage("command.map.currently-standing-at", playerPosition.toString(), playerPositionClaim));
    }

    private void claimByMap(ServerPlayer player, Vector3i chunk)
    {
        //Because faction could have changed we need to get it again here.

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        final ServerWorld world = player.world();
        final Claim claim = new Claim(player.world().uniqueId(), chunk);
        final boolean hasFactionsAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player.user());

        if(!optionalPlayerFaction.isPresent())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY)));
            return;
        }

        final Faction playerFaction = optionalPlayerFaction.get();
        final boolean hasClaimPermission = super.getPlugin().getPermsManager().canClaim(player.uniqueId(), playerFaction);
        final boolean isFactionAttacked = EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName());

        if(!hasFactionsAdminMode && !hasClaimPermission)
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.claim.players-with-your-rank-cant-claim-lands")));
            return;
        }

        //If claimed then unclaim
        if(super.getPlugin().getFactionLogic().isClaimed(world.uniqueId(), chunk))
        {
            if (EventRunner.runFactionUnclaimEventPre(player, playerFaction, world, chunk))
                return;

            //Check if faction's home was set in this claim. If yes then remove it.
            if (playerFaction.getHome() != null)
            {
                if (world.uniqueId().equals(playerFaction.getHome().getWorldUUID()))
                {
                    final ServerLocation homeLocation = world.location(playerFaction.getHome().getBlockPosition());
                    if (homeLocation.chunkPosition().toString().equals(player.serverLocation().chunkPosition().toString()))
                    {
                        super.getPlugin().getFactionLogic().setHome(playerFaction, null);
                    }
                }
            }
            super.getPlugin().getFactionLogic().removeClaim(playerFaction, new Claim(world.uniqueId(), chunk));
            player.sendMessage(messageService.resolveMessageWithPrefix("command.unclaim.land-has-been-successfully-unclaimed", chunk.toString()));
            EventRunner.runFactionUnclaimEventPost(player, playerFaction, world, chunk);
        }
        else
        {
            if(isFactionAttacked)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.claim.faction.under-attack", EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()))));
                return;
            }

            if(super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size())
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.claim.faction.not-enough-power")));
                return;
            }

            if (playerFaction.isSafeZone() || playerFaction.isWarZone())
            {
                if (EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk))
                    return;

                super.getPlugin().getFactionLogic().addClaim(playerFaction, claim);
                player.sendMessage(messageService.resolveMessageWithPrefix("command.claim.land-has-been-successfully-claimed", chunk.toString()));
            }
            else
            {
                if(this.factionsConfig.requireConnectedClaims())
                {
                    if(super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, claim))
                    {
                        if (EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk))
                            return;

                        super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.uniqueId(), chunk);
                    }
                    else
                    {
                        player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.claim.claim.claims-need-to-be-connected")));
                    }
                }
                else
                {
                    if (EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk))
                        return;

                    super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.uniqueId(), chunk);
                }
            }
        }
        generateMap(player);
    }
}
