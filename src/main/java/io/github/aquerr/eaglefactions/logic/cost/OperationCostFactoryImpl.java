package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.logic.cost.OperationCostFactory;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationHavingPowerCost;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationItemsCost;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationPowerCost;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;

public class OperationCostFactoryImpl implements OperationCostFactory
{
    private final MessageService messageService;
    private final PowerManager powerManager;

    public OperationCostFactoryImpl(MessageService messageService,
                                    PowerManager powerManager)
    {
        this.messageService = messageService;
        this.powerManager = powerManager;
    }

    @Override
    public OperationPowerCost power(float power)
    {
        return new OperationPowerCostImpl(this.messageService, this.powerManager, power);
    }

    @Override
    public OperationHavingPowerCost havingPower(float power)
    {
        return new OperationHavingPowerCostImpl(this.messageService, this.powerManager, power);
    }

    @Override
    public OperationItemsCost items(List<ItemStack> items)
    {
        return new OperationItemsCostImpl(this.messageService, items);
    }
}
