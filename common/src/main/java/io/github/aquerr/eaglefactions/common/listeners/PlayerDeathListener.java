package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class PlayerDeathListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;
    private final PowerConfig powerConfig;

    public PlayerDeathListener(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.POST)
    public void onPlayerDeath(final DestructEntityEvent.Death event)
    {
        if(event.getTargetEntity() instanceof Player)
        {
            final Player player = (Player)event.getTargetEntity();
            super.getPlugin().getPowerManager().decreasePower(player.getUniqueId());

            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_POWER_HAS_BEEN_DECREASED_BY + " ", TextColors.GOLD, this.powerConfig.getPowerDecrement() + "\n",
                    TextColors.GRAY, PluginMessages.CURRENT_POWER + " ", super.getPlugin().getPowerManager().getPlayerPower(player.getUniqueId()) + "/" + super.getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId())));

            final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());

            if (this.protectionConfig.getWarZoneWorldNames().contains(player.getWorld().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equals("WarZone")))
            {
                super.getPlugin().getPlayerManager().setDeathInWarZone(player.getUniqueId(), true);
            }

            if (this.factionsConfig.shouldBlockHomeAfterDeathInOwnFaction())
            {
                final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

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
