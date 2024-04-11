package io.github.aquerr.eaglefactions.managers.creation;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.exception.CouldNotCreateFactionException;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.creation.FactionCreationManager;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.logic.cost.OperationCostHandler;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Identifiable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.kyori.adventure.text.Component.text;

public class FactionCreationManagerImpl implements FactionCreationManager
{
    private final FactionsConfig factionsConfig;
    private final ChatConfig chatConfig;
    private final StorageManager storageManager;
    private final MessageService messageService;
    private final PlayerManager playerManager;
    private final List<OperationCost> creationCosts = new ArrayList<>();

    public FactionCreationManagerImpl(FactionsConfig factionsConfig,
                                      ChatConfig chatConfig,
                                      StorageManager storageManager,
                                      PlayerManager playerManager,
                                      MessageService messageService)
    {
        this.factionsConfig = factionsConfig;
        this.chatConfig = chatConfig;
        this.storageManager = storageManager;
        this.playerManager = playerManager;
        this.messageService = messageService;
    }

    @Override
    public void setCreationCosts(List<OperationCost> creationCosts)
    {
        checkNotNull(creationCosts);
        this.creationCosts.clear();
        this.creationCosts.addAll(creationCosts);
    }

    @Override
    public void addCreationCost(OperationCost creationCost)
    {
        checkNotNull(creationCost);
        this.creationCosts.add(creationCost);
    }

    @Override
    public void createFaction(Audience audience, String factionName, String factionTag) throws CouldNotCreateFactionException
    {
        try
        {
            if (isServerPlayer(audience))
            {
                ServerPlayer serverPlayer = (ServerPlayer) audience;
                boolean hasAdminMode = this.playerManager.hasAdminMode(serverPlayer.user());
                if (hasAdminMode)
                {
                    serverPlayer.sendMessage(messageService.resolveMessageWithPrefix("general.cost.bypassing-operation-cost-because-of-admin-mode"));
                }
                else
                {
                    payForOperation(serverPlayer);
                }
            }

            doCreateFaction(audience, factionName, factionTag);
        }
        catch (Exception exception)
        {
            throw new CouldNotCreateFactionException(exception.getMessage());
        }
    }

    private void payForOperation(ServerPlayer serverPlayer) throws CostNotSatisfiedException
    {
        OperationCostHandler.payWithRollback(serverPlayer, this.creationCosts);
    }

    private void doCreateFaction(Audience audience, String factionName, String factionTag)
    {
        UUID leaderUUID = Optional.ofNullable(audience)
                .filter(Identifiable.class::isInstance)
                .map(Identifiable.class::cast)
                .map(Identifiable::uniqueId)
                .orElse(null);

        Set<FactionMember> members = new HashSet<>();
        if (leaderUUID != null)
        {
            members.add(new FactionMemberImpl(leaderUUID, Set.of(RankManagerImpl.LEADER_RANK_NAME)));
        }

        final Faction faction = FactionImpl.builder(factionName, text(factionTag, this.chatConfig.getDefaultTagColor()))
                .leader(leaderUUID)
                .members(members)
                .createdDate(Instant.now())
                .ranks(prepareDefaultRanks())
                .protectionFlags(prepareDefaultProtectionFlags())
                .build();

        final boolean isCancelled = EventRunner.runFactionCreateEventPre(
                Optional.ofNullable(audience)
                        .filter(Player.class::isInstance)
                        .map(Player.class::cast)
                        .orElse(null),
                faction
        );

        if (isCancelled)
            return;

        this.storageManager.saveFaction(faction);

        //Update player cache...
        if (leaderUUID != null)
        {
            final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(leaderUUID).orElse(null);
            final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionName, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
            this.playerManager.savePlayer(updatedPlayer);
        }

        notifyServerPlayersAboutNewFaction(faction);
        if (audience != null)
        {
            audience.sendMessage(messageService.resolveMessageWithPrefix("command.create.success", faction.getName()));
        }
        EventRunner.runFactionCreateEventPost(Optional.ofNullable(audience)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .orElse(null), faction);
    }

    private boolean isServerPlayer(Audience audience)
    {
        return audience instanceof ServerPlayer;
    }

    private List<Rank> prepareDefaultRanks()
    {
        List<Rank> defaultRanks = factionsConfig.getDefaultRanks();
        if (defaultRanks.stream().noneMatch(rank -> rank.getName().equalsIgnoreCase(RankManagerImpl.DEFAULT_RANK_NAME)))
        {
            defaultRanks = new ArrayList<>(defaultRanks);
            defaultRanks.add(RankManagerImpl.buildDefaultRank());
        }
        if (defaultRanks.stream().noneMatch(rank -> rank.getName().equalsIgnoreCase(RankManagerImpl.LEADER_RANK_NAME)))
        {
            defaultRanks = new ArrayList<>(defaultRanks);
            defaultRanks.add(RankManagerImpl.buildLeaderRank());
        }

        return defaultRanks;
    }

    private Set<ProtectionFlag> prepareDefaultProtectionFlags()
    {
        return new HashSet<>(Arrays.asList(
                new ProtectionFlagImpl(ProtectionFlagType.TERRITORY_POWER_LOSS, true),
                new ProtectionFlagImpl(ProtectionFlagType.ALLOW_EXPLOSION, true),
                new ProtectionFlagImpl(ProtectionFlagType.MOB_GRIEF, true),
                new ProtectionFlagImpl(ProtectionFlagType.PVP, true),
                new ProtectionFlagImpl(ProtectionFlagType.FIRE_SPREAD, true),
                new ProtectionFlagImpl(ProtectionFlagType.SPAWN_ANIMALS, true),
                new ProtectionFlagImpl(ProtectionFlagType.SPAWN_MONSTERS, true)
        ));
    }

    private void notifyServerPlayersAboutNewFaction(Faction faction)
    {
        if (this.factionsConfig.shouldNotifyWHenFactionCreated())
        {
            Sponge.server().sendMessage(messageService.resolveMessageWithPrefix("command.create.notify-server-about-new-faction", faction.getName()));
        }
    }
}
