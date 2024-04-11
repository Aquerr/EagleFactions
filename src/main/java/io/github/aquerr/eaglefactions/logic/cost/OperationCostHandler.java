package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class OperationCostHandler
{
    public static void payWithRollback(ServerPlayer serverPlayer, List<OperationCost> operationCosts) throws CostNotSatisfiedException
    {
        List<OperationCost> handledCosts = new ArrayList<>();

        try
        {
            for (OperationCost claimCost : operationCosts)
            {
                claimCost.pay(serverPlayer);
                handledCosts.add(claimCost);
            }
        }
        catch (Exception exception)
        {
            rollback(serverPlayer, handledCosts);
            throw exception;
        }
    }

    private static void rollback(ServerPlayer serverPlayer, List<OperationCost> handledCosts)
    {
        for (OperationCost operationCost : handledCosts)
        {
            operationCost.rollBack(serverPlayer);
        }
    }

    private OperationCostHandler()
    {

    }
}
