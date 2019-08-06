package io.github.aquerr.eaglefactions.api.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.format.TextColor;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;

public interface FactionLogic
{

    Optional<Faction> getFactionByPlayerUUID(UUID playerUUID);

    Optional<Faction> getFactionByChunk(UUID worldUUID, Vector3i chunk);

    @Nullable
    Faction getFactionByName(String factionName);

    List<Player> getOnlinePlayers(Faction faction);

    Set<String> getFactionsNames();

    Map<String, Faction> getFactions();

    void addFaction(Faction faction);

    boolean disbandFaction(String factionName);

    void joinFaction(UUID playerUUID, String factionName);

    void leaveFaction(UUID playerUUID, String factionName);

    void addAlly(String playerFactionName, String invitedFactionName);

    void removeAlly(String playerFactionName, String removedFactionName);

    void addEnemy(String playerFactionName, String enemyFactionName);

    void removeEnemy(String playerFactionName, String enemyFactionName);

    void setLeader(UUID newLeaderUUID, String playerFactionName);

    Set<Claim> getAllClaims();

    void addClaims(Faction faction, List<Claim> claims);

    void addClaim(Faction faction, Claim claim);

    void removeClaim(Faction faction, Claim claim);

    boolean isClaimed(UUID worldUUID, Vector3i chunk);

    boolean isClaimConnected(Faction faction, Claim claimToCheck);

    void setHome(@Nullable UUID worldUUID, Faction faction, @Nullable Vector3i home);

    List<String> getFactionsTags();

    boolean hasOnlinePlayers(Faction faction);

    void removeAllClaims(Faction faction);

    void kickPlayer(UUID playerUUID, String factionName);

    void startClaiming(Player player, Faction faction, UUID worldUUID, Vector3i chunk);

    boolean addClaimByItems(Player player, Faction faction, UUID worldUUID, Vector3i chunk);

    void toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionFlagTypes factionFlagTypes, Boolean flagValue);

    void changeTagColor(Faction faction, TextColor textColor);

    FactionMemberType promotePlayer(Faction faction, Player playerToPromote);

    FactionMemberType demotePlayer(Faction faction, Player playerToDemote);

    void setLastOnline(Faction faction, Instant instantTime);

    void renameFaction(Faction faction, String newFactionName);

    void changeTag(Faction faction, String newTag);

    void setChest(Faction faction, FactionChest inventory);

    void setDescription(Faction faction, String description);

    void setMessageOfTheDay(Faction faction, String motd);

    Inventory convertFactionChestToInventory(FactionChest factionChest);
}
