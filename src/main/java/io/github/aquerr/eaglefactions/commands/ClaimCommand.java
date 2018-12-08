package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.events.FactionClaimEvent;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ClaimCommand extends AbstractCommand
{
    public ClaimCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player) source;
            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

            if (optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if (this.getPlugin().getFlagManager().canClaim(player.getUniqueId(), playerFaction))
                {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);
                    if(!getPlugin().getConfiguration().getConfigFields().getClaimableWorldNames().contains(player.getWorld().getName()))
                    {
                        player.sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, "You can not claim territories in this world!")));
                        return CommandResult.success();
                    }
                    else
                    {
                        if (!optionalChunkFaction.isPresent())
                        {
                            if (getPlugin().getPowerManager().getFactionPower(playerFaction).doubleValue() > playerFaction.getClaims().size())
                            {
                                if (!EagleFactions.AttackedFactions.containsKey(playerFaction.getName()))
                                {
                                    if (!playerFaction.getClaims().isEmpty())
                                    {
                                        if (playerFaction.getName().equals("SafeZone") || playerFaction.getName().equals("WarZone"))
                                        {
                                            FactionClaimEvent event = new FactionClaimEvent(player, playerFaction, world, chunk, Cause.of(EventContext.builder().add(EventContextKeys.OWNER, player).build(), player));
                                            Sponge.getEventManager().post(event);
                                            getPlugin().getFactionLogic().addClaim(playerFaction, world.getUniqueId(), chunk);
                                            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));

                                            return CommandResult.success();
                                        }
                                        else
                                        {
                                            if (getPlugin().getConfiguration().getConfigFields().requireConnectedClaims())
                                            {
                                                if (getPlugin().getFactionLogic().isClaimConnected(playerFaction, world.getUniqueId(), chunk))
                                                {
                                                    getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                                    return CommandResult.success();
                                                }
                                                else
                                                {
                                                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));
                                                }
                                            }
                                            else
                                            {
                                                getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                                return CommandResult.success();
                                            }
                                        }
                                    }
                                    else
                                    {
                                        getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
                                        return CommandResult.success();
                                    }
                                }
                                else
                                {
                                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_IS_UNDER_ATTACK + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.GOLD, PluginMessages.TWO_MINUTES, TextColors.RED, " " + PluginMessages.TO_BE_ABLE_TO_CLAIM_AGAIN));
                                }
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                        }
                    }
                }
                else if (EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    if (!getPlugin().getFactionLogic().isClaimed(world.getUniqueId(), chunk))
                    {
                        getPlugin().getFactionLogic().addClaim(playerFaction, world.getUniqueId(), chunk);

                        FactionClaimEvent event = new FactionClaimEvent(player, playerFaction, world, chunk, Cause.of(EventContext.builder().add(EventContextKeys.OWNER, player).build(), player));
                        Sponge.getEventManager().post(event);

                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                        return CommandResult.success();
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
