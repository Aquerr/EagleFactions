package io.github.aquerr.eaglefactions.listeners;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockBreakListener extends AbstractListener
{
    public BlockBreakListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onBlockBreak(ChangeBlockEvent.Pre event)
    {
        if(event.getContext().containsKey(EventContextKeys.PLAYER_BREAK))
        {
            List<Location<World>> locationList = new ArrayList<>(event.getLocations());
            for(Location<World> location : locationList)
            {
                BlockState blockState = location.getBlock();
                if(blockState.getType() == BlockTypes.FLOWING_WATER)
                {
                    return;
                }

                if(event.getContext().containsKey(EventContextKeys.OWNER))
                {
                    Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
                    if(!EagleFactions.AdminList.contains(player.getUniqueId()))
                    {
                        World world = player.getWorld();

                        if(getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()))
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                            event.setCancelled(true);
                            return;
                        }
                        else if(getPlugin().getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                            event.setCancelled(true);
                            return;
                        }

                        Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                        Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
                        if(optionalChunkFaction.isPresent())
                        {
                            if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                            {
                                return;
                            }
                            else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                            {
                                return;
                            }
                            else if(optionalPlayerFaction.isPresent())
                            {
                                if(!getPlugin().getFlagManager().canBreakBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                                {
                                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                            else
                            {
                                event.setCancelled(true);
                                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                                return;
                            }
                        }
                    }
                }
                else
                {
                    if(blockState.getType() == BlockTypes.FLOWING_WATER)
                    {
                        return;
                    }

                    World world = location.getExtent();

                    if(getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if(getPlugin().getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }

                    Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
                    if(optionalChunkFaction.isPresent())
                    {
                        if(!optionalChunkFaction.get().getName().equals("SafeZone") && !optionalChunkFaction.get().getName().equals("WarZone") && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtClaimsDisabled())
                        {
                            event.setCancelled(true);
                            return;
                        }
                        else if(optionalChunkFaction.get().getName().equals("SafeZone"))
                        {
                            event.setCancelled(true);
                            return;
                        }
                        else if(optionalChunkFaction.get().getName().equals("WarZone") && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
                        {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onBlockBreak(ChangeBlockEvent.Break event)
    {
        if(event.getCause().root() instanceof Player)
        {
            Player player = (Player)event.getCause().root();

            if(!EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                for (Transaction<BlockSnapshot> transaction : event.getTransactions())
                {
                    World world = player.getWorld();

                    if (getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if (getPlugin().getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }

                    Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();
                    Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                    Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), claim);

                    if(optionalChunkFaction.isPresent())
                    {
                        if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                        {
                            return;
                        }
                        else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                        {
                            return;
                        }
                        else if(optionalPlayerFaction.isPresent())
                        {
                            if (!getPlugin().getFlagManager().canBreakBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                            {
                                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                                event.setCancelled(true);
                                return;
                            }
                        }
                        else
                        {
                            event.setCancelled(true);
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                            return;
                        }
                    }
                }
            }
        }
        else
        {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                if(transaction.getOriginal().getState().getType() == BlockTypes.FLOWING_WATER)
                {
                    return;
                }

                World world = transaction.getFinal().getLocation().get().getExtent();

                if (getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()))
                {
                    event.setCancelled(true);
                    return;
                }
                else if (getPlugin().getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
                {
                    event.setCancelled(true);
                    return;
                }

                Vector3i claim = transaction.getFinal().getLocation().get().getChunkPosition();
                Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), claim);

                if (optionalChunkFaction.isPresent())
                {
                    if(!optionalChunkFaction.get().getName().equals("SafeZone") && !optionalChunkFaction.get().getName().equals("WarZone") && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtClaimsDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if(optionalChunkFaction.get().getName().equals("SafeZone"))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    else if (optionalChunkFaction.get().getName().equals("WarZone") && getPlugin().getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
