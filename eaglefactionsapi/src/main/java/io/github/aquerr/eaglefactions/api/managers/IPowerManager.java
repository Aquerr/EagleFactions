package io.github.aquerr.eaglefactions.api.managers;

import io.github.aquerr.eaglefactions.api.entities.Faction;

import java.util.UUID;

public interface IPowerManager
{
    float getPlayerPower(UUID playerUUID);
    float getPlayerMaxPower(UUID playerUUID);

    void decreasePower(UUID playerUUID);

    float getFactionPower(Faction faction);
    float getFactionMaxPower(Faction faction);

    void penalty(UUID playerUUID);
    void addPower(UUID playerUUID, boolean isKillAward);

    void startIncreasingPower(UUID playerUUID);

    int getFactionMaxClaims(Faction faction);

    void setMaxPower(UUID playerUUID, float newPower);

    void setPower(UUID playerUUID, float newPower);
}
