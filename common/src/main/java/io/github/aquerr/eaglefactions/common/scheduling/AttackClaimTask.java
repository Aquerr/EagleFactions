package io.github.aquerr.eaglefactions.common.scheduling;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class AttackClaimTask implements EagleFactionsConsumerTask<Task>
{
    int seconds = 1;

    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final AttackLogic attackLogic;
    private final Vector3i attackedChunk;
    private final Player player;

    public AttackClaimTask(final FactionsConfig factionsConfig, final FactionLogic factionLogic, final AttackLogic attackLogic, final Player player, final Vector3i attackedChunk)
    {
        this.factionsConfig = factionsConfig;
        this.factionLogic = factionLogic;
        this.attackLogic = attackLogic;
        this.player = player;
        this.attackedChunk = attackedChunk;
    }

    @Override
    public void accept(Task task)
    {
        if (this.player.health().get() <= 0)
        {
            //TODO: Add translation key for the following text.
            this.player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Attack on claim has been cancelled!"));
            task.cancel();
        }

        if (!this.attackedChunk.equals(this.player.getLocation().getChunkPosition()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MOVED_FROM_THE_CHUNK));
            task.cancel();
        }

        if(this.seconds >= this.factionsConfig.getAttackTime())
        {
            final Optional<Faction> optionalChunkFaction = factionLogic.getFactionByChunk(player.getWorld().getUniqueId(), attackedChunk);
            if (!optionalChunkFaction.isPresent())
            {
                task.cancel();
                return;
            }

            final Faction chunkFaction = optionalChunkFaction.get();
            this.attackLogic.informAboutDestroying(chunkFaction);
            this.player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.CLAIM_DESTROYED));

            final Claim claim = new Claim(player.getWorld().getUniqueId(), attackedChunk);
            factionLogic.destroyClaim(chunkFaction, claim);
            task.cancel();
        }
        else
        {
            //TODO: Add translation key for the following text.
            this.player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.AQUA, "Claim will be destroyed in [", TextColors.GOLD, seconds, TextColors.AQUA, "] seconds"));
            this.seconds++;
        }
    }
}
