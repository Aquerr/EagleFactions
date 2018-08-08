package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Optional;

public class FireBlockPlaceListener
{
    @Listener
    public void onIgnite(ChangeBlockEvent.Place event)
    {
        if (event.getCause().root() instanceof Player)
        {
            Player player = (Player)event.getCause().root();

            if (!EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                event.getTransactions().forEach(x->
                {
                    Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition());

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
                Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition());

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