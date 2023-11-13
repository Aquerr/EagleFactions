package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.HomeConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.scheduling.AttackClaimTask;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.github.aquerr.eaglefactions.util.WorldUtil.getChunkTopCenter;

public class AttackLogicImpl implements AttackLogic
{
    private final FactionsConfig factionsConfig;
    private final HomeConfig homeConfig;
    private final FactionLogic factionLogic;
    private final MessageService messageService;

    public AttackLogicImpl(final FactionLogic factionLogic,
                           final FactionsConfig factionsConfig,
                           final MessageService messageService,
                           final HomeConfig homeConfig)
    {
        this.factionLogic = factionLogic;
        this.factionsConfig = factionsConfig;
        this.messageService = messageService;
        this.homeConfig = homeConfig;
    }

    @Override
    public void attack(final ServerPlayer player, final Vector3i attackedChunk)
    {
        final AttackClaimTask attackClaimTask = new AttackClaimTask(this.messageService, this.factionsConfig, this.factionLogic, this, player, attackedChunk);
        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(attackClaimTask, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
    }

    @Override
    public void blockClaiming(String factionName)
    {
        //TODO: Make block claiming time configurable...
        EagleFactionsPlugin.ATTACKED_FACTIONS.put(factionName, 120);
        runClaimingRestorer(factionName);
    }

    @Override
    public void runClaimingRestorer(String factionName)
    {
        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(task -> {
            if(EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(factionName))
            {
                int seconds = EagleFactionsPlugin.ATTACKED_FACTIONS.get(factionName);

                if (seconds <= 0)
                {
                    EagleFactionsPlugin.ATTACKED_FACTIONS.remove(factionName);
                    task.cancel();
                }
                else
                {
                    EagleFactionsPlugin.ATTACKED_FACTIONS.replace(factionName, seconds, seconds - 1);
                }
            }
        }, 0, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
    }

    @Override
    public void informAboutAttack(final Faction faction, final ServerLocation location)
    {
        if (!this.factionsConfig.shouldInformAboutAttack())
            return;

        final List<ServerPlayer> playersList = factionLogic.getOnlinePlayers(faction);
        if (this.factionsConfig.shouldShowAttackedClaim())
        {
            playersList.forEach(x -> x.sendMessage(messageService.resolveMessageWithPrefix("attack.claim-at-coords-is-being-attacked-by-an-enemy", getChunkTopCenter(location.world(), location.chunkPosition()).toString())));
        }
        else
        {
            playersList.forEach(x -> x.sendMessage(messageService.resolveMessageWithPrefix("attack.your-faction-is-under-attack")));
        }
    }

    @Override
    public void informAboutDestroying(final Faction faction, final ServerLocation serverLocation)
    {
        if (!this.factionsConfig.shouldInformAboutDestroy())
            return;

        final List<ServerPlayer> playersList = factionLogic.getOnlinePlayers(faction);
        if (this.factionsConfig.shouldShowDestroyedClaim())
        {
            playersList.forEach(x -> x.sendMessage(messageService.resolveMessageWithPrefix("attack.claim-at-coords-has-been-destroyed-by-an-enemy", getChunkTopCenter(serverLocation.world(), serverLocation.chunkPosition()).toString())));
        }
        else
        {
            playersList.forEach(x -> x.sendMessage(messageService.resolveMessageWithPrefix("attack.one-of-your-claims-has-been-destroyed-by-an-enemy")));
        }
    }

    @Override
    public void blockHome(final UUID playerUUID)
    {
        if(EagleFactionsPlugin.BLOCKED_HOME.containsKey(playerUUID))
        {
            EagleFactionsPlugin.BLOCKED_HOME.replace(playerUUID, homeConfig.getHomeBlockTimeAfterDeathInOwnFaction());
        }
        else
        {
            EagleFactionsPlugin.BLOCKED_HOME.put(playerUUID, homeConfig.getHomeBlockTimeAfterDeathInOwnFaction());
            runHomeUsageRestorer(playerUUID);
        }
    }

    @Override
    public void runHomeUsageRestorer(final UUID playerUUID)
    {
        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync((task) -> {
            if (EagleFactionsPlugin.BLOCKED_HOME.containsKey(playerUUID))
            {
                int seconds = EagleFactionsPlugin.BLOCKED_HOME.get(playerUUID);

                if (seconds <= 0)
                {
                    EagleFactionsPlugin.BLOCKED_HOME.remove(playerUUID);
                    task.cancel();
                }
                else
                {
                    EagleFactionsPlugin.BLOCKED_HOME.replace(playerUUID, seconds, seconds - 1);
                }
            }
        }, 0, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
    }

}
