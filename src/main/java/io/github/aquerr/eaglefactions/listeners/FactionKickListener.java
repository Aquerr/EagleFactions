package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.events.FactionKickEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.Text;

import java.util.List;

public class FactionKickListener extends AbstractListener
{
    public FactionKickListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onPlayerFactionKick(FactionKickEvent event)
    {
        Faction faction = event.getFaction();
        IFactionPlayer kickedPlayer = event.getKickedPlayer();

        List<Player> onlineFactionPlayers = super.getPlugin().getFactionLogic().getOnlinePlayers(faction);
        for(Player player : onlineFactionPlayers)
        {
            if(player.getName().equals(kickedPlayer.getName()))
            {
                continue;
            }

            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, "Player " + kickedPlayer.getName() + " has been kicked from the faction."));
        }
    }
}
