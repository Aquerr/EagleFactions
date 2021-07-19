package io.github.aquerr.eaglefactions.commands.general;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        if (isPlayer(context))
        {
            final ServerPlayer player = (ServerPlayer) context.cause().audience();
            if (this.protectionConfig.getClaimableWorldNames().contains(((TextComponent)player.world().properties().displayName().get()).content()))
            {
                generateMap(player);
            }
            else
            {
                context.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANT_VIEW_MAP_IN_THIS_WORLD, NamedTextColor.RED)));
            }
        }
        else
        {
            context.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(Component.text(Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND, NamedTextColor.RED)));
        }

        return CommandResult.success();
    }

    private void generateMap(ServerPlayer player)
    {
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        final boolean showPlayerFactionClaimsOnly = this.factionsConfig.shouldShowOnlyPlayerFactionsClaimsInMap();

        final ServerWorld world = player.world();

        final TextComponent notCapturedMark = Component.text("/", NamedTextColor.GRAY);
        final TextComponent factionMark = Component.text("+", NamedTextColor.GREEN);
        final TextComponent truceMark = Component.text("+", NamedTextColor.GRAY);
        final TextComponent allianceMark = Component.text("+", NamedTextColor.AQUA);
        final TextComponent enemyMark = Component.text("#", NamedTextColor.RED);
        final TextComponent normalFactionMark = Component.text("+", NamedTextColor.WHITE);
        final TextComponent playerLocationMark = Component.text("+", NamedTextColor.GOLD);

        final Vector3i playerPosition = player.serverLocation().chunkPosition();

        final List<TextComponent> map = new LinkedList<>();
        final List<String> normalFactions = new LinkedList<>();
        final List<String> truceFactions = new LinkedList<>();
        final List<String> allianceFactions = new LinkedList<>();
        final List<String> enemyFactions = new LinkedList<>();
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
            TextComponent.Builder textBuilder = Component.text();

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
                            textBuilder.append(factionMark.toBuilder().clickEvent(SpongeComponents.executeCallback((commandCause) -> claimByMap(player, chunk))).build());
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
                                textBuilder.append(Component.text("+", NamedTextColor.AQUA));
                            }
                            else if (chunkFaction.isWarZone())
                            {
                                textBuilder.append(Component.text("#", NamedTextColor.DARK_RED));
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
                            textBuilder.append(Component.text("+", NamedTextColor.AQUA));
                        }
                        else if (chunkFaction.isWarZone())
                        {
                            textBuilder.append(Component.text("#", NamedTextColor.DARK_RED));
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
                                    && (optionalPlayerFaction.get().getLeader().equals(player.uniqueId())
                                        || optionalPlayerFaction.get().getOfficers().contains(player.uniqueId())))))
                    {
                        textBuilder.append(notCapturedMark.toBuilder().clickEvent(SpongeComponents.executeCallback((commandCause) -> claimByMap(player, chunk))).build());
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
        player.sendMessage(Component.text(Messages.FACTIONS_MAP_HEADER, NamedTextColor.GREEN));
        for (TextComponent text : map)
        {
            player.sendMessage(text);
        }
        player.sendMessage(Component.text(Messages.FACTIONS_MAP_FOOTER, NamedTextColor.GREEN));

        //Print factions on map
        if (optionalPlayerFaction.isPresent())
        {
            player.sendMessage(Component.text(Messages.YOUR_FACTION + ": ", NamedTextColor.GREEN).append(Component.text(optionalPlayerFaction.get().getName(), NamedTextColor.GREEN)));
        }
        if (!normalFactions.isEmpty())
        {
            player.sendMessage(Component.text(Messages.FACTIONS + ": ", NamedTextColor.WHITE).append(Component.text(String.join(",", normalFactions))));
        }
        if(!truceFactions.isEmpty())
        {
            player.sendMessage(Component.text(Messages.TRUCES + ": " + String.join(",", truceFactions), NamedTextColor.AQUA));
        }
        if (!allianceFactions.isEmpty())
        {
            player.sendMessage(Component.text(Messages.ALLIANCES + ": " + String.join(",", allianceFactions), NamedTextColor.AQUA));
        }
        if (!enemyFactions.isEmpty())
        {
            player.sendMessage(Component.text(Messages.ENEMIES + ": " + String.join(",", enemyFactions), NamedTextColor.RED));
        }

        player.sendMessage(MessageLoader.parseMessage(Messages.CURRENTLY_STANDING_AT_CLAIM_WHICH_IS_CLAIMED_BY, NamedTextColor.WHITE, ImmutableMap.of(Placeholders.CLAIM, Component.text(playerPosition.toString(), NamedTextColor.GOLD), Placeholders.FACTION_NAME, Component.text(playerPositionClaim, NamedTextColor.GOLD))));
    }


    private Consumer<CommandCause> claimByMap(ServerPlayer player, Vector3i chunk)
    {
        return consumer ->
        {
            //Because faction could have changed we need to get it again here.

            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
            final ServerWorld world = player.world();
            final Claim claim = new Claim(player.world().uniqueId(), chunk);
            final boolean hasFactionsAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player);

            if(!optionalPlayerFaction.isPresent())
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));
                return;
            }

            final Faction playerFaction = optionalPlayerFaction.get();
            final boolean hasClaimPermission = super.getPlugin().getPermsManager().canClaim(player.uniqueId(), playerFaction);
            final boolean isFactionAttacked = EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName());

            if(!hasFactionsAdminMode && !hasClaimPermission)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS, NamedTextColor.RED)));
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
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.LAND_HAS_BEEN_SUCCESSFULLY_UNCLAIMED, NamedTextColor.GREEN)));
                EventRunner.runFactionUnclaimEventPost(player, playerFaction, world, chunk);
            }
            else
            {
                if(isFactionAttacked)
                {
                    player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", NamedTextColor.RED).append(MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, NamedTextColor.RED, Collections.singletonMap(Placeholders.NUMBER, Component.text(EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()), NamedTextColor.GOLD))))));
                    return;
                }

                if(super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size())
                {
                    player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS, NamedTextColor.RED)));
                    return;
                }

                if (playerFaction.isSafeZone() || playerFaction.isWarZone())
                {
                    if (EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk))
                        return;

                    super.getPlugin().getFactionLogic().addClaim(playerFaction, claim);
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.LAND + " ")).append(Component.text(chunk.toString(), NamedTextColor.GOLD)).append(Component.text(" " + Messages.HAS_BEEN_SUCCESSFULLY + " ")).append(Component.text(Messages.CLAIMED, NamedTextColor.GOLD)).append(Component.text("!")));
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
                            player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.CLAIMS_NEED_TO_BE_CONNECTED, NamedTextColor.RED)));
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
        };
    }
}
