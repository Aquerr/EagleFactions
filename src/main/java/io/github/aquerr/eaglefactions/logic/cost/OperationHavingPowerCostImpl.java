package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationHavingPowerCost;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class OperationHavingPowerCostImpl implements OperationHavingPowerCost
{
    private final MessageService messageService;
    private final PowerManager powerManager;
    private final float power;

    public OperationHavingPowerCostImpl(MessageService messageService,
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
        float currentPower = this.powerManager.getPlayerPower(serverPlayer.uniqueId());
        if (currentPower < this.power)
            throw new CostNotSatisfiedException(messageService.resolveMessage("error.cost.power.not-satisfied-player-power", this.power, currentPower));
    }
}
