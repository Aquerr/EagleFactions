package io.github.aquerr.eaglefactions.util;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ItemUtil
{
    private static final Logger LOGGER = LogManager.getLogger(ItemUtil.class);

    private ItemUtil()
    {

    }

    public static void pollItemsNeededForClaimFromPlayer(final ServerPlayer player) throws RequiredItemsNotFoundException
    {
        final Map<String, Integer> requiredItems = EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig().getRequiredItemsToClaim();
        final List<ItemStack> itemStacks = convertToItemStackList(requiredItems);
        pollItemsFromPlayer(player, itemStacks);
    }

    public static void pollItemsNeededForCreationFromPlayer(final ServerPlayer player) throws RequiredItemsNotFoundException
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
            //TODO: To fix
            Optional<Item> itemType = Optional.ofNullable(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)));

            if (!itemType.isPresent())
            {
                LOGGER.warn("ItemType has not been found for id = " + itemId);
                continue;
            }

            ItemStack itemStack = new ItemStack(itemType.get(), items.get(requiredItem));

            if (idAndVariant.length == 3)
            {
                //TODO: TO fix...
//                if (itemType.get().block().isPresent())
//                {
//                    final int variant = Integer.parseInt(idAndVariant[2]);
//                    final BlockState blockState = (BlockState) itemType.get().block().get().validStates().toArray()[variant];
//                    itemStack = new ItemStack(blockState.getBlock());
//                }
            }

            itemStacks.add(itemStack);
        }
        return itemStacks;
    }

    public static void pollItemsFromPlayer(final ServerPlayer player, final List<ItemStack> items) throws RequiredItemsNotFoundException
    {
        final Inventory inventory = player.getInventory();

        //Run check loop before... so that player either loses all items or none at all.
        for (final ItemStack itemStack : items)
        {
            if (!inventory.hasAnyMatching(stack -> stack.getItem() == itemStack.getItem() && stack.getCount() == itemStack.getCount()))
                throw new RequiredItemsNotFoundException(itemStack, items);
        }

        for (final ItemStack itemStack : items)
        {
            int slotIndex = inventory.findSlotMatchingItem(new ItemStack(itemStack.getItem()));
            ContainerHelper.removeItem(inventory.items, slotIndex, itemStack.getCount());
        }
    }
}
