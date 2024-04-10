package io.github.aquerr.eaglefactions.logic.cost;

import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationItemsCost;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OperationItemsCostImpl implements OperationItemsCost
{
    private final MessageService messageService;
    private final List<ItemStack> items;

    public OperationItemsCostImpl(MessageService messageService, List<ItemStack> items)
    {
        this.messageService = messageService;
        this.items = new ArrayList<>(items);
    }

    @Override
    public void pay(ServerPlayer serverPlayer) throws CostNotSatisfiedException
    {
        try
        {
            ItemUtil.pollItemsFromPlayer(serverPlayer, items);
        }
        catch (RequiredItemsNotFoundException exception)
        {
            throw new CostNotSatisfiedException(messageService.resolveMessage("error.cost.items.not-satisfied-player-items", exception.requiredItemsAsString(), exception.missingItemAsString()));
        }
    }
}
