package io.github.aquerr.eaglefactions.storage.serializers;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class InventorySerializer {
    static final DataQuery SLOT = DataQuery.of("slot");
    static final DataQuery STACK = DataQuery.of("stack");

    public static List<DataView> serializeInventory(Inventory inventory) {
        DataContainer container;
        List<DataView> slots = new LinkedList<>();

        int i = 0;
        ItemStack stack;

        for (Slot inv : inventory.slots()) {
            stack = inv.peek();

            if (stack != ItemStack.empty()) {
                container = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);

                container.set(SLOT, i);
                container.set(STACK, serializeItemStack(stack));

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

        for (Slot slot : inventory.slots()) {
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

    public static DataView serializeItemStack(ItemStack item) {
        return item.toContainer();
    }

    public static ItemStack deserializeItemStack(DataView data) {
        return ItemStack.builder().fromContainer(data).build();
    }
}
