package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.AttackLogic;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

@Singleton
public class PlayerDeathListener extends GenericListener
{

    @Inject
    PlayerDeathListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, EventManager eventManager)
    {
        super(cache, settings, eagleFactions, eventManager);
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event)
    {
        if (event.getTargetEntity() instanceof Player)
        {
            Player player = (Player) event.getTargetEntity();

            PowerManager.decreasePower(player.getUniqueId());

            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(settings.getPowerDecrement()) + "\n",
                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(PowerManager.getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(PowerManager.getPlayerMaxPower(player.getUniqueId()))));

            Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());

            if (settings.getWarZoneWorldNames().contains(player.getWorld().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().Name.equals("WarZone")))
            {
                PlayerManager.setDeathInWarZone(player.getUniqueId(), true);
            }

            if (settings.shouldBlockHomeAfterDeathInOwnFaction())
            {
                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

                if (optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent() && optionalChunkFaction.get().Name.equals(optionalPlayerFaction.get().Name))
                {
                    AttackLogic.blockHome(player.getUniqueId());
                }
            }
        }
    }
}
