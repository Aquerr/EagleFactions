package io.github.aquerr.eaglefactions.util;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemUtil.class);

    public static void pollItemsNeededForClaimFromPlayer(final Player player) throws RequiredItemsNotFoundException
    {
        final Map<String, Integer> requiredItems = EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig().getRequiredItemsToClaim();
        final List<ItemStack> itemStacks = convertToItemStackList(requiredItems);
        pollItemsFromPlayer(player, itemStacks);
    }

    public static void pollItemsNeededForCreationFromPlayer(final Player player) throws RequiredItemsNotFoundException
    {
        final Map<String, Integer> requiredItems = EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig().getRequiredItemsToCreateFaction();
        final List<ItemStack> itemStacks = convertToItemStackList(requiredItems);
        pollItemsFromPlayer(player, itemStacks);
    }

    /**
     * Converts a map that contains mappings between items ids (String) and amount (Integer).
     * Example mapping: minecraft:wool:1 --> 25
     *
     * @param items to convert
     * @return list of itemstacks.
     */
    public static List<ItemStack> convertToItemStackList(final Map<String, Integer> items)
    {
        final List<ItemStack> itemStacks = new ArrayList<>(items.size());
        for (String requiredItem : items.keySet())
        {
            final String[] idAndVariant = requiredItem.split(":");
            final String itemId = idAndVariant[0] + ":" + idAndVariant[1];
            final Optional<ItemType> itemType = RegistryTypes.ITEM_TYPE.get().findValue(RegistryKey.of(RegistryTypes.ITEM_TYPE, ResourceKey.resolve(itemId)));

            if (!itemType.isPresent())
            {
                LOGGER.warn("ItemType has not been found for id = " + itemId);
                continue;
            }

            ItemStack itemStack = ItemStack.builder()
                    .itemType(itemType.get()).build();
            itemStack.setQuantity(items.get(requiredItem));

            if (idAndVariant.length == 3)
            {
                if (itemType.get().block().isPresent())
                {
                    final int variant = Integer.parseInt(idAndVariant[2]);
                    final BlockState blockState = (BlockState) itemType.get().block().get().validStates().toArray()[variant];
                    itemStack = ItemStack.builder().fromBlockState(blockState).build();
                }
            }

            itemStacks.add(itemStack);
        }
        return itemStacks;
    }

    public static void pollItemsFromPlayer(final Player player, final List<ItemStack> items) throws RequiredItemsNotFoundException
    {
        final PlayerInventory inventory = player.inventory().query(PlayerInventory.class).orElse(null);

        //Run check loop before... so that player either loses all items or none at all.
        for (final ItemStack itemStack : items)
        {
            if (!inventory.contains(itemStack))
                throw new RequiredItemsNotFoundException("Could not found required items in player's inventory. Missing item = " + itemStack);
        }

        for (final ItemStack itemStack : items)
        {
            inventory.query(QueryTypes.ITEM_TYPE.get().of(itemStack.type())).poll(itemStack.quantity());
        }
    }
}
