package io.github.aquerr.eaglefactions.logic;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.scheduling.AttackClaimTask;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class AttackLogicImpl implements AttackLogic
{
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;

    public AttackLogicImpl(final FactionLogic factionLogic, final FactionsConfig factionsConfig)
    {
        this.factionLogic = factionLogic;
        this.factionsConfig = factionsConfig;
    }

    @Override
    public void attack(final ServerPlayer player, final Vector3i attackedChunk)
    {
        final AttackClaimTask attackClaimTask = new AttackClaimTask(this.factionsConfig, this.factionLogic, this, player, attackedChunk);
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
            playersList.forEach(x -> x.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.CLAIM_AT_COORDS_IS_BEING_ATTACKED_BY_ENEMY, RED,  ImmutableMap.of(Placeholders.COORDS, Component.text(ParticlesUtil.getChunkCenter(location.world(), location.chunkPosition()).toString(), RED))))));
        }
        else
        {
            playersList.forEach(x -> x.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.YOUR_FACTION_IS_UNDER_ATTACK, RED))));
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
            playersList.forEach(x -> x.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.CLAIM_AT_COORDS_HAS_BEEN_DESTROYED_BY_ENEMY, RED, ImmutableMap.of(Placeholders.COORDS, Component.text(ParticlesUtil.getChunkCenter(serverLocation.world(), serverLocation.chunkPosition()).toString()))))));
        }
        else
        {
            playersList.forEach(x -> x.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.ONE_OF_YOUR_CLAIMS_HAS_BEEN_DESTROYED_BY_AN_ENEMY, RED))));
        }
    }

    @Override
    public void blockHome(final UUID playerUUID)
    {
        if(EagleFactionsPlugin.BLOCKED_HOME.containsKey(playerUUID))
        {
            EagleFactionsPlugin.BLOCKED_HOME.replace(playerUUID, factionsConfig.getHomeBlockTimeAfterDeathInOwnFaction());
        }
        else
        {
            EagleFactionsPlugin.BLOCKED_HOME.put(playerUUID, factionsConfig.getHomeBlockTimeAfterDeathInOwnFaction());
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
