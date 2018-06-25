package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ClaimCommand implements CommandExecutor
{
    @Inject
    private FactionsCache cache;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player) source;
            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

            if (optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if (FlagManager.canClaim(player, playerFaction))
                {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), chunk);

                    if (MainLogic.getClaimableWorldNames().contains(player.getWorld().getName()))
                    {
                        if (!optionalChunkFaction.isPresent())
                        {
                            if (PowerManager.getFactionPower(playerFaction).doubleValue() > playerFaction.Claims.size())
                            {
                                if (!EagleFactions.AttackedFactions.containsKey(playerFaction.Name))
                                {
                                    if (!playerFaction.Claims.isEmpty())
                                    {
                                        if (playerFaction.Name.equals("SafeZone") || playerFaction.Name.equals("WarZone"))
                                        {
                                            cache.addOrSetClaim(world.getUniqueId(), chunk, playerFaction.Name);
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));

                                            return CommandResult.success();
                                        } else
                                        {
                                            if (MainLogic.requireConnectedClaims())
                                            {
                                                if (FactionLogic.isClaimConnected(playerFaction, world.getUniqueId(), chunk))
                                                {
                                                    FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                                    return CommandResult.success();
                                                } else
                                                {
                                                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));
                                                }
                                            } else
                                            {
                                                FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                                return CommandResult.success();
                                            }
                                        }
                                    } else
                                    {
                                        FactionLogic.startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                        return CommandResult.success();
                                    }
                                } else
                                {
                                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOUR_FACTION_IS_UNDER_ATTACK + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.GOLD, PluginMessages.TWO_MINUTES, TextColors.RED, " " + PluginMessages.TO_BE_ABLE_TO_CLAIM_AGAIN));
                                }
                            } else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                            }
                        } else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                        }
                    }
                } else if (EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    if (!cache.getClaimOwner(world.getUniqueId(), chunk).isPresent())
                    {
                        cache.addOrSetClaim(world.getUniqueId(), chunk, playerFaction.Name);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                        return CommandResult.success();
                    } else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                    }
                } else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));
                }
            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
