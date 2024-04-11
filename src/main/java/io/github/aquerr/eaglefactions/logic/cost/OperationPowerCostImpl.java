package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationPowerCost;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.UUID;

/**
 * Cost that takes power from player for creating the faction.
 */
public class OperationPowerCostImpl implements OperationPowerCost
{
    private final MessageService messageService;
    private final PowerManager powerManager;
    private final float power;

    public OperationPowerCostImpl(MessageService messageService,
                                  PowerManager powerManager,
                                  float power)
    {
        this.messageService = messageService;
        this.powerManager = powerManager;
        this.power = power;
    }

    @Override
    public void pay(ServerPlayer serverPlayer) throws CostNotSatisfiedException
    {
        float currentPower = powerManager.getPlayerPower(serverPlayer.uniqueId());
        if (currentPower < power)
            throw new CostNotSatisfiedException(messageService.resolveMessage("error.cost.power.not-satisfied-player-power", this.power, currentPower));
        this.powerManager.setPlayerPower(serverPlayer.uniqueId(), currentPower - power);
    }

    @Override
    public void rollBack(ServerPlayer serverPlayer)
    {
        UUID playerUUID = serverPlayer.uniqueId();
        this.powerManager.setPlayerPower(playerUUID, powerManager.getPlayerPower(playerUUID) + this.power);
    }
}
