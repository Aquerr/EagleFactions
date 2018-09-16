package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;

import java.util.Optional;

public class FireBlockPlaceListener extends AbstractListener
{
    public FireBlockPlaceListener(EagleFactions eagleFactions)
    {
        super(eagleFactions);
    }

    @Listener(order = Order.EARLY)
    public void onIgnite(ChangeBlockEvent.Place event)
    {
        if (event.getCause().root() instanceof Player)
        {
            Player player = (Player)event.getCause().root();

            if (!EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                event.getTransactions().forEach(x->
                {
                    Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition());

                    if (x.getFinal().getState().getType() == BlockTypes.FIRE
                            && optionalChunkFaction.isPresent() && (optionalChunkFaction.get().getName().equals("SafeZone")
                            || optionalChunkFaction.get().getName().equals("WarZone")))
                    {
                        event.setCancelled(true);
                    }
                });
            }
        }
        else
        {
            event.getTransactions().forEach(x->
            {
                Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition());

                if (x.getFinal().getState().getType() == BlockTypes.FIRE
                        && optionalChunkFaction.isPresent() && (optionalChunkFaction.get().getName().equals("SafeZone")
                        || optionalChunkFaction.get().getName().equals("WarZone")))
                {
                    event.setCancelled(true);
                }
            });
        }
    }
}