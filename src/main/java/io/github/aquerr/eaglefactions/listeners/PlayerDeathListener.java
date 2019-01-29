package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class PlayerDeathListener extends AbstractListener
{
    public PlayerDeathListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    public void onPlayerDeath(DestructEntityEvent.Death event)
    {
        if(event.getTargetEntity() instanceof Player)
        {
            Player player = (Player)event.getTargetEntity();

            super.getPlugin().getPowerManager().decreasePower(player.getUniqueId());

            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, String.valueOf(getPlugin().getConfiguration().getConfigFields().getPowerDecrement()) + "\n",
                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", String.valueOf(super.getPlugin().getPowerManager().getPlayerPower(player.getUniqueId())) + "/" + String.valueOf(getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));

            Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());

            if (super.getPlugin().getConfiguration().getConfigFields().getWarZoneWorldNames().contains(player.getWorld().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equals("WarZone")))
            {
                super.getPlugin().getPlayerManager().setDeathInWarZone(player.getUniqueId(), true);
            }

            if (super.getPlugin().getConfiguration().getConfigFields().shouldBlockHomeAfterDeathInOwnFaction())
            {
                Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

                if (optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent() && optionalChunkFaction.get().getName().equals(optionalPlayerFaction.get().getName()))
                {
                    super.getPlugin().getAttackLogic().blockHome(player.getUniqueId());
                }
            }

            if(super.getPlugin().getPVPLogger().isActive() && super.getPlugin().getPVPLogger().isPlayerBlocked(player))
            {
                super.getPlugin().getPVPLogger().removePlayer(player);
            }
        }
    }
}
