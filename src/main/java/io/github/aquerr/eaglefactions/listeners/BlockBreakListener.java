package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class BlockBreakListener
{
    @Inject
    private FactionsCache cache;

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event)
    {
        Player cause = null;
        Optional<Faction> optionalPlayerFaction = Optional.empty();
        World world;
        if (event.getCause().root() instanceof Player)
        {
            cause = (Player) event.getCause().root();
            if (EagleFactions.AdminList.contains(cause.getUniqueId()))
            {
                return;
            }
            optionalPlayerFaction = cache.getFactionByPlayer(cause.getUniqueId());
            world = cause.getWorld();
        } else
        {
            world = event.getTransactions().get(0).getFinal().getLocation().get().getExtent();
        }

        if (MainLogic.getSafeZoneWorldNames().contains(world.getName()) || (MainLogic.getWarZoneWorldNames().contains(world.getName()) && MainLogic.isBlockDestroyingInWarZoneDisabled()))
        {
            event.setCancelled(true);
            return;
        }


        for (Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            Optional<Faction> optionalChunkFaction = cache.getFactionByChunk(world.getUniqueId(), transaction.getFinal().getLocation().get().getChunkPosition());
            if (optionalChunkFaction.isPresent())
            {
                if (cause != null)
                {
                    if (optionalChunkFaction.get().Name.equals("SafeZone") && cause.hasPermission(PluginPermissions.SAFE_ZONE_BUILD) || (optionalChunkFaction.get().Name.equals("WarZone") && cause.hasPermission(PluginPermissions.WAR_ZONE_BUILD)))
                    {
                        return;
                    } else if (optionalPlayerFaction.isPresent())
                    {
                        if (!FlagManager.canBreakBlock(cause, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                        {
                            cause.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                            event.setCancelled(true);
                            return;
                        }
                    } else
                    {
                        event.setCancelled(true);
                        cause.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                        return;
                    }
                } else
                {
                    if (!optionalChunkFaction.get().Name.equals("SafeZone") && !optionalChunkFaction.get().Name.equals("WarZone") && MainLogic.isBlockDestroyingDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    } else if (optionalChunkFaction.get().Name.equals("SafeZone"))
                    {
                        event.setCancelled(true);
                        return;
                    } else if (optionalChunkFaction.get().Name.equals("WarZone") && MainLogic.isBlockDestroyingInWarZoneDisabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
