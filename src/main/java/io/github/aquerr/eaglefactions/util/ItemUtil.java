package io.github.aquerr.eaglefactions.util;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ItemUtil
{
    private static final Logger LOGGER = LogManager.getLogger(ItemUtil.class);

    /**
     * Converts a map that contains mappings between items ids (String) and quantity (Integer).
     * Example mapping: minecraft:orange_wool --> 25
     *
     * @param items to convert
     * @return list of itemstacks.
     */
    public static List<ItemStack> convertToItemStackList(final Map<String, Integer> items)
    {
        final List<ItemStack> itemStacks = new ArrayList<>(items.size());
        for (Map.Entry<String, Integer> itemEntry : items.entrySet())
        {
            final String itemId = itemEntry.getKey();
            final int quantity = itemEntry.getValue();
            final Optional<ItemType> itemType = RegistryTypes.ITEM_TYPE.get().findValue(ResourceKey.resolve(itemId));

            if (itemType.isEmpty())
            {
                LOGGER.warn("ItemType has not been found for id = " + itemId);
                continue;
            }

            ItemStack itemStack = ItemStack.builder()
                    .itemType(itemType.get())
                    .quantity(quantity)
                    .build();

            itemStacks.add(itemStack);
        }
        return itemStacks;
    }

    public static void pollItemsFromPlayer(final Player player, final List<ItemStack> items) throws RequiredItemsNotFoundException
    {
        final PlayerInventory inventory = (PlayerInventory) player.inventory().query(QueryTypes.INVENTORY_TYPE, PlayerInventory.class);

        //Run check loop before... so that player either loses all items or none at all.
        for (final ItemStack itemStack : items)
        {
            if (!inventory.contains(itemStack))
                throw new RequiredItemsNotFoundException(itemStack, items);
        }

        for (final ItemStack itemStack : items)
        {
            inventory.query(QueryTypes.ITEM_TYPE, itemStack.type()).poll(itemStack.quantity());
        }
    }

    private ItemUtil()
    {

    }
}
