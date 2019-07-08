package io.github.aquerr.eaglefactions.api.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public interface AttackLogic
{

    void attack(Player player, Vector3i attackedChunk);

    void blockClaiming(String factionName);

    void runClaimingRestorer(String factionName);

    void informAboutAttack(Faction faction);

    void informAboutDestroying(Faction faction);

    void blockHome(UUID playerUUID);

    void runHomeUsageRestorer(UUID playerUUID);
}
