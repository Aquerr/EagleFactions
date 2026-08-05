package io.github.aquerr.eaglefactions.managers.claim;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.exception.CouldNotClaimException;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimContext;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.logic.cost.OperationCostHandler;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.scheduling.DelayedClaimTask;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClaimManagerImpl implements ClaimManager
{
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final MessageService messageService;

    private List<OperationCost> claimCosts = new ArrayList<>();

    public ClaimManagerImpl(FactionsConfig factionsConfig,
                            FactionLogic factionLogic,
                            PermsManager permsManager,
                            PlayerManager playerManager,
                            MessageService messageService)
    {
        this.factionsConfig = factionsConfig;
        this.factionLogic = factionLogic;
        this.permsManager = permsManager;
        this.playerManager = playerManager;
        this.messageService = messageService;
    }

    @Override
    public void setClaimCosts(List<OperationCost> creationCosts)
    {
        checkNotNull(creationCosts);
        this.claimCosts.clear();
        this.claimCosts.addAll(creationCosts);
    }

    @Override
    public void addClaimCost(OperationCost creationCost)
    {
        checkNotNull(creationCost);
        this.claimCosts.add(creationCost);
    }

    @Override
    public void claim(ServerPlayer player, Faction faction, ServerLocation serverLocation) throws CouldNotClaimException
    {
        claim(player, faction, serverLocation, factionsConfig.shouldDelayClaim());
    }

    @Override
    public void claim(ServerPlayer player,
                      Faction faction,
                      ServerLocation serverLocation,
                      boolean shouldDelayClaim) throws CouldNotClaimException
    {
        boolean isCancelled = EventRunner.runFactionClaimEventPre(player, faction, serverLocation.world(), serverLocation.chunkPosition());
        if (isCancelled)
            return;

        try
        {
            ClaimContext claimContext = new ClaimContextImpl(player, faction, serverLocation);

            if (shouldDelayClaim)
            {
                EagleFactionsScheduler.getInstance().scheduleWithDelayedInterval(new DelayedClaimTask(
                                messageService,
                                true,
                                factionsConfig.getClaimDelay(),
                                () -> doClaimWithMessageHandling(claimContext), claimContext),
                        1, TimeUnit.SECONDS,
                        1, TimeUnit.SECONDS);
            }
            else
            {
                doClaimOrThrow(claimContext);
            }
        }
        catch (Exception exception)
        {
            throw new CouldNotClaimException(exception.getMessage());
        }
    }

    // Used by delayed claim
    private void doClaimWithMessageHandling(ClaimContext claimContext)
    {
        try
        {
            doClaim(claimContext);
            claimContext.getServerPlayer().sendMessage(messageService.resolveComponentWithMessage("command.claim.land-has-been-successfully-claimed", claimContext.getServerLocation().chunkPosition().toString()));
        }
        catch (CostNotSatisfiedException e)
        {
            claimContext.getServerPlayer().sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.claim.could-not-claim-territory-with-reason", e.getMessage())));
        }
        catch (Exception e)
        {
            claimContext.getServerPlayer().sendMessage(messageService.resolveMessageWithPrefix("error.claim.could-not-claim-territory"));
        }
    }

    private void doClaimOrThrow(ClaimContext claimContext) throws CostNotSatisfiedException
    {
        doClaim(claimContext);
    }

    private void doClaim(ClaimContext claimContext) throws CostNotSatisfiedException
    {
        ServerPlayer player = claimContext.getServerPlayer();
        boolean hasAdminMode = playerManager.hasAdminMode(player.user());
        Faction faction = claimContext.getFaction();
        Faction playerActualFaction = this.factionLogic.getFactionByPlayerUUID(player.uniqueId()).orElse(null);
        ServerWorld serverWorld = claimContext.getServerLocation().world();
        Vector3i chunkPosition = claimContext.getServerLocation().chunkPosition();

        // Let's check if player is in faction with correct permission right before adding the claim.
        if (!hasAdminMode && (playerActualFaction == null
                || !playerActualFaction.getName().equalsIgnoreCase(faction.getName())
                || !this.permsManager.canClaim(player.uniqueId(), playerActualFaction)))
        {
            throw new IllegalStateException(messageService.resolveMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
        }

        // Very important check!
        if (this.factionLogic.isClaimed(serverWorld.key().asString(), chunkPosition))
        {
            throw new IllegalStateException(messageService.resolveMessage("error.claim.place-is-already-claimed"));
        }

        if (hasAdminMode)
        {
            claimContext.getServerPlayer().sendMessage(messageService.resolveMessageWithPrefix("general.cost.bypassing-operation-cost-because-of-admin-mode"));
        }
        else
        {
            payForOperation(claimContext.getServerPlayer());
        }

        this.factionLogic.addClaim(claimContext.getFaction(), toClaim(claimContext.getServerLocation()));
        EventRunner.runFactionClaimEventPost(claimContext.getServerPlayer(), claimContext.getFaction(), serverWorld, chunkPosition);
    }

    private void payForOperation(ServerPlayer serverPlayer) throws CostNotSatisfiedException
    {
        OperationCostHandler.payWithRollback(serverPlayer, this.claimCosts);
    }

    private Claim toClaim(ServerLocation serverLocation)
    {
        return new Claim(serverLocation.world().key().asString(), serverLocation.chunkPosition());
    }
}
