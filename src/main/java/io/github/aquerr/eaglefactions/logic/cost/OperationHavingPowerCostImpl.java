package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationHavingPowerCost;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static java.lang.String.format;

public class OperationHavingPowerCostImpl implements OperationHavingPowerCost
{
    private final PowerManager powerManager;
    private final float power;

    public OperationHavingPowerCostImpl(PowerManager powerManager,
                                        float power)
    {
        this.powerManager = powerManager;
        this.power = power;
    }

    @Override
    public void pay(ServerPlayer serverPlayer) throws CostNotSatisfiedException
    {
        float currentPower = this.powerManager.getPlayerPower(serverPlayer.uniqueId());
        if (currentPower < this.power)
            throw new CostNotSatisfiedException(format("Power not satisfied! Required power: %f, Player power: %f", this.power, currentPower));
    }
}
