package io.github.aquerr.eaglefactions.storage;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.DataView.SafetyMode;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.*;

public class InventorySerializer {
    static final DataQuery SLOT = DataQuery.of("slot");
    static final DataQuery STACK = DataQuery.of("stack");

    public static List<DataView> serializeInventory(Inventory inventory) {
        DataContainer container;
        List<DataView> slots = new LinkedList<>();

        int i = 0;
        Optional<ItemStack> stack;

        for (Inventory inv : inventory.slots()) {
            stack = inv.peek();

            if (stack.isPresent()) {
                container = DataContainer.createNew(SafetyMode.ALL_DATA_CLONED);

                container.set(SLOT, i);
                container.set(STACK, serializeItemStack(stack.get()));

                slots.add(container);
            }

            i++;
        }

        return slots;
    }

    public static boolean deserializeInventory(List<DataView> slots, Inventory inventory) {
        Map<Integer, ItemStack> stacks = new HashMap<>();
        int i;
        ItemStack stack;
        boolean fail = false;

        for (DataView slot : slots) {
            i = slot.getInt(SLOT).get();
            stack = deserializeItemStack(slot.getView(STACK).get());

            stacks.put(i, stack);
        }

        i = 0;

        for (Inventory slot : inventory.slots()) {
            if (stacks.containsKey(i)) {
                try {
                    slot.set(stacks.get(i));
                } catch (NoSuchElementException e) {
                    slot.clear();

                    fail = true;
                }
            } else {
                slot.clear();
            }

            ++i;
        }

        return fail;
    }

    static DataView serializeItemStack(ItemStack item) {
        return item.toContainer();
    }

    static ItemStack deserializeItemStack(DataView data) {
        return ItemStack.builder().fromContainer(data).build();
    }
}
