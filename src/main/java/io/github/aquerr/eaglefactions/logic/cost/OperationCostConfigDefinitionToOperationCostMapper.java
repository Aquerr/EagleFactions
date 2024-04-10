package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCostFactory;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.List;
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
            return operationCostFactory.power(definition.getValue().getFloat());
        }
        else if ("having-power".equalsIgnoreCase(definition.getType()))
        {
            return operationCostFactory.havingPower(definition.getValue().getFloat());
        }
        else if ("items".equalsIgnoreCase(definition.getType()))
        {
            try
            {
                return operationCostFactory.items(ItemUtil.convertToItemStackList(prepareItems(definition.getValue().getList(TypeToken.get(String.class)))));
            }
            catch (SerializationException e)
            {
                throw new RuntimeException(e);
            }
        }

        throw new IllegalArgumentException("Unrecognized cost type = " + definition.getType());
    }

    private OperationCostConfigDefinitionToOperationCostMapper()
    {

    }

    private static HashMap<String, Integer> prepareItems(final List<String> itemsToPrepare)
    {
        final HashMap<String, Integer> items = new HashMap<>();
        for (final String itemWithAmount : itemsToPrepare)
        {
            final String[] strings = itemWithAmount.split("\\|");
            final String item = strings[0];
            final int amount = Integer.parseInt(strings[1]);
            items.put(item, amount);
        }
        return items;
    }
}
