package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCostFactory;
import io.github.aquerr.eaglefactions.util.ItemUtil;

import java.util.Map;

/**
 * This class name is soo beautiful that I just had to comment it ;D (irony)
 */
public class OperationCostConfigDefinitionToOperationCostMapper
{
    public static OperationCost map(OperationCostFactory operationCostFactory,
                                    FactionsConfig.CostConfigDefinition definition)
    {
        if ("power".equalsIgnoreCase(definition.getType()))
        {
            return operationCostFactory.power((float)definition.getValue());
        }
        else if ("having-power".equalsIgnoreCase(definition.getType()))
        {
            operationCostFactory.havingPower((float) definition.getValue());
        }
        else if ("items".equalsIgnoreCase(definition.getType()))
        {
            operationCostFactory.items(ItemUtil.convertToItemStackList((Map<String, Integer>)definition.getValue()));
        }

        throw new IllegalArgumentException("Unrecognized cost type = " + definition.getType());
    }

    private OperationCostConfigDefinitionToOperationCostMapper()
    {

    }
}
