package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;

import java.util.ArrayList;
import java.util.List;

public class FactionChestImpl implements FactionChest
{
    private String factionName; //Reference to faction holding this chest
    private Inventory inventory;

    public FactionChestImpl(final String factionName)
    {
        this.factionName = factionName;
    }

    public FactionChestImpl(final String factionName, final List<SlotItem> items)
    {
        this.factionName = factionName;
        this.inventory = buildInventory(items);
    }

    public FactionChestImpl(final String factionName, final Inventory inventory)
    {
        this.factionName = factionName;
        this.inventory = buildInventory(toSlotItems(inventory));
    }

    @Override
    public String getFactionName()
    {
        return this.factionName;
    }

    @Override
    public List<SlotItem> getItems()
    {
        if(this.inventory == null)
            return new ArrayList<>();
        return toSlotItems(this.inventory);
    }

    @Override
    public Inventory getInventory()
    {
        if(this.inventory == null)
            this.inventory = buildInventory(new ArrayList<>());
        return this.inventory;
    }

    private Inventory buildInventory(final List<SlotItem> slotItems)
    {
        FactionChest factionChest = this;

        //Create inventory
        final Inventory inventory = Inventory.builder()
                .slots(27)
                .completeStructure()
                .build();
        final ViewableInventory viewableInventory = inventory.asViewable().get();
        final InventoryMenu inventoryMenu = viewableInventory.asMenu();
        inventoryMenu.setTitle(Component.text("Faction's chest", NamedTextColor.BLUE));
        inventoryMenu.registerClose((cause, container) ->
        {
            final Faction faction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByName(factionName);
            if(faction != null)
            {
                EagleFactionsPlugin.getPlugin().getFactionLogic().setChest(faction, factionChest);
            }
        });

        //Fill it with items
        int column = 1;
        int row = 1;

        for(final Inventory slot : inventory.slots())
        {
            ItemStack itemStack = getAtPosition(slotItems, row, column);
            if(itemStack != null)
                slot.offer(itemStack);

            column++;
            if(column > 9)
            {
                column = 1;
                row++;
            }
        }

        return inventory;
    }

    private List<SlotItem> toSlotItems(final Inventory inventory)
    {
        final List<FactionChest.SlotItem> slotItemList = new ArrayList<>();
        final List<Slot> slots = inventory.slots();
        int column = 1;
        int row = 1;
        for(Inventory slot : slots)
        {
            ItemStack itemStack = slot.peek();
            if(itemStack != ItemStack.empty())
            {
                slotItemList.add(new FactionChestImpl.SlotItemImpl(column, row, itemStack));
            }

            column++;
            if(column > 9)
            {
                row++;
                column = 1;
            }

            if(row > 3)
                break;
        }
        return slotItemList;
    }

    private ItemStack getAtPosition(final List<SlotItem> items, int row, int column)
    {
        ItemStack itemStack = null;
        for(SlotItem slotItem : items)
        {
            if(slotItem.getRow() == row && slotItem.getColumn() == column)
                itemStack = slotItem.getItem().copy();
        }

        return itemStack;
    }

    public static final class SlotItemImpl implements FactionChest.SlotItem
    {
        private int column;
        private int row;
        private ItemStack item;

        public SlotItemImpl()
        {

        }

        public SlotItemImpl(int column, int row, ItemStack item)
        {
            this.column = column;
            this.row = row;
            this.item = item;
        }

        public int getColumn()
        {
            return this.column;
        }

        public int getRow()
        {
            return this.row;
        }

        public ItemStack getItem()
        {
            return this.item;
        }
    }
}
