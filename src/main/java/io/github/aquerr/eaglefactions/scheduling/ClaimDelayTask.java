package io.github.aquerr.eaglefactions.scheduling;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ClaimDelayTask implements EagleFactionsRunnableTask
{
    private final FactionLogic factionLogic = EagleFactions.getPlugin().getFactionLogic();
    private final Player player;
    private final Faction playerFaction;
    private final Vector3i chunkPosition;
    private final int claimDelay;
    private final boolean shouldClaimByItems;

    private int currentWaitSeconds = 0;

    public ClaimDelayTask(Player player, Faction playerFaction, Vector3i chunkPosition)
    {
        this.player = player;
        this.playerFaction = playerFaction;
        this.chunkPosition = chunkPosition;
        this.claimDelay = EagleFactions.getPlugin().getConfiguration().getConfigFields().getClaimDelay();
        this.shouldClaimByItems = EagleFactions.getPlugin().getConfiguration().getConfigFields().shouldClaimByItems();
    }

    @Override
    public void run()
    {
        if(!chunkPosition.toString().equals(player.getLocation().getChunkPosition().toString()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MOVED_FROM_THE_CHUNK));
            return;
        }

        if(currentWaitSeconds >= claimDelay)
        {
            if(shouldClaimByItems)
            {
                if(factionLogic.addClaimByItems(player, playerFaction, player.getWorld().getUniqueId(), chunkPosition))
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
                }
            }
            else
            {
                factionLogic.addClaim(playerFaction, new Claim(player.getWorld().getUniqueId(), chunkPosition));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
            }
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RESET, currentWaitSeconds));
            currentWaitSeconds++;
        }
    }
}
